
package rtldr;

public interface JExtensionStateListener<R extends RExtensionBase> {
	public void onExtensionLoaded(R ext);
	public void onExtensionUnloaded(R ext);
}
