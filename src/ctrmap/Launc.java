package ctrmap;

import com.jogamp.opengl.GLProfile;
import xstandard.gui.ActionSelector;
import xstandard.gui.DialogUtils;
import xstandard.gui.components.ComponentUtils;
import xstandard.util.JARUtils;
import java.awt.Dialog;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.SwingUtilities;
import rtldr.JRTLDRCore;
import xstandard.util.ArraysEx;
import rtldr.JExtensionReceiver;
import rtldr.RExtensionBase;

public class Launc extends ActionSelector {
	
	private static final JulietInterface PLUGIN_IFACE_INSTANCE = new JulietInterface();
	private static final List<Subprocess> launcherSubprocs = new ArrayList<>();
	
	static {
		JRTLDRCore.bindExtensionManager("LauncherPlugin", PLUGIN_IFACE_INSTANCE);
		//no listening is needed as the launcher will only load plugins once
	}
	
	private static void registSubproces(Subprocess sp) {
		ArraysEx.addIfNotNullOrContains(launcherSubprocs, sp);
	}
	
	private static ASelAction[] createActionsForSubprocs() {
		ASelAction[] actions = new ASelAction[launcherSubprocs.size()];
		for (int i = 0; i < launcherSubprocs.size(); i++) {
			Subprocess sp = launcherSubprocs.get(i);
			actions[i] = new ASelAction(sp.name, sp);
		}
		return actions;
	}

	static final Thread singletonInitThread = new Thread((() -> {
		//Save time by early-calling JOGL singleton initializer
		GLProfile.initSingleton();
	}));
	
	public Launc() {
		super((Dialog) null, false,
			createActionsForSubprocs()
		);
		System.out.println("CTRMap Launcher initialized. Running on Java platform " + System.getProperty("java.version") + " (" + System.getProperty("sun.arch.data.model") + "-bit).");
		String buildDate = JARUtils.getBuildDate();
		if (buildDate != null) {
			System.out.println("Build date: " + buildDate);
		}
		else {
			System.out.println("Build date not present, assuming development build.");
		}
		
		CTRMapResources.load();

		setTitle("CTRMap Launcher");
		setDisposeOnSelected(false);
		btnCancel.setText("Quit");
		btnConfirm.setText("Launch");
		btnConfirm.addActionListener(((e) -> {
			try {
				singletonInitThread.join();
			} catch (InterruptedException ex) {
				Logger.getLogger(Launc.class.getName()).log(Level.SEVERE, null, ex);
			}
			Object userObj = getSelectedUserObj();
			if (userObj instanceof Subprocess) {
				Subprocess sp = (Subprocess) userObj;
				boolean started = sp.starter.start();
				if (!started) {
					DialogUtils.showErrorMessage(this, "Error", "Subprocess \"" + sp.name + "\" failed to start.");
				}
				else {
					setVisible(false);
				}
			}
		}));
		btnCancel.addActionListener(((e) -> {
			System.exit(0);
		}));
	}

	public static void main(String[] args) {
		singletonInitThread.start();
		SwingUtilities.invokeLater((() -> {
			ComponentUtils.setSystemNativeLookAndFeel();
			new Launc().setVisible(true);
		}));
	}
	
	public static class Subprocess {
		public final String name;
		public final SubprocessStarter starter;
		
		public Subprocess(String name, SubprocessStarter startFunc) {
			this.name = name;
			this.starter = startFunc;
		}
	}
	
	public static interface SubprocessStarter {
		public boolean start();
	}
	
	public static class JulietInterface implements JExtensionReceiver<IPlugin> {
		/**
		 * Registers an arbitrary top-level subprocess function into the global launcher.
		 * 
		 * @param name Name of the process in the launcher's dialog.
		 * @param starter The function that starts the subprocess.
		 */
		public void registerSubprocess(String name, SubprocessStarter starter) {
			Launc.registSubproces(new Subprocess(name, starter));
		}
	}
	
	public static interface IPlugin extends RExtensionBase<JulietInterface> {
		
	}
}
