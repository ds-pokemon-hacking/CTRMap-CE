package ctrmap.missioncontrol_base.debug;

public interface IMCDebuggable<D extends IMCDebugger> {
	public Class<D> getDebuggerClass();
	
	public void attach(D debugger);
	public void detach(D debugger);
	
	public void destroy(D debugger);
}
