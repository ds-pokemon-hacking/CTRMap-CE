package ctrmap.missioncontrol_base.debug;

import java.util.ArrayList;
import java.util.List;
import xstandard.util.ArraysEx;

public class MCDebuggerManager {

	private final List<IMCDebugger> allDebuggers = new ArrayList<>();
	private final List<IMCDebuggable> debuggables = new ArrayList<>();
	
	public void closeAll() {
		for (IMCDebuggable d : debuggables) {
			callDebuggers(d, (debugger) -> {
				d.destroy(debugger);
			});
		}
		debuggables.clear();
		allDebuggers.clear();
	}

	public void registDebuggable(IMCDebuggable debuggable) {
		ArraysEx.addIfNotNullOrContains(debuggables, debuggable);
		//attach existing debuggers
		reattachDebuggers(debuggable);
	}
	
	public void unregistDebuggable(IMCDebuggable debuggable) {
		debuggables.remove(debuggable);
		callDebuggers(debuggable, (debugger) -> {
			debuggable.detach(debugger);
		});
	}

	public void registDebugger(IMCDebugger debugger) {
		ArraysEx.addIfNotNullOrContains(allDebuggers, debugger);
		if (debugger != null) {
			for (IMCDebuggable d : debuggables) {
				if (d.getDebuggerClass().isAssignableFrom(debugger.getClass())) {
					d.attach(debugger);
				}
			}
		}
	}

	public void unregistDebugger(IMCDebugger debugger) {
		allDebuggers.remove(debugger);
		if (debugger != null) {
			for (IMCDebuggable d : debuggables) {
				if (d.getDebuggerClass().isAssignableFrom(debugger.getClass())) {
					d.detach(debugger);
				}
			}
		}
	}
	
	public <D extends IMCDebugger> void callDebuggers(Class<D> debuggerClass, CallFunc<D> callFunc) {
		for (IMCDebugger dbg : allDebuggers) {
			if (debuggerClass.isAssignableFrom(dbg.getClass())) {
				callFunc.exec((D)dbg);
			}
		}
	}

	public <D extends IMCDebugger> void callDebuggers(IMCDebuggable<D> parent, CallFunc<D> callFunc) {
		callDebuggers(parent.getDebuggerClass(), callFunc);
	}
	
	public <D extends IMCDebugger> boolean getBoolPermFromDebuggers(IMCDebuggable<D> parent, BoolCapFunc<D> func) {
		Class<D> debuggerClass = parent.getDebuggerClass();
		
		for (IMCDebugger dbg : allDebuggers) {
			if (debuggerClass.isAssignableFrom(dbg.getClass())) {
				if (func.get((D)dbg)) {
					return true;
				}
			}
		}
		
		return false;
	}
	
	public void reattachDebuggers(IMCDebuggable parent) {
		callDebuggers(parent, (debugger) -> {
			parent.attach(debugger);
		});
	}

	public interface CallFunc<D> {

		public void exec(D debugger);
	}
	
	public interface BoolCapFunc<D> {

		public boolean get(D debugger);
	}
}
