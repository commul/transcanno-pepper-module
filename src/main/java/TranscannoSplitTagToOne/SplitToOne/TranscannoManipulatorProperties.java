package TranscannoSplitTagToOne.SplitToOne;

import java.io.File;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.corpus_tools.pepper.modules.PepperModuleProperties;
import org.corpus_tools.pepper.modules.PepperModuleProperty;
import org.corpus_tools.pepper.modules.exceptions.PepperModuleException;

public class TranscannoManipulatorProperties extends PepperModuleProperties{
	/**
	 * Add the property AnnotationsUnifyingAttribute to the list of TranscannoManipulator properties.
	 */
	public TranscannoManipulatorProperties(){  
		this.addProperty(new PepperModuleProperty<String>("AnnotationsUnifyingAttribute", String.class, "if 2 or more annotations have the same value of this attribute, they will be unified into 1 span by TranscannoManipulator", true));
	}
	
	/**
	 * Returns the value of the property AnnotationsUnifyingAttribute.
	 * 
	 * @return String the name of the attribute that will allow to merge 2 or more structures, if they have the same value of this attribute
	 */
	public String getAnnotationsUnifyingAttribute(){
		return((String)this.getProperty("AnnotationsUnifyingAttribute").getValue());
	}

	/**
	 * Checks if the name of the AnnotationsUnifyingAttribute entered by the user is a valid XML attribute name.
	 */
	public boolean checkProperty(PepperModuleProperty<?> prop){
		//calls the check of constraints in parent,
		//for instance if a required value is set
		super.checkProperty(prop);
		if ("AnnotationsUnifyingAttribute".equals(prop.getName())){
			String propertyValue = (String)prop.getValue();

			if (!attributeNameOK(propertyValue)){
				throw new PepperModuleException("An XML attribute of this form cannot exist.");
			}
		
		}
	return(true);
	}
	
	/**
	 * Checks if the name of the AnnotationsUnifyingAttribute entered by the user is a valid XML attribute name.
	 */
	private static boolean attributeNameOK (String s){
        Pattern pattern1 = Pattern.compile("[<>&]");
        Matcher matcher1 = pattern1.matcher(s);
        
        if(Character.isDigit(s.charAt(0))){
        	return false;
        }else if (matcher1.find()){
            return false;
        }
        
        return true;
    }
}
