package ctrmap.creativestudio;

import ctrmap.creativestudio.ngcs.NGCS;

public class CreativeStudioChecker {

	public static boolean isCreativeStudioPresent() {
		try {
			NGCS.dummy();
			return true;
		} catch (NoClassDefFoundError ex) {
			return false;
		}
	}
}
