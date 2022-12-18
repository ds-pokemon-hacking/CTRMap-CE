
package ctrmap.editor.system.juliet;

public class CTRMapPluginControl {
	public static void readyInterface(CTRMapPluginInterface iface) {
		iface.ready();
	}
	
	public static void freeInterface(CTRMapPluginInterface iface) {
		iface.free();
	}
	
	public static void onProjectLoaded(CTRMapPluginInterface iface) {
		iface.registAllUI();
	}
}
