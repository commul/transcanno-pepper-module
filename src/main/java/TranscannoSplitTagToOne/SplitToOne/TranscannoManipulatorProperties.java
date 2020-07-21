package TranscannoSplitTagToOne.SplitToOne;

import static org.corpus_tools.pepper.modules.PepperModuleProperty.create;
import org.corpus_tools.pepper.modules.PepperModuleProperties;

class TranscannoManipulatorProperties extends PepperModuleProperties{
	private static final long serialVersionUID = 1L;
	
	public static final String PROP_ANNO_UNIFY_ATTR_ATTR="AnnotationsUnifyingAttribute";
	public final static String PROP_ANNO_UNIFY_ATTR_DESCR = "if 2 or more SSpans/STokens/SSTructure have the same value for this SAnnotation as well as for the rest of their SAnnotations, they will be merged into 1 span by TranscannoManipulator";
	private static String PROP_ANNO_UNIFY_ATTR_DV = "tagcode";
	@SuppressWarnings("rawtypes")
	public final static Class PPROP_ANNO_UNIFY_ATTR_CLASS = String.class;
	public final static Boolean PROP_ANNO_UNIFY_ATTR_REQ = false;
	
	private static String PROP_LOG_FILE_STRING = "LogFilePath";
	private static String PROP_LOG_FILE_DESCR = "A path to a file in which some statistics regarding the Sannotations discarded (when a merging happened) will be logged.";
	private static String PROP_LOG_FILE_DV = "";
	@SuppressWarnings("rawtypes")
	private static Class PROP_LOG_FILE_CLASS = String.class;
	private static Boolean PROP_LOG_FILE_REQ = false;
	
	private static String PROP_DEEP_LOG_REGEXP_STRING = "DeepLogRegExp";
	private static String PROP_DEEP_LOG_REGEXP_DESCR = "A regexp expression to identify the annotations for which the logging should be on the name and value and not on the name only.";
	private static String PROP_DEEP_LOG_REGEXP_DV = "";
	@SuppressWarnings("rawtypes")
	private static Class PROP_DEEP_LOG_REGEXP_CLASS = String.class;
	private static Boolean PROP_DEEP_LOG_REGEXP_REQ = false;
	
	private static String PROP_EXTRA_DEEP_LOG_REGEXP_STRING = "ExtraDeepLogRegExp";
	private static String PROP_EXTRA_DEEP_LOG_REGEXP_DESCR = "A regexp expression to identify the annotations for which the logging should be on the document id, name and value.";
	private static String PROP_EXTRA_DEEP_LOG_REGEXP_DV = "";
	@SuppressWarnings("rawtypes")
	private static Class PROP_EXTRA_DEEP_LOG_REGEXP_CLASS = String.class;
	private static Boolean PROP_EXTRA_DEEP_LOG_REGEXP_REQ = false;
	
	private static String PROP_MIN_TEXT_SIZE_STRING = "MinTextSize";
	private static String PROP_MIN_TEXT_SIZE_DESCR = "A mininum size in character for a document. An error is launched for the documents who don't comply. This is meant to be used to detect the empty-ish export from transcanno ";
	private static Integer PROP_MIN_TEXT_SIZE_DV = 0;
	@SuppressWarnings("rawtypes")
	private static Class PROP_MIN_TEXT_SIZE_CLASS = Integer.class;
	private static Boolean PROP_MIN_TEXT_SIZE_REQ = false;
	
	
	@SuppressWarnings("unchecked")
	public TranscannoManipulatorProperties()
	{
		this.addProperty(create().withName(TranscannoManipulatorProperties.PROP_ANNO_UNIFY_ATTR_ATTR)
				.withType(TranscannoManipulatorProperties.PPROP_ANNO_UNIFY_ATTR_CLASS)				
				.withDescription(TranscannoManipulatorProperties.PROP_ANNO_UNIFY_ATTR_DESCR)
				.withDefaultValue(TranscannoManipulatorProperties.PROP_ANNO_UNIFY_ATTR_DV)
				.isRequired(TranscannoManipulatorProperties.PROP_ANNO_UNIFY_ATTR_REQ).build());
		
		this.addProperty(create().withName(TranscannoManipulatorProperties.PROP_LOG_FILE_STRING)
				.withType(TranscannoManipulatorProperties.PROP_LOG_FILE_CLASS)				
				.withDescription(TranscannoManipulatorProperties.PROP_LOG_FILE_DESCR)
				.withDefaultValue(TranscannoManipulatorProperties.PROP_LOG_FILE_DV)
				.isRequired(TranscannoManipulatorProperties.PROP_LOG_FILE_REQ).build());
		
		this.addProperty(create().withName(TranscannoManipulatorProperties.PROP_DEEP_LOG_REGEXP_STRING)
				.withType(TranscannoManipulatorProperties.PROP_DEEP_LOG_REGEXP_CLASS)				
				.withDescription(TranscannoManipulatorProperties.PROP_DEEP_LOG_REGEXP_DESCR)
				.withDefaultValue(TranscannoManipulatorProperties.PROP_DEEP_LOG_REGEXP_DV)
				.isRequired(TranscannoManipulatorProperties.PROP_DEEP_LOG_REGEXP_REQ).build());
		
		this.addProperty(create().withName(TranscannoManipulatorProperties.PROP_EXTRA_DEEP_LOG_REGEXP_STRING)
				.withType(TranscannoManipulatorProperties.PROP_EXTRA_DEEP_LOG_REGEXP_CLASS)				
				.withDescription(TranscannoManipulatorProperties.PROP_EXTRA_DEEP_LOG_REGEXP_DESCR)
				.withDefaultValue(TranscannoManipulatorProperties.PROP_EXTRA_DEEP_LOG_REGEXP_DV)
				.isRequired(TranscannoManipulatorProperties.PROP_EXTRA_DEEP_LOG_REGEXP_REQ).build());
		
		this.addProperty(create().withName(TranscannoManipulatorProperties.PROP_MIN_TEXT_SIZE_STRING)
				.withType(TranscannoManipulatorProperties.PROP_MIN_TEXT_SIZE_CLASS)				
				.withDescription(TranscannoManipulatorProperties.PROP_MIN_TEXT_SIZE_DESCR)
				.withDefaultValue(TranscannoManipulatorProperties.PROP_MIN_TEXT_SIZE_DV)
				.isRequired(TranscannoManipulatorProperties.PROP_MIN_TEXT_SIZE_REQ).build());
	}
	
	/**
	 * Returns the value of the property AnnotationsUnifyingAttribute.
	 * 
	 * @return String the name of the attribute that will allow to merge 2 or more structures, if they have the same value of this attribute
	 */
	public String getAnnotationsUnifyingAttribute(){
		return((String)this.getProperty(TranscannoManipulatorProperties.PROP_ANNO_UNIFY_ATTR_ATTR).getValue());
	}
	
	public String getLogFilePath(){
		return ((String) this.getProperty(TranscannoManipulatorProperties.PROP_LOG_FILE_STRING).getValue());
	}
	
	public String getDeepLogRegExp(){
		return ((String) this.getProperty(TranscannoManipulatorProperties.PROP_DEEP_LOG_REGEXP_STRING).getValue());
	}
	
	public String getExtraDeepLogRegExp(){
		return ((String) this.getProperty(TranscannoManipulatorProperties.PROP_EXTRA_DEEP_LOG_REGEXP_STRING).getValue());
	}
	
	
	public int getMinTextSize(){
		return ((Integer) this.getProperty(TranscannoManipulatorProperties.PROP_MIN_TEXT_SIZE_STRING).getValue());
	}
	
}
