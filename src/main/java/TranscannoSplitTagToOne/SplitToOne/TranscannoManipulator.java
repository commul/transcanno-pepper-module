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
import org.corpus_tools.salt.core.SNode;
import org.corpus_tools.salt.core.SRelation;
import org.corpus_tools.salt.core.impl.SFeatureImpl;
import org.corpus_tools.salt.graph.Identifier;
import org.corpus_tools.salt.graph.Label;
import org.corpus_tools.salt.graph.Node;
import org.eclipse.emf.common.util.URI;
import org.osgi.service.component.annotations.Component;

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
 */
@Component(name = "TranscannoManipulatorComponent", factory = "PepperManipulatorComponentFactory")
public class TranscannoManipulator extends PepperManipulatorImpl {
	// =================================================== mandatory
	// ===================================================
	/**
	 * A constructor for TranscannoManipulator module. Set the coordinates, with which your
	 * module shall be registered. The coordinates (modules name, version and
	 * supported formats) are a kind of a fingerprint, which should make your
	 * module unique.
	 */
	public TranscannoManipulator() {
		super();
		setName("TranscannoManipulator");
		// TODO change suppliers e-mail address
		setSupplierContact(URI.createURI("nadezda.okinina@eurac.edu"));
		// TODO change suppliers homepage
		setSupplierHomepage(URI.createURI("https://github.com/commul/transcanno-pepper-module"));
		// TODO add a description of what your module is supposed to do
		setDesc("Flattens the document structure and replaces dominance relations by spanning relations. In case 2 structures have the same value of tagcode (or another label), merges them into 1 span.");
		setProperties(new TranscannoManipulatorProperties());
	}
	/*
	@Override
    public SelfTestDesc getSelfTestDesc() {
            return new SelfTestDesc(
                            getResources().appendSegment("transcannomanipulator_testcorpus"),
                            getResources().appendSegment("transcannomanipulator_testout"));
    }
	*/
	/**
	 * <strong>OVERRIDE THIS METHOD FOR CUSTOMIZATION</strong> <br/>
	 * This method creates a customized {@link PepperMapper} object and returns
	 * it. You can here do some additional initialisations. Thinks like setting
	 * the {@link Identifier} of the {@link SDocument} or {@link SCorpus} object
	 * and the {@link URI} resource is done by the framework (or more in detail
	 * in method {@link #start()}). The parameter <code>Identifier</code>, if a
	 * {@link PepperMapper} object should be created in case of the object to
	 * map is either an {@link SDocument} object or an {@link SCorpus} object of
	 * the mapper should be initialized differently. <br/>
	 * 
	 * @param Identifier
	 *            {@link Identifier} of the {@link SCorpus} or {@link SDocument}
	 *            to be processed.
	 * @return {@link PepperMapper} object to do the mapping task for object
	 *         connected to given {@link Identifier}
	 */
	public PepperMapper createPepperMapper(Identifier Identifier) {
		TranscannoMapper mapper = new TranscannoMapper();
		return (mapper);
	}

	/**
	 * TranscannoManipulator flattens the structure of an input document and replaces all the annotations by spans. These spans inherit all the attributes of the annotations they derive from.
	 * If 2 or more annotations have the same tagcode, they are merged into 1 span.
	 * "tagcode" is the default name of the attribute that allows to merge 2 or more annotations into 1: if the value of "tagcode" of 2 or more annotations is the same, they are merged into 1 span.
	 *	However, it is possible to define another attribute that will allow to merge annotations when having the same value. It is defined via the AnnotationsUnifyingAttribute parameter.
	 *	For example, if 2 XML tags with the same value of "id" should be merged together, while launching the TranscannoManipulator, we will define: AnnotationsUnifyingAttribute=id
	 *
	 *	Before processing tokenises the textual data.
	 */
	public static class TranscannoMapper extends PepperMapperImpl implements GraphTraverseHandler {
		//Will contain the name of the label that will allow to merge 2 or more structures, if they have the same value of this label.
		private String annotationsUnifyingAttribute;
		
		/**
		 * Constructor of TranscannoMapper.
		 * Initialises the annotationsUnifyingAttribute:
		 * 		either with the value entered by the user or with the default value "tagcode".
		 */
		public TranscannoMapper() {
			super();
			annotationsUnifyingAttribute = new TranscannoManipulatorProperties().getAnnotationsUnifyingAttribute();
			if (annotationsUnifyingAttribute==null){
				annotationsUnifyingAttribute = "tagcode";
			}
		}
		
		/**
		 * Creates meta annotations, if not already exists
		 */
		@Override
		public DOCUMENT_STATUS mapSCorpus() {
			if (getCorpus().getMetaAnnotation("date") == null) {
				SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
				String dateString = format.format(new Date());
				getCorpus().createMetaAnnotation(null, "date", dateString);
			}
			return (DOCUMENT_STATUS.COMPLETED);
		}

		/**
		 * Flattens the document structure.
		 * Replaces structures by spans.
		 * If 2 or more structures have identical tagcodes (or values of another, user-specified, attribute(=label)), they are replaced by 1 span.
		 * Before processing tokenises the textual data.
		 */
		@Override
		public DOCUMENT_STATUS mapSDocument() {				
			SDocument doc = getDocument();
			SDocumentGraph docGraph = doc.getDocumentGraph();
			//Tokenises the textual data
			docGraph.tokenize();
			
			//Transform into structures with name "line" the divs that don't contain annotations inside and therefore were not transformed into structures my GenericXMLImporter, but were transformed into spans
			transformLineSpansWithoutAnnotationsIntoStructures(docGraph);
			
			//Will contain nodes (structures and tokens) with identical tagcodes
			HashMap<String, List <SNode> > hashmapIdenticalTagcodeSTokens = new HashMap<String, List <SNode>>();
			//Have to find structures and tokens, not only the ones or the others
			List <SNode> sNodes = getDocument().getDocumentGraph().getNodes();
			
			//Fill the hashmap with nodes having identical tagcodes
			fillHashMapOfNodesWithIdenticalTagcodes(hashmapIdenticalTagcodeSTokens, sNodes);

			//Create a span for each list of nodes having identical tagcodes and associate the tokens (pointing to words of text) to this span
			createSpansForIdenticalTagcodes(hashmapIdenticalTagcodeSTokens, docGraph);
            
			//Replace all structures by spans
			replaceStructuresWithSpans(docGraph);
			
			//Remove duplicate tokens
			removeDuplicateTokens(docGraph);
			
			List <SToken> tokens = docGraph.getTokens();
			//Delete annotations from tokens
			for(SToken tok: tokens){
				tok.removeAll();
			}

			return (DOCUMENT_STATUS.COMPLETED);
		}
		
		/**
		 * Transform into structures with name "line"
		 * the divs that don't contain annotations inside and therefore were not transformed into structures my GenericXMLImporter,
		 * but were transformed into spans.
		 * @param  docGraph  document graph of the document
		 */
		private void transformLineSpansWithoutAnnotationsIntoStructures(SDocumentGraph docGraph){
			boolean breakNow;
			List <SSpan> spans = docGraph.getSpans();
			for (SNode span : spans){
				breakNow = false;
				List <SRelation> inRelations = span.getInRelations();
				for (SRelation inrel: inRelations){
					Node struct = inrel.getSource();
					Collection <Label> structLabels = struct.getLabels();
					for (Label label: structLabels){
						if(label.getName().equals("SNAME") && label.getValue().equals("p")){
							SStructure structure = docGraph.createStructure((SStructuredNode)span);
							structure.setName("line");
							breakNow = true;
							break;
						}
					}
					if(breakNow){
						break;
					}
					
				}
			}
		}
				
		/**
		 * Replaces structures by spans.
		 * Does not take into account structures having tagcodes (= Transc&Anno annotations), because they are taken care of in a different function.
		 * 
		 * @param  docGraph  document graph of the document
		 */
		private void replaceStructuresWithSpans(SDocumentGraph docGraph){
			List <SStructure> listStructures = docGraph.getStructures();
			
			//Find all the tokens contained in the structure
			for (SStructure struct: listStructures){
				//We exclude structures with tagcodes, because we have already taken care of them
				if (struct.getLabel(annotationsUnifyingAttribute)==null && !struct.getName().equals("root") && !struct.getName().equals("p")){
					List <SToken> tokensArray = new ArrayList <SToken>();
					
					if(struct.getName().equals("div")  && struct.getLabel("class")!=null){
						continue;
					}
					else if(struct.getName().equals("div")){
						struct.setName("line");
					}
					
					addTokensToList((SNode) struct, tokensArray);
					/*
					System.out.println("\nin replaceStructuresWithSpans ");
					System.out.println("struct.toString(): " + struct.toString());
					System.out.println("tokensArray.size(): " + tokensArray.size());
					*/
					//Put all those tokens into a new span
					if (tokensArray.size() > 0){
						createNewSpan(docGraph, (SNode) struct, tokensArray);
					}
				}

			}
			
			//Remove all the structures
			List<Object> list2 = listStructures.stream().collect(Collectors.toList());			
			for (Object s: list2){
				docGraph.removeNode((SNode) s);
			}
		
			return;
		}
		
		/**
		 * Creates a new span overlapping a given list of tokens and having the characteristics of a given node.
		 *  
		 * @param  docGraph  document graph of the document
		 * @param  mainNode  the node that will give its name, id and labels to the span that will be created
		 * @param  overlappingTokens  list of tokens that will be overlapped by the span that will be created
		 */
		private void createNewSpan(SDocumentGraph docGraph, SNode mainNode, List <SToken> overlappingTokens){			
			//create span overlaping a set of tokens        
    		SSpan newSpan= docGraph.createSpan(overlappingTokens);
    		/*
    		System.out.println("\nin createNewSpan ");
    		System.out.println("mainNode.toString(): " + mainNode.toString());
    		System.out.println("mainNode.getName(): " + mainNode.getName());
    		System.out.println("overlappingTokens.size(): " + overlappingTokens.size());
    		System.out.println("newSpan.toString(): " + newSpan.toString());
    		*/
    		//Give to the new span the caracteristics of the given node
    		newSpan.setName(mainNode.getName());
    		newSpan.setId(mainNode.getId());

    		Collection <Label> labels = mainNode.getLabels(); 
    		for (Label l: labels){
    			if (((String)l.getName()).equals("SNAME")){
    				newSpan.createAnnotation(null, (String)l.getValue(), (String)l.getValue());
    			}else if (((String)l.getName()).equals("class") || ((String)l.getName()).equals("id") || ((String)l.getName()).equals("mode") || ((String)l.getName()).equals("tagcode") ){
    				continue;
    			}
    			else{
    				newSpan.createAnnotation(null, (String)l.getName(), (String)l.getValue());
    			}
    		}

    		//Add the new SSpan to the documentGraph
    		docGraph.addNode(newSpan);
    		return;
		}
		
		/**
		 * Adds the tokens contained in the node to the list of tokens that will be in the span
		 * 
		 * @param  node  the node whose tokens we want to place into the list
		 * @param  overlappingTokens  list of tokens that will be filled by the function
		 */
		private void addTokensToList (SNode node, List<SToken> overlappingTokens){
			List <SRelation> nodeOutRelations = node.getOutRelations();
			//Loop through the outgoing relations of the node having the tagcode
        	for (SRelation rel : nodeOutRelations){
        		String relationClass = rel.getClass().toString();

        		if (relationClass.equals("class org.corpus_tools.salt.common.impl.STextualRelationImpl")){        			
        			overlappingTokens.add((SToken)node);	
        		}
        		else if (relationClass.equals("class org.corpus_tools.salt.common.impl.SDominanceRelationImpl")){
        			//Run down the tree till I find a Textual Relation
        			SNode targetNode=(SNode)rel.getTarget();
        			addTokensToList (targetNode, overlappingTokens);
        		}
        		else if (relationClass.equals("class org.corpus_tools.salt.common.impl.SSpanningRelationImpl")){
        			//Run down the tree till I find a Textual Relation
        			SToken targetNode=(SToken)rel.getTarget();      			
        			addTokensToList(targetNode, overlappingTokens);
        		}
        	}
        	return;
		}
		
		/**
		 * Removes duplicate tokens from the document graph.
		 * By duplicate tokens  we intend tokens that point to exactly the same text fragment.
		 * 
		 * @param  docGraph  document graph of the document
		 */	
		private void removeDuplicateTokens(SDocumentGraph docGraph){
			List <SToken> tokens = docGraph.getTokens();
			
			HashMap <String, List<SToken>> hashOfTokensWithIdenticalTextualDSReferences=new HashMap <String, List<SToken>>();
			fillhashOfTokensWithIdenticalTextualDSReferences(hashOfTokensWithIdenticalTextualDSReferences,tokens);
			cleanDuplicateTokens(hashOfTokensWithIdenticalTextualDSReferences, docGraph);
			return;
		}
		
		/**
		 * Removes duplicate tokens registered in the hashmap from the document graph.
		 * 
		 * @param  hashOfTokensWithIdenticalTextualDSReferences  hashmap containing lists of tokens pointing to the same text fragments.
		 * 				key: a string containing the beginning and the end positions of the TextualDS where these tokens point to
		 * 				value: list of tokens pointing to this text fragment
		 * @param  docGraph  document graph of the document
		 */	
		private void cleanDuplicateTokens(HashMap <String, List<SToken>> hashOfTokensWithIdenticalTextualDSReferences, SDocumentGraph docGraph){
			Iterator it = hashOfTokensWithIdenticalTextualDSReferences.entrySet().iterator();
        	while (it.hasNext()) {
        		Map.Entry pair = (Map.Entry)it.next();
        		List<SToken> listSTokens = (List<SToken>)pair.getValue();
        		
        		if(listSTokens.size()>1){

        			SToken mainToken = listSTokens.get(0);
        			List <SRelation> mainTokenInRelations = mainToken.getInRelations();

        			//Transfer all the ingoing relations from the tokens to the main token
        			for (SToken token : listSTokens){
        				if(!token.equals(mainToken)){
        					List <SRelation> tokenInRelations = token.getInRelations();

        					for(SRelation rel: tokenInRelations){
        						Boolean sameRel=false;

        						for (SRelation mainRel: mainTokenInRelations){
        							if(rel.equals(mainRel)){
        								sameRel=true;
        							}
        						}

        						if (sameRel==false){
        							rel.setTarget(mainToken);
        						}
        					}
        				}
        			}

        			//remove all tokens apart from the main token
        			for (SToken token : listSTokens){
        				if(!token.equals(mainToken)){
        					docGraph.removeNode(token);
        				}
        			}

        			//Remove all labels apart from id and SNAME from the maintoken
        			Collection <Label> labels = mainToken.getLabels();
        			List <String> labelsToRemove = new ArrayList <String>();

        			for (Label l: labels){

        				String labelName= (String)l.getName();

        				if (!labelName.equals("id") && !labelName.equals("SNAME")){
        					labelsToRemove.add(labelName);
        				}
        			}

        			for (String la: labelsToRemove){
        				mainToken.removeLabel(la);
        			}
        			
        		}
        	}
		}
		
		/**
		 * Fills a hashmap with lists of tokens pointing to the same text fragments.
		 * 
		 * @param  hashOfTokensWithIdenticalTextualDSReferences  hashmap that will contain lists of tokens pointing to the same text fragments.
		 * 				key: a string containing the beginning and the end positions of the TextualDS where these tokens point to
		 * 				value: list of tokens pointing to this text fragment
		 * @param  tokens  list of tokens among which we should find duplicates: those that point to the same textual fragments
		 */
		private void fillhashOfTokensWithIdenticalTextualDSReferences(HashMap <String, List<SToken>> hashOfTokensWithIdenticalTextualDSReferences, List <SToken> tokens){
			for (SToken token : tokens){
				
				List <SRelation> tokenOutRelations = token.getOutRelations();
				//Loop through the outgoing relations of the node having the tagcode
	        	for (SRelation rel : tokenOutRelations){

	        		if (rel.getClass().toString().equals("class org.corpus_tools.salt.common.impl.STextualRelationImpl")){
	        			//Collect information about the token in order to create a new one
	        			Node textualDS = rel.getTarget();
	        			Integer start=null;
	        			Integer end=null;
	        			Collection <Label> relLabels = rel.getLabels();
	        			for (Label rl : relLabels){
	        				String rlabelName= (String)rl.getName();
	    	            	
	    	            	if (rlabelName.equals("SSTART")){
	    	            		start=(Integer)rl.getValue();
	    	            	}else if (rlabelName.equals("SEND")){
	    	            		end=(Integer)rl.getValue();
	    	            	}
	        			}
	        			
	        			String tokenHashCode=start.toString()+":"+end.toString();
	        			
	        			if (hashOfTokensWithIdenticalTextualDSReferences.get(tokenHashCode)==null){
	            			List<SToken> listSTokens = new ArrayList<SToken>();
	            			listSTokens.add(token);
	            			hashOfTokensWithIdenticalTextualDSReferences.put(tokenHashCode, listSTokens);
	            		}else{
	            			List<SToken> listSTokens = hashOfTokensWithIdenticalTextualDSReferences.get(tokenHashCode);
	            			listSTokens.add(token);
	            			hashOfTokensWithIdenticalTextualDSReferences.put(tokenHashCode, listSTokens);
	            		} 
	        		}
	        	}
            
			}
			return;
		}
		
		/**
		 * Creates spans over the tokens contained by the nodes registered in the lists of the hashmap (nodes with identical tagcodes).
		 * 
		 * @param  hashmapIdenticalTagcodeSTokens  hashmap containing lists of tokens pointing to the same text fragments.
		 * 				key: a string containing the tagcode
		 * 				value: list of nodes having this tagcode
		 * @param  docGraph  document graph of the document
		 */
		private void createSpansForIdenticalTagcodes(HashMap<String, List <SNode> > hashmapIdenticalTagcodeSTokens, SDocumentGraph docGraph){
			//Loop through the HashMap containing nodes with identical tagcodes
        	Iterator it = hashmapIdenticalTagcodeSTokens.entrySet().iterator();
        	while (it.hasNext()) {
        		Map.Entry pair = (Map.Entry)it.next();
        		List<SNode> listSNode = (List<SNode>)pair.getValue();      		
        		SNode mainNode = listSNode.get(0);

        		//Create a list of tokens contained by nodes with identical tagcodes
        		List<SToken> overlappingTokens= new ArrayList<>();        			            
        		for (SNode node : listSNode){        			
        			addTokensToList (node, overlappingTokens);
        		}

        		//create span overlaping a set of tokens
        		if (overlappingTokens.size() > 0 ){
        			createNewSpan(docGraph, mainNode, overlappingTokens);
        		}    		
        	}
        	return;
		}
		
		
		/**
		 * Fills the hashmap with nodes having identical tagcodes (and also nodes with unique tagcodes).
		 * 
		 * @param  hashmapIdenticalTagcodeSTokens  hashmap that will contain lists of tokens pointing to the same text fragments.
		 * 				key: a string containing the tagcode
		 * 				value: list of nodes having this tagcode
		 * @param	sNodes	list of nodes among which will be classified by tagcodes and registered into the hashmap
		 */
		private void fillHashMapOfNodesWithIdenticalTagcodes (HashMap<String, List <SNode> > hashmapIdenticalTagcodeSTokens, List <SNode> sNodes){
			for (SNode s: sNodes){
				
				Collection <Label> labels = s.getLabels(); 
				for (Label l: labels){
					
					String labelValue= (String)l.getValue();	            	
	            	String labelName= (String)l.getName();
	            	
	            	if (labelName==annotationsUnifyingAttribute){
	            		if (hashmapIdenticalTagcodeSTokens.get(labelValue)==null){
	            			List<SNode> listSNodes = new ArrayList<SNode>();
	            			listSNodes.add(s);
	            			hashmapIdenticalTagcodeSTokens.put(labelValue, listSNodes);
	            		}else{
	            			List<SNode> listSNodes = hashmapIdenticalTagcodeSTokens.get(labelValue);
	            			listSNodes.add(s);
	            			hashmapIdenticalTagcodeSTokens.put(labelValue, listSNodes);
	            		}
	            	}
				}
            }
			return;
		}
				

		/** A map storing frequencies of annotations of processed documents. */
		private Map<String, Integer> frequencies = new Hashtable<String, Integer>();

		/**
		 * This method is called for each node in document-structure, as long as
		 * {@link #checkConstraint(GRAPH_TRAVERSE_TYPE, String, SRelation, SNode, long)}
		 * returns true for this node. <br/>
		 * In our dummy implementation it just collects frequencies of
		 * annotations.
		 */
		@Override
		public void nodeReached(GRAPH_TRAVERSE_TYPE traversalType, String traversalId, SNode currNode, SRelation sRelation, SNode fromNode, long order) {
			if (currNode.getAnnotations().size() != 0) {
				// step through all annotations to collect them in frequencies
				// table
				for (SAnnotation annotation : currNode.getAnnotations()) {
					Integer frequence = frequencies.get(annotation.getName());
					// if annotation hasn't been seen yet, create entry in
					// frequencies set frequency to 0
					if (frequence == null) {
						frequence = 0;
					}
					frequence++;
					frequencies.put(annotation.getName(), frequence);
				}
			}
		}

		/**
		 * This method is called on the way back, in depth first mode it is
		 * called for a node after all the nodes belonging to its subtree have
		 * been visited. <br/>
		 * In our dummy implementation, this method is not used.
		 */
		@Override
		public void nodeLeft(GRAPH_TRAVERSE_TYPE traversalType, String traversalId, SNode currNode, SRelation edge, SNode fromNode, long order) {
		}

		/**
		 * With this method you can decide if a node is supposed to be visited
		 * by methods
		 * {@link #nodeReached(GRAPH_TRAVERSE_TYPE, String, SNode, SRelation, SNode, long)}
		 * and
		 * {@link #nodeLeft(GRAPH_TRAVERSE_TYPE, String, SNode, SRelation, SNode, long)}
		 * . In our dummy implementation for instance we do not need to visit
		 * the nodes {@link STextualDS}.
		 */
		@Override
		public boolean checkConstraint(GRAPH_TRAVERSE_TYPE traversalType, String traversalId, SRelation edge, SNode currNode, long order) {
			if (currNode instanceof STextualDS) {
				return (false);
			} else {
				return (true);
			}
		}
	}

	// =================================================== optional
	// ===================================================
	/**
	 * <strong>OVERRIDE THIS METHOD FOR CUSTOMIZATION</strong> <br/>
	 * This method is called by the pepper framework after initializing this
	 * object and directly before start processing. Initializing means setting
	 * properties {@link PepperModuleProperties}, setting temporary files,
	 * resources etc. . returns false or throws an exception in case of
	 * {@link PepperModule} instance is not ready for any reason.
	 * 
	 * @return false, {@link PepperModule} instance is not ready for any reason,
	 *         true, else.
	 */
	@Override
	public boolean isReadyToStart() throws PepperModuleNotReadyException {
		// TODO make some initializations if necessary
		return (super.isReadyToStart());
	}
}
