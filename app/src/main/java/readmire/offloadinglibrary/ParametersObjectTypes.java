package readmire.offloadinglibrary;

import java.io.Serializable;
/**
 *
 *    @yasemin
 **/

public class ParametersObjectTypes implements Serializable{

	Long id;
	String type;
	boolean local;
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
	public boolean isLocal() {
		return local;
	}
	public void setLocal(boolean local) {
		this.local = local;
	}
	
	
}
