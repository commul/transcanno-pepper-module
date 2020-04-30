package TranscannoSplitTagToOne.SplitToOne;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.corpus_tools.pepper.common.DOCUMENT_STATUS;
import org.corpus_tools.pepper.common.PepperConfiguration;
import org.corpus_tools.pepper.core.SelfTestDesc;
import org.corpus_tools.pepper.impl.PepperManipulatorImpl;
import org.corpus_tools.pepper.impl.PepperMapperImpl;
import org.corpus_tools.pepper.modules.PepperManipulator;
import org.corpus_tools.pepper.modules.PepperMapper;
import org.corpus_tools.pepper.modules.PepperModule;
import org.corpus_tools.pepper.modules.PepperModuleProperties;
import org.corpus_tools.pepper.modules.exceptions.PepperModuleException;
import org.corpus_tools.pepper.modules.exceptions.PepperModuleNotReadyException;
import org.corpus_tools.salt.common.SCorpus;
import org.corpus_tools.salt.common.SDocument;
import org.corpus_tools.salt.common.SDocumentGraph;
import org.corpus_tools.salt.common.SDominanceRelation;
import org.corpus_tools.salt.common.SPointingRelation;
import org.corpus_tools.salt.common.SSequentialDS;
import org.corpus_tools.salt.common.SSpan;
import org.corpus_tools.salt.common.SSpanningRelation;
import org.corpus_tools.salt.common.SStructure;
import org.corpus_tools.salt.common.SStructuredNode;
import org.corpus_tools.salt.common.STextualDS;
import org.corpus_tools.salt.common.STextualRelation;
import org.corpus_tools.salt.common.SToken;
import org.corpus_tools.salt.common.impl.SDominanceRelationImpl;
import org.corpus_tools.salt.common.impl.SPointingRelationImpl;
import org.corpus_tools.salt.common.impl.SSpanImpl;
import org.corpus_tools.salt.common.impl.SSpanningRelationImpl;
import org.corpus_tools.salt.common.impl.SStructureImpl;
import org.corpus_tools.salt.core.GraphTraverseHandler;
import org.corpus_tools.salt.core.SAnnotation;
import org.corpus_tools.salt.core.SFeature;
import org.corpus_tools.salt.core.SGraph.GRAPH_TRAVERSE_TYPE;
import org.corpus_tools.salt.core.SLayer;
import org.corpus_tools.salt.core.SNode;
import org.corpus_tools.salt.core.SRelation;
import org.corpus_tools.salt.core.impl.SFeatureImpl;
import org.corpus_tools.salt.graph.Identifier;
import org.corpus_tools.salt.graph.Label;
import org.corpus_tools.salt.graph.Node;
import org.eclipse.emf.common.util.URI;
import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
		TranscannoMapper mapper = new TranscannoMapper();
		return (mapper);
	}

	/**
	 * TranscannoManipulator takes as input a salt structure created importing transcanno with the GenericXMLImporter. and onvert  flattens the structure of an input document and replaces all the annotations by spans. These spans inherit all the attributes of the annotations they derive from.
	 * It does three things:
	 * - it merges into a SSPan and remove the nodes (SToken/SSpan/SStructure) that have the same value for 'annotationsUnifyingAttribute' annotation (see propertes).
	 * - it converts all remaining SSTructure into SSpans spanning the same STokens, 
	 * - it displaces  all annotations present on a SToken into a new SSPan coverging this SToken only.
	 */
	public static class TranscannoMapper extends PepperMapperImpl{

		public TranscannoMapper() {
			super();
		}
		
	
		@Override
		public DOCUMENT_STATUS mapSDocument() {
			SDocument doc = getDocument();
			SDocumentGraph docGraph = doc.getDocumentGraph();
			
			String annotationsUnifyingAttribute = new TranscannoManipulatorProperties().getAnnotationsUnifyingAttribute();
			
			// The list of nodes that will be duplicated and adapted that can later be removed
			ArrayList<SNode> nodesToRemove = new ArrayList<SNode>();
						
			// Step 1 : identifying the nodes that have the same value for the 'annotationsUnifyingAttribute' annotation and the ones that don't
			HashMap<String, ArrayList <SNode> > hashmapIdenticalTagcodeSTokens = new HashMap<String, ArrayList<SNode>>();
			ArrayList<SNode> otherSNodes = new ArrayList<SNode>();
			for (SNode sNode: docGraph.getNodes()){
				
				boolean hasUnifyingAttr = false;
				for(SAnnotation sAnno: sNode.getAnnotations()){
					if(sAnno.getName().equals(annotationsUnifyingAttribute)) {
						if(!hashmapIdenticalTagcodeSTokens.containsKey(sAnno.getValue())){
							hashmapIdenticalTagcodeSTokens.put(sAnno.getValue_STEXT(), new ArrayList<SNode>());
						}
						hashmapIdenticalTagcodeSTokens.get(sAnno.getValue_STEXT()).add(sNode);
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
			for(String tagCodeValue: hashmapIdenticalTagcodeSTokens.keySet()){
				if(hashmapIdenticalTagcodeSTokens.get(tagCodeValue).size() == 1) {
					otherSNodes.add(hashmapIdenticalTagcodeSTokens.get(tagCodeValue).get(0));
					tagCodesToRemove.add(tagCodeValue);
				}
			}
			
			for(String tagCodeToRemove : tagCodesToRemove) {
				hashmapIdenticalTagcodeSTokens.remove(tagCodeToRemove);
			}
			tagCodesToRemove.clear();
			
			// Step 2 : creatring merged version of the nodes that have a same tagcode and the same set of annotations 
			Hashtable<SNode,Hashtable<SToken,Boolean>> coverage = new Hashtable<SNode, Hashtable<SToken,Boolean>>();
			for(String tagcode: hashmapIdenticalTagcodeSTokens.keySet()) {
				SNode mainSNode = null;
				Set<SLayer> mainLayerList = null;
				Hashtable<SToken, Boolean> hashTokensCovered = new Hashtable<SToken, Boolean>();
				
				for(SNode sNode: hashmapIdenticalTagcodeSTokens.get(tagcode)) {
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
								throw new PepperModuleException("Data issue: A sNode to merge has an Annotation with no equivalent on the other nodes it should be merged with... "+sAnno+" Main "+mainSNode+" /// "+mainSNode.getAnnotation(sAnno.getNamespace(), sAnno.getName())+" /// "+mainSNode.getAnnotation(sAnno.getNamespace(), sAnno.getName()).equals(sAnno.getValue()));
							}
						}
					}
						
					if(!(sNode instanceof SToken)) {
						nodesToRemove.add(sNode);
					}
				}

				ArrayList<SToken> tokensCovered = new ArrayList<SToken>(hashTokensCovered.keySet());
				SSpan copySpan = docGraph.createSpan(tokensCovered);
				for(SAnnotation sAnno: mainSNode.getAnnotations()) {
					copySpan.createAnnotation(sAnno.getNamespace(),sAnno.getName(),sAnno.getValue());
				}
				docGraph.addNode(copySpan);
				for(SLayer sLayer: mainLayerList){
					sLayer.addNode(copySpan);
				}
				
			}
			
			// Step 3 : Shaping the SNodes that don't need to be merged into one 
			
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
			
			// Step 4 : removing the nodes that have been changed/adapted 
			
			for(SNode sNode: nodesToRemove){
				for(SRelation sRel: sNode.getOutRelations()) {
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
			
			List <SToken> tokens = docGraph.getTokens();
			//Delete annotations from tokens
			for(SToken tok: tokens){
				tok.removeAll();
			}

			return (DOCUMENT_STATUS.COMPLETED);
		}
		
		private void findCoverage(Hashtable<SNode,Hashtable<SToken,Boolean>> coverage, SNode sNode){
			Hashtable<SToken,Boolean> tokensCovered = new Hashtable<SToken,Boolean>();
			
			//System.out.println("In "+sNode +" rel "+sNode.getOutRelations().size());
			
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
						//System.out.println("Yup "+sNode+" "+sRel);
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
			}else {
				//System.out.println("contain "+sNode+" "+coverage.get(sNode).keySet().size());
			}
			//System.out.println("Out "+sNode);
		}
		
	}
}
