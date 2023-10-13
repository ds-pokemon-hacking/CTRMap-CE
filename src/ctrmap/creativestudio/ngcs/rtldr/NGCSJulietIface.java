package ctrmap.creativestudio.ngcs.rtldr;

import ctrmap.creativestudio.ngcs.NGCS;
import ctrmap.creativestudio.ngcs.io.CSG3DIOContentType;
import ctrmap.creativestudio.ngcs.io.IG3DFormatHandler;
import xstandard.util.ArraysEx;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JMenuItem;
import rtldr.JExtensionManager;
import rtldr.JExtensionStateListener;
import rtldr.JGarbageCollector;
import rtldr.JRTLDRCore;
import rtldr.JExtensionReceiver;
import rtldr.RExtensionBase;

public class NGCSJulietIface implements JExtensionReceiver<INGCSPlugin> {

	private static NGCSJulietIface INSTANCE;

	private JExtensionManager<NGCSJulietIface, INGCSPlugin> extensionManager;

	private JGarbageCollector<IG3DFormatHandler> formatGc = new JGarbageCollector<>();
	private JGarbageCollector<JMenuItem> menuGc = new JGarbageCollector<>();

	private NGCSIOManager ioMgr = NGCSIOManager.getInstance();
	private NGCSUIManager currentUiMgr;

	private List<NGCS> csInstances = new ArrayList<>();

	private NGCSJulietIface() {

	}

	private void registCS(NGCS cs) {
		ArraysEx.addIfNotNullOrContains(csInstances, cs);
	}

	private void unregistCS(NGCS cs) {
		csInstances.remove(cs);
	}

	public static NGCSJulietIface getInstance() {
		if (INSTANCE == null) {
			INSTANCE = new NGCSJulietIface();
			INSTANCE.extensionManager = JRTLDRCore.bindExtensionManager("NGCSPlugin", INSTANCE, INSTANCE.createExtensionListener());
		}
		return INSTANCE;
	}

	private JExtensionStateListener<INGCSPlugin> createExtensionListener() {
		return new JExtensionStateListener<INGCSPlugin>() {
			@Override
			public void onExtensionLoaded(INGCSPlugin ext) {
				//Format registration is resident
				formatGc.startListening(ext);
				ext.registerFormats(NGCSJulietIface.this);
				formatGc.stopListening();
				menuGc.startListening(ext);
				for (NGCS cs : csInstances) {
					currentUiMgr = cs.getUIManager();
					ext.registerUI(INSTANCE, cs, cs);
				}
				menuGc.stopListening();
			}

			@Override
			public void onExtensionUnloaded(INGCSPlugin ext) {
				unloadPluginCommon(ext);
			}
		};
	}

	void onCSWindowLoad(NGCS cs) {
		currentUiMgr = cs.getUIManager();
		for (INGCSPlugin plg : extensionManager.getLoadedExtensions()) {
			menuGc.startListening(plg);
			plg.registerUI(this, cs, cs);
		}
		menuGc.stopListening();
		registCS(cs);
	}

	void onCSWindowClose(NGCS cs) {
		unregistCS(cs);
	}

	private void unloadPluginCommon(RExtensionBase pluginIdentity) {
		formatGc.collect(pluginIdentity, (t) -> {
			ioMgr.unregistHandler(t);
		});
		menuGc.collect(pluginIdentity, (t) -> {
			for (NGCS cs : csInstances) {
				cs.getUIManager().removeMenuItem(t);
			}
		});
	}

	/**
	 * Registers an I/O handler for all present and future CreativeStudio instances.
	 * 
	 * @param handler The I/O handler singleton.
	 * @param types Generic resource content types that the class can handle.
	 */
	public void registFormatSupport(IG3DFormatHandler handler, CSG3DIOContentType... types) {
		IG3DFormatHandler result = formatGc.regGc(handler);
		if (result != null) {
			ioMgr.registHandler(handler, types);
		} else if (handler != null) {
			System.out.println("Could not register format " + handler.getExtensionFilter().formatName + ": not allowed here.");
		}
	}

	/**
	 * Registers a batch of I/O handlers for all present and future CreativeStudio instances.
	 * 
	 * @param type The generic resource type that these I/O classes handle.
	 * @param handlers An arbitrary number of handler classes.
	 */
	public void registFormatSupport(CSG3DIOContentType type, IG3DFormatHandler... handlers) {
		for (IG3DFormatHandler h : handlers) {
			registFormatSupport(h, type);
		}
	}

	/**
	 * Adds an UI menu item into all CreativeStudio windows.
	 * 
	 * @param menuName Name of the item's parent menu.
	 * @param item The item to add.
	 */
	public void addMenuItem(String menuName, JMenuItem item) {
		if (menuName != null && item != null) {
			if (currentUiMgr != null) {
				if ((item = menuGc.regGc(item)) != null) {
					currentUiMgr.addMenuItem(menuName, item);
				}
			}
		}
	}
}
