package TranscannoSplitTagToOne.SplitToOne;

import static org.corpus_tools.pepper.modules.PepperModuleProperty.create;
import org.corpus_tools.pepper.modules.PepperModuleProperties;

class TranscannoManipulatorProperties extends PepperModuleProperties{
	private static final long serialVersionUID = 1L;
	
	public static final String PROP_ANNO_UNIFY_ATTR_ATTR="AnnotationsUnifyingAttribute";
	public final static String PROP_ANNO_UNIFY_ATTR_DESCR = "if 2 or more annotations have the same value of this attribute, they will be unified into 1 span by TranscannoManipulator";
	private static String PROP_ANNO_UNIFY_ATTR_DV = "tagcode";
	@SuppressWarnings("rawtypes")
	public final static Class PPROP_ANNO_UNIFY_ATTR_CLASS = String.class;
	public final static Boolean PROP_ANNO_UNIFY_ATTR_REQ = false;
	
	@SuppressWarnings("unchecked")
	public TranscannoManipulatorProperties()
	{
		this.addProperty(create().withName(TranscannoManipulatorProperties.PROP_ANNO_UNIFY_ATTR_ATTR)
				.withType(TranscannoManipulatorProperties.PPROP_ANNO_UNIFY_ATTR_CLASS)				
				.withDescription(TranscannoManipulatorProperties.PROP_ANNO_UNIFY_ATTR_DESCR)
				.withDefaultValue(TranscannoManipulatorProperties.PROP_ANNO_UNIFY_ATTR_DV)
				.isRequired(TranscannoManipulatorProperties.PROP_ANNO_UNIFY_ATTR_REQ).build());
	}
	
	/**
	 * Returns the value of the property AnnotationsUnifyingAttribute.
	 * 
	 * @return String the name of the attribute that will allow to merge 2 or more structures, if they have the same value of this attribute
	 */
	public String getAnnotationsUnifyingAttribute(){
		return((String)this.getProperty(TranscannoManipulatorProperties.PROP_ANNO_UNIFY_ATTR_ATTR).getValue());
	}
}
