package ctrmap.renderer.backends.houston;

import xstandard.res.ResourceAccess;
import java.io.File;
import xstandard.res.ResourceAccessor;

public class HoustonResources {
	
	public static final ResourceAccessor ACCESSOR = new ResourceAccessor("ctrmap/resources");
	
	public static void load(){
		ResourceAccess.loadResourceTable(HoustonResources.class.getClassLoader(), "ctrmap/resources/houston/res_houston.tbl");
	}

	public static void main(String[] args) {
		ResourceAccess.buildResourceTable(new File("src/ctrmap/resources/houston"), "ctrmap/resources", "houston", "res_houston.tbl");
	}
}
