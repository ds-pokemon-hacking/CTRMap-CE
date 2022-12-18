package ctrmap.renderer.util;

import ctrmap.renderer.backends.base.AbstractBackend;
import ctrmap.renderer.scenegraph.SceneAnimationCallback;
import java.util.function.Supplier;

public class AspectRatioSyncCallback implements SceneAnimationCallback {

	private final Supplier<AbstractBackend> backendFn;

	public AspectRatioSyncCallback(Supplier<AbstractBackend> backendFn) {
		this.backendFn = backendFn;
	}

	@Override
	public void run(float frameAdvance) {
		AbstractBackend bknd = backendFn.get();
		bknd.getScene().setAllCameraAspectRatio(bknd.getViewportInfo().getAspectRatio());
	}

	public static class Default extends AspectRatioSyncCallback {

		public Default(AbstractBackend backend) {
			super(() -> {
				return backend;
			});
		}
	}
}
