package ctrmap;

import ctrmap.renderer.backends.houston.HoustonResources;
import xstandard.res.ResourceAccess;
import java.io.File;
import xstandard.res.ResourceAccessor;

/**
 *
 */
public class CTRMapResources {
	
	public static final ResourceAccessor ACCESSOR = new ResourceAccessor("ctrmap/resources");
	
	public static void load(){
		ResourceAccess.loadResourceTable(CTRMapResources.class.getClassLoader(), "ctrmap/resources/res_ctrmap.tbl");
	}

	public static void main(String[] args) {
		ResourceAccess.buildResourceTable(new File("src/ctrmap/resources"), "ctrmap/resources", "res_ctrmap.tbl");
		HoustonResources.main(args);
	}
}
