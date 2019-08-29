package readmire.offloadinglibrary;

/**
 *
 *    @yasemin
 **/
public class ControlParameters {
	public static boolean offload=false;
	public String compression;
	public String filetype;
	
	public static boolean isOffload() {
		return offload;
	}
	public static void setOffload(boolean off) {
		offload = off;
	}
	public String getCompression() {
		return compression;
	}
	public void setCompression(String compression) {
		this.compression = compression;
	}
	public String getFiletype() {
		return filetype;
	}
	public void setFiletype(String filetype) {
		this.filetype = filetype;
	}

}
