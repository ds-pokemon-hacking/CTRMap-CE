package ctrmap.creativestudio;

import ctrmap.Launc;
import ctrmap.creativestudio.ngcs.NGCS;

public class NGCSStarter implements Launc.SubprocessStarter {

	public static final NGCSStarter INSTANCE = new NGCSStarter();

	private NGCSStarter() {

	}

	@Override
	public boolean start() {
		if (CreativeStudioChecker.isCreativeStudioPresent()) {
			NGCS.main(null);
			return true;
		} else {
			return false;
		}
	}
}
