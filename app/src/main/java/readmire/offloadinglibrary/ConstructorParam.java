package readmire.offloadinglibrary;

import java.io.Serializable;

/**
 *
 *    @yasemin
 **/
public class ConstructorParam implements Serializable {

	 private Class<?>[] constructorArgTypes = new Class[0];
	 private Object[] constructorArgValues = new Object[0];	 

	 public void setConstructorArgValues(Object... constructorArgValues) {
	        this.constructorArgValues = constructorArgValues;
	        
	 }
	 public void setConstructorArgTypes(Class<?>... constructorArgTypes) {
	        this.constructorArgTypes = constructorArgTypes;
	        
	 }

	public Class<?>[] getConstructorArgTypes() {
			return constructorArgTypes;
	}

	public Object[] getConstructorArgValues() {
			return constructorArgValues;
	}
	 
	    
}
