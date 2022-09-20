
package ctrmap.creativestudio.ngcs.rtldr;

import ctrmap.creativestudio.ngcs.NGCS;

public class NGCSJulietHelper {
	public static void onCSWindowLoad(NGCS cs) {
		NGCSJulietIface.getInstance().onCSWindowLoad(cs);
	}
	
	public static void onCSWindowClose(NGCS cs) {
		NGCSJulietIface.getInstance().onCSWindowClose(cs);
	}
}
