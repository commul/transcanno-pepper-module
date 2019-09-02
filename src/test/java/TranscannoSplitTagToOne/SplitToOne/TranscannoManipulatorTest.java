package TranscannoSplitTagToOne.SplitToOne;

import TranscannoSplitTagToOne.SplitToOne.TranscannoManipulator;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.List;
import java.io.File;
import java.util.ArrayList;
import java.util.Collection;

import org.corpus_tools.pepper.common.CorpusDesc;
import org.corpus_tools.pepper.core.SelfTestDesc;
import org.corpus_tools.pepper.impl.PepperMapperImpl;
import org.corpus_tools.pepper.testFramework.PepperManipulatorTest;
import org.corpus_tools.salt.SALT_TYPE;
import org.corpus_tools.salt.SaltFactory;
import org.corpus_tools.salt.common.SCorpus;
import org.corpus_tools.salt.common.SCorpusGraph;
import org.corpus_tools.salt.common.SDocument;
import org.corpus_tools.salt.common.SDocumentGraph;
import org.corpus_tools.salt.common.SDominanceRelation;
import org.corpus_tools.salt.common.SSpan;
import org.corpus_tools.salt.common.SStructure;
import org.corpus_tools.salt.common.SStructuredNode;
import org.corpus_tools.salt.common.STextualDS;
import org.corpus_tools.salt.core.SAnnotation;
import org.corpus_tools.salt.core.SNode;
import org.corpus_tools.salt.core.SRelation;
import org.corpus_tools.salt.graph.Label;
import org.corpus_tools.salt.samples.SampleGenerator;
import org.eclipse.emf.common.util.URI;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * JUnit test suite for testing the {@link TranscannoManipulator} class.
 * 
 * @author NadezdaOkinina
 */
public class TranscannoManipulatorTest extends PepperManipulatorTest {
	/**
	 * This method is called by the JUnit environment each time before a test
	 * case starts. So each time a method annotated with @Test is called. This
	 * enables, that each method could run in its own environment being not
	 * influenced by before or after running test cases.
	 */
	@Before
	public void setUp() {
		setFixture(new TranscannoManipulator());
				
	}
	
	
	/**
	 * Creates a corpus containing 1 document and returns its document graph.
	 */
	private SDocumentGraph fillCorpusGraph (SCorpusGraph graph1){
		SCorpus c1_test = SaltFactory.createSCorpus();

		SCorpus c2 = SaltFactory.createSCorpus();
		SDocument d1 = SaltFactory.createSDocument();
		d1.setDocumentGraph(SaltFactory.createSDocumentGraph());

		c1_test.setName("c1");
		c2.setName("c2");
		d1.setName("d1");

		graph1.addNode(c1_test);
		graph1.addSubCorpus(c1_test, c2);
		graph1.addDocument(c2, d1);	

		SDocumentGraph dGraph = d1.createDocumentGraph();
		
		return dGraph;
	}
	
	/**
	 * Gets the document graph after the manipulator run.
	 */
	private SDocumentGraph getResultingDocumentGraph (TranscannoManipulator fixture2){
		SCorpusGraph cGraph2 = fixture2.getSaltProject().getCorpusGraphs().get(1);
		SDocument doc2 = cGraph2.getDocuments().get(0);
		SDocumentGraph docGraph2 = doc2.getDocumentGraph();
		return docGraph2;
	}
	
	/**
	 * Creates a "hey" structure (annotation) and adds it to the document graph.
	 */
	private SStructure createHeyStructure (SDocumentGraph dGraph, SNode token2, SNode token3){
		//Create a hey structure
		ArrayList <SStructuredNode> tokenList1= new ArrayList <SStructuredNode> ();
		tokenList1.add((SStructuredNode)token2);
		tokenList1.add((SStructuredNode)token3);		
		SStructure structure1 = dGraph.createStructure(tokenList1);
		structure1.createAnnotation(null, "mode", "0");
		structure1.createAnnotation(null, "tagcode", "1524141024154");
		structure1.createAnnotation(null, "class", "medium-hey_id35");
		
		return structure1;
	}
	
	/**
	 * Checks that if there are no annotations there are no spans.
	 */
	/*
	@Test
	public void test_checkNoAnnotationsNoSpansTest() {		
		SCorpusGraph graph1 = SaltFactory.createSCorpusGraph();
		SDocumentGraph dGraph = fillCorpusGraph (graph1);
		//Create a textual DS
		STextualDS textualDS = dGraph.createTextualDS("here text tag");		
		//Tokenize the text: creates 3 tokens
		dGraph.tokenize();
		SNode token1=dGraph.getTokens().get(0);
		SNode token2=dGraph.getTokens().get(1);
		SNode token3=dGraph.getTokens().get(2);	
		
		TranscannoManipulator fixture2 = (TranscannoManipulator) getFixture();		 
		fixture2.getSaltProject().addCorpusGraph(graph1);
		 
		//run your Pepper module
		start();
		
		//Get the resulting document graph
		SDocumentGraph docGraph2 = getResultingDocumentGraph (fixture2);
		//Checks that there are no spans
		assertEquals(0, docGraph2.getSpans().size());				
	}
	*/

	/**
	 * Checks that if there are no dominance relations in the initial document, there are no dominance relations in the resulting document.
	 */
	/*
	@Test
	public void test_checkNoRelationsTest() {		
		SCorpusGraph graph1 = SaltFactory.createSCorpusGraph();
		SDocumentGraph dGraph = fillCorpusGraph (graph1);
		//Create a textual DS
		STextualDS textualDS = dGraph.createTextualDS("here text tag");		
		//Tokenize the text: creates 3 tokens
		dGraph.tokenize();
		SNode token1=dGraph.getTokens().get(0);
		SNode token2=dGraph.getTokens().get(1);
		SNode token3=dGraph.getTokens().get(2);	
		
		TranscannoManipulator fixture2 = (TranscannoManipulator) getFixture();		 
		fixture2.getSaltProject().addCorpusGraph(graph1);
		 
		//run your Pepper module
		start();
		
		//Get the resulting document graph
		SDocumentGraph docGraph2 = getResultingDocumentGraph (fixture2);
		//Checks that there are no dominance relations
		assertEquals(0, docGraph2.getDominanceRelations().size());				
	}
	*/
	
	/**
	 * Checks if 1 annotation has been transformed into 1 span.
	 */
	/*
	@Test
	public void test_checkTransformsOneAnnotationIntoOneSpanTest() {		
		SCorpusGraph graph1 = SaltFactory.createSCorpusGraph();
		SDocumentGraph dGraph = fillCorpusGraph (graph1);
		
		//Create a textual DS
		STextualDS textualDS = dGraph.createTextualDS("here text tag");		
		//Tokenize the text: creates 3 tokens
		dGraph.tokenize();
		SNode token1=dGraph.getTokens().get(0);
		SNode token2=dGraph.getTokens().get(1);
		SNode token3=dGraph.getTokens().get(2);
		
		//Create a hey structure
		SStructure structure1 = createHeyStructure (dGraph, token2, token3);
				
		TranscannoManipulator fixture2 = (TranscannoManipulator) getFixture();
		 
		fixture2.getSaltProject().addCorpusGraph(graph1);
		 
		//run your Pepper module
		start();
		
		//Get the resulting document graph
		SDocumentGraph docGraph2 = getResultingDocumentGraph (fixture2);
		//Checks that there is one span
		assertEquals(1, docGraph2.getSpans().size());
	}
	*/
	
	/**
	 * Checks if the span has inherited attributes from the initial text's Transc&Anno annotation.
	 */
	/*
	@Test
	public void test_checkSpanGotAttributesFromTranscAnnoAnnotationTest() {		
		SCorpusGraph graph1 = SaltFactory.createSCorpusGraph();
		SDocumentGraph dGraph = fillCorpusGraph (graph1);
		
		//Create a textual DS
		STextualDS textualDS = dGraph.createTextualDS("here text tag");		
		//Tokenize the text: creates 3 tokens
		dGraph.tokenize();
		SNode token1=dGraph.getTokens().get(0);
		SNode token2=dGraph.getTokens().get(1);
		SNode token3=dGraph.getTokens().get(2);
		
		//Create a hey structure
		SStructure structure1 = createHeyStructure (dGraph, token2, token3);
		
		TranscannoManipulator fixture2 = (TranscannoManipulator) getFixture();
		 
		fixture2.getSaltProject().addCorpusGraph(graph1);
		 
		//run your Pepper module
		start();
		
		//Get the resulting document graph
		SDocumentGraph docGraph2 = getResultingDocumentGraph (fixture2);
		//Checks that there is one span
		assertEquals(1, docGraph2.getSpans().size());
		
		SSpan span = docGraph2.getSpans().get(0);
		/*
		assertEquals("0", span.getLabel("mode").getValue());
		assertEquals("1524141024154", span.getLabel("tagcode").getValue());
		assertEquals("medium-hey_id35", span.getLabel("class").getValue());
		*/
	/*
		Assert.assertNull(span.getLabel("mode"));
		Assert.assertNull(span.getLabel("tagcode"));
		Assert.assertNull(span.getLabel("class"));
	}
	*/
	
	/**
	 * Checks if the span has inherited attributes from the initial text's non-Transc&Anno annotation.
	 */
	/*
	@Test
	public void test_checkSpanGotAttributesFromAnnotationTest() {		
		SCorpusGraph graph1 = SaltFactory.createSCorpusGraph();
		SDocumentGraph dGraph = fillCorpusGraph (graph1);
		
		//Create a textual DS
		STextualDS textualDS = dGraph.createTextualDS("here text tag");		
		//Tokenize the text: creates 3 tokens
		dGraph.tokenize();
		SNode token1=dGraph.getTokens().get(0);
		SNode token2=dGraph.getTokens().get(1);
		SNode token3=dGraph.getTokens().get(2);
		
		//Create a structure overlapping 2 tokens
		ArrayList <SStructuredNode> tokenList1= new ArrayList <SStructuredNode> ();
		tokenList1.add((SStructuredNode)token2);
		tokenList1.add((SStructuredNode)token3);		
		SStructure structure1 = dGraph.createStructure(tokenList1);
		structure1.createAnnotation(null, "attr1", "22");
		structure1.createAnnotation(null, "attr2", "kria");
		structure1.createAnnotation(null, "attr3", "7zu7");
		
		TranscannoManipulator fixture2 = (TranscannoManipulator) getFixture();
		 
		fixture2.getSaltProject().addCorpusGraph(graph1);
		 
		//run your Pepper module
		start();
		
		//Get the resulting document graph
		SDocumentGraph docGraph2 = getResultingDocumentGraph (fixture2);
		//Checks that there is one span
		assertEquals(1, docGraph2.getSpans().size());
		
		SSpan span = docGraph2.getSpans().get(0);
		
		assertEquals("22", span.getLabel("attr1").getValue());
		assertEquals("kria", span.getLabel("attr2").getValue());
		assertEquals("7zu7", span.getLabel("attr3").getValue());
	}
	*/
	
	/**
	 * Checks that the manipulator has deleted 2 dominance relations.
	 */
	/*
	@Test
	public void test_checkDeletedTwoDominanceRelationsTest() {		
		SCorpusGraph graph1 = SaltFactory.createSCorpusGraph();
		SDocumentGraph dGraph = fillCorpusGraph (graph1);
		
		//Create a textual DS
		STextualDS textualDS = dGraph.createTextualDS("here text tag");		
		//Tokenize the text: creates 3 tokens
		dGraph.tokenize();
		SNode token1=dGraph.getTokens().get(0);
		SNode token2=dGraph.getTokens().get(1);
		SNode token3=dGraph.getTokens().get(2);
		
		//Create a hey structure
		SStructure structure1 = createHeyStructure (dGraph, token2, token3);		
		
		TranscannoManipulator fixture2 = (TranscannoManipulator) getFixture();
		 
		fixture2.getSaltProject().addCorpusGraph(graph1);
		 
		//run your Pepper module
		start();
		
		//Get the resulting document graph
		SDocumentGraph docGraph2 = getResultingDocumentGraph (fixture2);
		
		//Checks that there are no dominance relations
		assertEquals(0, docGraph2.getDominanceRelations().size());	
	}
	*/
	
	/**
	 * Checks if 2 overlapping Transc&Anno annotations have been transformed into 2 spans.
	 */
	/*
	@Test
	public void test_checkTransformsTwoOverlappingTranscAnnoAnnotationsIntoTwoSpansTest() {		
		SCorpusGraph graph1 = SaltFactory.createSCorpusGraph();
		SDocumentGraph dGraph = fillCorpusGraph (graph1);
		
		//Create a textual DS
		STextualDS textualDS = dGraph.createTextualDS("here text tag");		
		//Tokenize the text: creates 3 tokens
		dGraph.tokenize();
		SNode token1=dGraph.getTokens().get(0);
		SNode token2=dGraph.getTokens().get(1);
		SNode token3=dGraph.getTokens().get(2);
		
		//Create the first infinitive annotation
		token1.createAnnotation(null, "mode", "0");
		token1.createAnnotation(null, "tagcode", "1524141045536");
		token1.createAnnotation(null, "class", "medium-infinitive_id1");
		
		//Create the second infinitive annotation
		token2.createAnnotation(null, "mode", "0");
		token2.createAnnotation(null, "tagcode", "1524141045536");
		token2.createAnnotation(null, "class", "medium-infinitive_id1");
		
		//Create a hey structure
		SStructure structure1 = createHeyStructure (dGraph, token2, token3);		
		
		TranscannoManipulator fixture2 = (TranscannoManipulator) getFixture();
		 
		fixture2.getSaltProject().addCorpusGraph(graph1);
		 
		//run your Pepper module
		start();
		
		//Get the resulting document graph	
		SDocumentGraph docGraph2 = getResultingDocumentGraph (fixture2);

		//Checks that there are 2 spans
		assertEquals(2, docGraph2.getSpans().size());		
	}
	*/
	
	/**
	 * Checks if 2 overlapping non-Transc&Anno annotations have been transformed into 2 spans.
	 */
	/*
	@Test
	public void test_checkTransformsTwoOverlappingNonTranscAnnoAnnotationsIntoThreeSpansTest() {
		SCorpusGraph graph1 = SaltFactory.createSCorpusGraph();
		SDocumentGraph dGraph = fillCorpusGraph (graph1);
		
		//Create a textual DS
		STextualDS textualDS = dGraph.createTextualDS("here text tag");		
		//Tokenize the text: creates 3 tokens
		dGraph.tokenize();
		SNode token1=dGraph.getTokens().get(0);
		SNode token2=dGraph.getTokens().get(1);
		SNode token3=dGraph.getTokens().get(2);
				
		//Create a structure overlapping 2 tokens: the second and the third
		ArrayList <SStructuredNode> tokenList1= new ArrayList <SStructuredNode> ();
		tokenList1.add((SStructuredNode)token2);
		tokenList1.add((SStructuredNode)token3);		
		SStructure structure1 = dGraph.createStructure(tokenList1);
		structure1.createAnnotation(null, "attr1", "22");
		structure1.createAnnotation(null, "attr2", "kria");
		structure1.createAnnotation(null, "attr3", "7zu7");
		
		//Create a structure overlapping structure1 and the first token
		ArrayList <SStructuredNode> structureList= new ArrayList <SStructuredNode> ();
		structureList.add((SStructuredNode)token1);
		structureList.add((SStructuredNode)structure1);		
		SStructure structure2 = dGraph.createStructure(structureList);
		structure2.createAnnotation(null, "bu", "0");
		structure2.createAnnotation(null, "mu", "15");
		structure2.createAnnotation(null, "class", "cow");
		
		TranscannoManipulator fixture2 = (TranscannoManipulator) getFixture();
		 
		fixture2.getSaltProject().addCorpusGraph(graph1);
		 
		//run your Pepper module
		start();
		
		//Get the resulting document graph	
		SDocumentGraph docGraph2 = getResultingDocumentGraph (fixture2);
		List <SSpan> spansList = docGraph2.getSpans();
		
		//Checks that there are 3 spans
		assertEquals(2, docGraph2.getSpans().size());		
	}
	*/
	
	/**
	 * Checks if flattens a hierarchical structure with 2 levels of dominance relations.
	 */
	/*
	@Test
	public void test_flattensStructureTest() {		
		SCorpusGraph graph1 = SaltFactory.createSCorpusGraph();
		SDocumentGraph dGraph = fillCorpusGraph (graph1);
		
		//Create a textual DS
		STextualDS textualDS = dGraph.createTextualDS("here text tag");		
		//Tokenize the text: creates 3 tokens
		dGraph.tokenize();
		SNode token1=dGraph.getTokens().get(0);
		SNode token2=dGraph.getTokens().get(1);
		SNode token3=dGraph.getTokens().get(2);
		
		//Create the first infinitive annotation
		token1.createAnnotation(null, "mode", "0");
		token1.createAnnotation(null, "tagcode", "1524141045536");
		token1.createAnnotation(null, "class", "medium-infinitive_id1");
		
		//Create the second infinitive annotation
		token2.createAnnotation(null, "mode", "0");
		token2.createAnnotation(null, "tagcode", "1524141045536");
		token2.createAnnotation(null, "class", "medium-infinitive_id1");
		
		//Create a hey structure
		SStructure structure1 = createHeyStructure (dGraph, token2, token3);
		
		//Create a structure overlapping hey and the first token
		ArrayList <SStructuredNode> structureList= new ArrayList <SStructuredNode> ();
		structureList.add((SStructuredNode)token1);
		structureList.add((SStructuredNode)structure1);		
		SStructure structure2 = dGraph.createStructure(structureList);
		structure2.createAnnotation(null, "mode", "0");
		structure2.createAnnotation(null, "tagcode", "1929191824199");
		structure2.createAnnotation(null, "class", "medium-noun_id22");
		
		TranscannoManipulator fixture2 = (TranscannoManipulator) getFixture();
		 
		fixture2.getSaltProject().addCorpusGraph(graph1);
		 
		//run your Pepper module
		start();
		
		//Get the resulting document graph	
		SDocumentGraph docGraph2 = getResultingDocumentGraph (fixture2);
		
		//Checks that there are 2 spans
		assertEquals(3, docGraph2.getSpans().size());
		assertEquals(0, docGraph2.getDominanceRelations().size());		
	}
	*/
	
	/**
	 * Checks if the number of tokens remains the same.
	 */
	/*
	@Test
	public void test_checkNumberTokensRemainsSameTest() {		
		SCorpusGraph graph1 = SaltFactory.createSCorpusGraph();
		SDocumentGraph dGraph = fillCorpusGraph (graph1);
		
		//Create a textual DS
		STextualDS textualDS = dGraph.createTextualDS("here text tag");		
		//Tokenize the text: creates 3 tokens
		dGraph.tokenize();
		SNode token1=dGraph.getTokens().get(0);
		SNode token2=dGraph.getTokens().get(1);
		SNode token3=dGraph.getTokens().get(2);
		
		//Create the first infinitive annotation
		token1.createAnnotation(null, "mode", "0");
		token1.createAnnotation(null, "tagcode", "1524141045536");
		token1.createAnnotation(null, "class", "medium-infinitive_id1");
		
		//Create the second infinitive annotation
		token2.createAnnotation(null, "mode", "0");
		token2.createAnnotation(null, "tagcode", "1524141045536");
		token2.createAnnotation(null, "class", "medium-infinitive_id1");
		
		//Create a hey structure
		SStructure structure1 = createHeyStructure (dGraph, token2, token3);
		
		//Create a structure overlapping hey and the first token
		ArrayList <SStructuredNode> structureList= new ArrayList <SStructuredNode> ();
		structureList.add((SStructuredNode)token1);
		structureList.add((SStructuredNode)structure1);		
		SStructure structure2 = dGraph.createStructure(structureList);
		structure2.createAnnotation(null, "mode", "0");
		structure2.createAnnotation(null, "tagcode", "1929191824199");
		structure2.createAnnotation(null, "class", "medium-noun_id22");
		
		TranscannoManipulator fixture2 = (TranscannoManipulator) getFixture();
		 
		fixture2.getSaltProject().addCorpusGraph(graph1);
		 
		//run your Pepper module
		start();
		
		//Get the resulting document graph	
		SDocumentGraph docGraph2 = getResultingDocumentGraph(fixture2);
		
		//Checks that there are 2 spans
		assertEquals(3, docGraph2.getTokens().size());		
	}
	*/
}
