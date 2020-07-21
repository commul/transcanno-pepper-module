package TranscannoSplitTagToOne.SplitToOne;


import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.corpus_tools.pepper.common.DOCUMENT_STATUS;
import org.corpus_tools.pepper.impl.PepperManipulatorImpl;
import org.corpus_tools.pepper.impl.PepperMapperImpl;

import org.corpus_tools.pepper.modules.PepperMapper;
import org.corpus_tools.pepper.modules.exceptions.PepperModuleException;
import org.corpus_tools.salt.common.SDocument;
import org.corpus_tools.salt.common.SDocumentGraph;
import org.corpus_tools.salt.common.SDominanceRelation;
import org.corpus_tools.salt.common.SSpan;
import org.corpus_tools.salt.common.SSpanningRelation;
import org.corpus_tools.salt.common.SStructure;
import org.corpus_tools.salt.common.SStructuredNode;
import org.corpus_tools.salt.common.STextualDS;
import org.corpus_tools.salt.common.STextualRelation;
import org.corpus_tools.salt.common.SToken;
import org.corpus_tools.salt.core.SAnnotation;
import org.corpus_tools.salt.core.SLayer;
import org.corpus_tools.salt.core.SNode;
import org.corpus_tools.salt.core.SRelation;
import org.corpus_tools.salt.graph.Identifier;
import org.eclipse.emf.common.util.URI;
import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * TranscannoManipulator flattens the structure of an input document and replaces all the annotations by spans. These spans inherit all the attributes of the annotations they derive from.
 * If 2 or more annotations have the same tagcode, they are merged into 1 span.
 * "tagcode" is the default name of the attribute that allows to merge 2 or more annotations into 1: if the value of "tagcode" of 2 or more annotations is the same, they are merged into 1 span.
 *	However, it is possible to define another attribute that will allow to merge annotations when having the same value. It is defined via the AnnotationsUnifyingAttribute parameter.
 *	For example, if 2 XML tags with the same value of "id" should be merged together, while launching the TranscannoManipulator, we will define: AnnotationsUnifyingAttribute=id
 *
 *	Before processing tokenises the textual data.
 * 
 * @author NadezdaOkinina
 * @author Lionel Nicolas
 */
@Component(name = "TranscannoManipulatorComponent", factory = "PepperManipulatorComponentFactory")
public class TranscannoManipulator extends PepperManipulatorImpl {
	
	private Hashtable<String,Integer> nbAnnotationsRemoved = new Hashtable<String, Integer>();
	
	// the Logger
	private final static Logger logger = LoggerFactory.getLogger(TranscannoManipulator.class);
		
	// =================================================== mandatory
	// ===================================================
	/**
	 * A constructor for TranscannoManipulator module. Set the coordinates, with which your
	 * module shall be registered. The coordinates (modules name, version and
	 * supported formats) are a kind of a fingerprint, which should make your
	 * module unique.
	 */
	//TODO update contact info
	public TranscannoManipulator() {
		super();
		setName("TranscannoManipulator");
		setSupplierContact(URI.createURI("nadezda.okinina@eurac.edu"));
		setSupplierHomepage(URI.createURI("https://github.com/commul/transcanno-pepper-module"));
		setDesc("Flattens the document structure and replaces dominance relations by spanning relations. In case 2 structures have the same value of tagcode (or another label), merges them into 1 span.");
		setProperties(new TranscannoManipulatorProperties());
	}
	
	public PepperMapper createPepperMapper(Identifier Identifier) {
		TranscannoMapper mapper = new TranscannoMapper(nbAnnotationsRemoved);
		mapper.setProperties(this.getProperties());
		return (mapper);
	}

	/**
	 * TranscannoManipulator takes as input a salt structure created importing transcanno with the GenericXMLImporter. and onvert  flattens the structure of an input document and replaces all the annotations by spans. These spans inherit all the attributes of the annotations they derive from.
	 * It does three things:
	 * - it merges into a SSPan and remove the nodes (SToken/SSpan/SStructure) that have the same value for 'annotationsUnifyingAttribute' annotation (see propertes).
	 * - it converts all remaining SSTructure into SSpans spanning the same STokens, 
	 * - it displaces  all annotations present on a SToken into a new SSPan covering this SToken only.
	 */
	public static class TranscannoMapper extends PepperMapperImpl{
		
		private Hashtable<String,Integer> localNbAnnotationsRemoved = new Hashtable<String, Integer>();
		private Hashtable<String,Integer> globalNbAnnotationsRemoved;
		
		public TranscannoMapper(Hashtable<String,Integer> globalNbAnnotationsRemoved){
			super();
			this.globalNbAnnotationsRemoved = globalNbAnnotationsRemoved;
		}
		
	
		@Override
		public DOCUMENT_STATUS mapSDocument() {
			SDocument doc = getDocument();
			SDocumentGraph docGraph = doc.getDocumentGraph();
			TranscannoManipulatorProperties prop = (TranscannoManipulatorProperties) this.getProperties();
			
			int sizeText = 0;
			for(STextualDS sTextDs: docGraph.getTextualDSs()) {
				if(sTextDs.getText() != null) {
					sizeText += sTextDs.getText().length();
				}else{
					throw new PepperModuleException("The document contains an empty StextualDs... "+sTextDs);
				}
			}
			
			if(sizeText < prop.getMinTextSize()) {
				throw new PepperModuleException("The number of caracters in this document ("+sizeText+") is inferior to the min set in the properties 'MinTextSize' ("+prop.getMinTextSize()+")");
			}
			
			String annotationsUnifyingAttribute = prop.getAnnotationsUnifyingAttribute();
			String deepLogRegExp = prop.getDeepLogRegExp();
			String extraDeepLogRegExp = prop.getExtraDeepLogRegExp();
			
			// The list of nodes that will be duplicated and adapted that can later be removed
			ArrayList<SNode> nodesToRemove = new ArrayList<SNode>();
						
			// Step 1 : identifying the nodes that have the same value for the 'annotationsUnifyingAttribute' annotation and the ones that don't
			HashMap<String, ArrayList <SNode> > hashmapIdenticalUnifyingAttr = new HashMap<String, ArrayList<SNode>>();
			ArrayList<SNode> otherSNodes = new ArrayList<SNode>();
			{
				for (SNode sNode: docGraph.getNodes()){
					
					boolean hasUnifyingAttr = false;
					for(SAnnotation sAnno: sNode.getAnnotations()){
						if(sAnno.getName().equals(annotationsUnifyingAttribute)) {
							if(!hashmapIdenticalUnifyingAttr.containsKey(sAnno.getValue())){
								hashmapIdenticalUnifyingAttr.put(sAnno.getValue_STEXT(), new ArrayList<SNode>());
							}
							hashmapIdenticalUnifyingAttr.get(sAnno.getValue_STEXT()).add(sNode);
							sNode.removeLabel(sAnno.getNamespace(), sAnno.getName());
							hasUnifyingAttr = true;
							break;
						}
					}
					if(hasUnifyingAttr == false) {
						otherSNodes.add(sNode);
					}
				}
				
				ArrayList<String> tagCodesToRemove = new ArrayList<String>(); 
				for(String tagCodeValue: hashmapIdenticalUnifyingAttr.keySet()){
					if(hashmapIdenticalUnifyingAttr.get(tagCodeValue).size() == 1) {
						otherSNodes.add(hashmapIdenticalUnifyingAttr.get(tagCodeValue).get(0));
						tagCodesToRemove.add(tagCodeValue);
					}
				}
				
				for(String tagCodeToRemove : tagCodesToRemove) {
					hashmapIdenticalUnifyingAttr.remove(tagCodeToRemove);
				}
				tagCodesToRemove.clear();
			}
			
			// Step 2 : creating merged version of the nodes that have a same tagcode and the same set of annotations 
			Hashtable<SNode,Hashtable<SToken,Boolean>> coverage = new Hashtable<SNode, Hashtable<SToken,Boolean>>();
			{
				for(String unifyingAttr: hashmapIdenticalUnifyingAttr.keySet()) {
					SNode mainSNode = null;
					Set<SLayer> mainLayerList = null;
					Hashtable<SToken, Boolean> hashTokensCovered = new Hashtable<SToken, Boolean>();
					
					ArrayList<SNode> nodesWithIndenticalTagcode = hashmapIdenticalUnifyingAttr.get(unifyingAttr);
					int nbNodesWithSameUnifyingAttr = nodesWithIndenticalTagcode.size();
					for(SNode sNode: nodesWithIndenticalTagcode) {
						findCoverage(coverage,sNode);
						for(SToken sToken: coverage.get(sNode).keySet()) {
							hashTokensCovered.put(sToken, true);
						}
						
						if(mainSNode == null) {
							mainSNode = sNode;
							mainLayerList = sNode.getLayers();
						}else {
							for(SAnnotation sAnno: sNode.getAnnotations()) {
								if((mainSNode.getAnnotation(sAnno.getNamespace(), sAnno.getName()) == null)
									|| (!mainSNode.getAnnotation(sAnno.getNamespace(), sAnno.getName()).getValue().equals(sAnno.getValue()))) {
									throw new PepperModuleException("Data issue: A sNode to merge has an Annotation with no equivalent on the other nodes it should be merged with... "+sAnno+" Main "+mainSNode+" /// "+mainSNode.getAnnotation(sAnno.getNamespace(), sAnno.getName()));
								}
							}
						}
							
						if(!(sNode instanceof SToken)) {
							nodesToRemove.add(sNode);
						}
					}
	
					ArrayList<SToken> tokensCovered = new ArrayList<SToken>(hashTokensCovered.keySet());
					if(tokensCovered.size() != 0) {
						SSpan copySpan = docGraph.createSpan(tokensCovered);
						for(SAnnotation sAnno: mainSNode.getAnnotations()) {
							copySpan.createAnnotation(sAnno.getNamespace(),sAnno.getName(),sAnno.getValue());
							String annoHashKey;
							if(sAnno.getName().matches(deepLogRegExp)) {
								annoHashKey = sAnno.getNamespace()+":"+sAnno.getName()+":"+sAnno.getValue_STEXT();
							}else if(sAnno.getName().matches(extraDeepLogRegExp)) {
								annoHashKey = doc.getId()+":"+sAnno.getNamespace()+":"+sAnno.getName()+":"+sAnno.getValue_STEXT();	
							}else {
								annoHashKey = sAnno.getNamespace()+":"+sAnno.getName();
							}
							if(localNbAnnotationsRemoved.containsKey(annoHashKey)) {
								localNbAnnotationsRemoved.put(annoHashKey, localNbAnnotationsRemoved.get(annoHashKey) + nbNodesWithSameUnifyingAttr - 1);
							}else {
								localNbAnnotationsRemoved.put(annoHashKey,nbNodesWithSameUnifyingAttr - 1);
							}
						}
						docGraph.addNode(copySpan);
						for(SLayer sLayer: mainLayerList){
							sLayer.addNode(copySpan);
						}
					}else{
						throw new PepperModuleException("Annotations on empty string... the set of Nodes to merge with node '"+mainSNode+"'");
					}
				}		
			}
			
			// Step 3 : Shaping the SNodes that don't need to be merged into one 
			{
				for(SNode sNode: otherSNodes){
					if(sNode instanceof SStructure) {
						if(!sNode.getName().equals("root")) { //all except the root
							findCoverage(coverage,sNode);
							
							ArrayList<SToken> tokensCovered = new ArrayList<SToken>(coverage.get(sNode).keySet());
							if(tokensCovered.size() != 0){
								SSpan copySpan = docGraph.createSpan(tokensCovered);
								for(SAnnotation sAnno: sNode.getAnnotations()) {
									copySpan.createAnnotation(sAnno.getNamespace(),sAnno.getName(),sAnno.getValue());
								}
								
								//System.out.println("Adding "+copySpan);
								docGraph.addNode(copySpan);
								for(SLayer sLayer: sNode.getLayers()){
									sLayer.addNode(copySpan);
								}
							}else {
								logger.warn("Faulty content... A SStructure that spans no text has been detected and its annotations will be removed'"+sNode+"'");
								for(SAnnotation sAnno: sNode.getAnnotations()) {
									String annoHashKey;
									if(sAnno.getName().matches(deepLogRegExp)) {
										annoHashKey = sAnno.getNamespace()+":"+sAnno.getName()+":"+sAnno.getValue_STEXT();
									}else if(sAnno.getName().matches(extraDeepLogRegExp)) {
										annoHashKey = doc.getId()+":"+sAnno.getNamespace()+":"+sAnno.getName()+":"+sAnno.getValue_STEXT();	
									}else {
										annoHashKey = sAnno.getNamespace()+":"+sAnno.getName();
									}
									if(localNbAnnotationsRemoved.containsKey(annoHashKey)) {
										localNbAnnotationsRemoved.put(annoHashKey, localNbAnnotationsRemoved.get(annoHashKey) + 1);
									}else {
										localNbAnnotationsRemoved.put(annoHashKey,1);
									}
								}
							}
						}
						
						nodesToRemove.add(sNode);
					}else if(sNode instanceof SToken) {
						if(sNode.getAnnotations().size() != 0) {
							ArrayList<SToken> tokensCovered = new ArrayList<SToken>();
							tokensCovered.add((SToken) sNode);
							
							SSpan copySpan = docGraph.createSpan(tokensCovered);
							for(SAnnotation sAnno: sNode.getAnnotations()) {
								copySpan.createAnnotation(sAnno.getNamespace(),sAnno.getName(),sAnno.getValue());
							}
							sNode.removeAll();
								
							//System.out.println("Adding "+copySpan);
							docGraph.addNode(copySpan);
							for(SLayer sLayer: sNode.getLayers()){
								sLayer.addNode(copySpan);
							}
						}
					}else {
						// Do nothing 
						
					}
				}
			}
			
			// Step 4 : removing the nodes that have been changed/adapted 
			{
				for(SNode sNode: nodesToRemove){
					for( SRelation sRel: sNode.getOutRelations()) {
						if((sNode instanceof SStructure) && (sRel instanceof SDominanceRelation)){
							docGraph.removeRelation(sRel);
						}else if((sNode instanceof SSpan) && (sRel instanceof SSpanningRelation)){
							docGraph.removeRelation(sRel);
						}else if((sNode instanceof SToken) && (sRel instanceof STextualRelation)){
							//do nothing
						}else {
							//unforeseen type of SRelation, it is not critical but worth giving a warning.
							logger.warn("Unkown type of SRelation met on a node '"+sNode+"'to discard after merging => "+sRel);
						}
					}
				
					for(SLayer sLayer: sNode.getLayers()){
						sLayer.removeNode(sNode);
					}
					docGraph.removeNode(sNode);
				}
			}
			
			//Delete annotations from tokens
			for(SToken tok: docGraph.getTokens()){
				tok.removeAll();
			}
			
			
			synchronized(this.globalNbAnnotationsRemoved) {
				for(String annoHashKey : this.localNbAnnotationsRemoved.keySet()){
					if(this.globalNbAnnotationsRemoved.containsKey(annoHashKey)) {
						this.globalNbAnnotationsRemoved.put(annoHashKey, this.localNbAnnotationsRemoved.get(annoHashKey) + this.globalNbAnnotationsRemoved.get(annoHashKey));
					}else {
						this.globalNbAnnotationsRemoved.put(annoHashKey, this.localNbAnnotationsRemoved.get(annoHashKey));
					}
				}
			}
			

			return (DOCUMENT_STATUS.COMPLETED);
		}
		
		private void findCoverage(Hashtable<SNode,Hashtable<SToken,Boolean>> coverage, SNode sNode){
			Hashtable<SToken,Boolean> tokensCovered = new Hashtable<SToken,Boolean>();
			
			if(!coverage.containsKey(sNode)) {
				if(sNode instanceof SToken) {
					tokensCovered.put((SToken) sNode,true);
				}else if(sNode instanceof SSpan){
					for(SRelation sRel: sNode.getOutRelations()) {
						if(sRel instanceof SSpanningRelation) {
							tokensCovered.put((SToken) sRel.getTarget(),true);
						}
					}
				}else if(sNode instanceof SStructure) {
					for(SRelation sRel: sNode.getOutRelations()) {
						if(sRel instanceof SDominanceRelation){
							SStructuredNode target = (SStructuredNode) sRel.getTarget();
							findCoverage(coverage,target);
							
							for(SToken sToken: coverage.get(target).keySet()) {
								tokensCovered.put(sToken,true);
							}
						}
					}
				}else {
					throw new PepperModuleException("Unforeseen use case: uable to establish the token coverage for such type of node => "+sNode);
				}
				coverage.put(sNode,tokensCovered);
			}
		}
		
	}
	
	public void end(){
	    super.end();
	    String log_file_path = ((TranscannoManipulatorProperties) this.getProperties()).getLogFilePath();
		if(!log_file_path.equals("")) {
			
			StringBuilder fileContent = new StringBuilder();
			
			List<String> sortedKeys = Collections.list(nbAnnotationsRemoved.keys());
			Collections.sort(sortedKeys);
			Iterator<String> iterator = sortedKeys.iterator();

			while(iterator.hasNext()){
			    String annoHashKey =iterator.next();
			    int nbAnnoRemoved = nbAnnotationsRemoved.get(annoHashKey);
			    fileContent.append("Nb "+annoHashKey+" removed => "+nbAnnoRemoved+"\n");
			}
	
			BufferedWriter writer;
			try {
				writer = new BufferedWriter(new FileWriter(log_file_path));
				writer.write(fileContent.toString());
			    writer.close();
			} catch (IOException e) {
				throw new PepperModuleException("Could not output information to log file '"+log_file_path+"' message:"+e.getMessage()+ "\nStack\n"+e.getStackTrace());
			}
		}
	}
}
