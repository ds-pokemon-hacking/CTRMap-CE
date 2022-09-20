package ctrmap.missioncontrol_base;

import java.util.ArrayList;
import java.util.List;

public abstract class McLogger {

	private List<McLogger> children = new ArrayList<>();
	
	public McSubLogger createSubLogger(ILogMsgSource src) {
		return new McSubLogger(this, src);
	}
	
	public McSubLogger createSubLogger(String srcName) {
		return new McSubLogger(this, srcName);
	}
	
	public void addChild(McLogger ch){
		children.add(ch);
	}
	
	public void removeChild(McLogger ch){
		children.remove(ch);
	}
	
	protected abstract void print(String s);

	public void err(String msg) {
		err(msg, new McLogger.ILogMsgSource[0]);
	}

	public void out(String msg) {
		out(msg, new McLogger.ILogMsgSource[0]);
	}

	public void warn(String msg) {
		warn(msg, new McLogger.ILogMsgSource[0]);
	}
	
	public void err(String msg, ILogMsgSource... src) {
		err(msg, null, src);
	}

	public void out(String msg, ILogMsgSource... src) {
		out(msg, null, src);
	}
	
	public void warn(String msg, ILogMsgSource... src) {
		warn(msg, null, src);
	}
	
	public void err(String msg, String extraInfo, ILogMsgSource... src) {
		printLnImpl("ERR", msg, extraInfo, src);
	}

	public void out(String msg, String extraInfo, ILogMsgSource... src) {
		printLnImpl("INFO", msg, extraInfo, src);
	}
	
	public void warn(String msg, String extraInfo, ILogMsgSource... src){
		printLnImpl("WARN", msg, extraInfo, src);
	}

	protected void printLnImpl(String type, String msg, String extraInfo, ILogMsgSource... sources) {
		StringBuilder sb = new StringBuilder(type);
		sb.append(": [MC]");
		for (ILogMsgSource src : sources) {
			sb.append("[");
			sb.append(src.name());
			sb.append("]");
		}
		if (extraInfo != null) {
			sb.append(extraInfo);
		}
		sb.append(" ");
		sb.append(msg);
		sb.append("\n");
		print(sb.toString());
		for (McLogger ch : children){
			ch.printLnImpl(type, msg, extraInfo, sources);
		}
	}
	
	public interface ILogMsgSource {
		public String name();
	}

	public static class StdOutLogger extends McLogger {

		@Override
		protected void print(String s) {
			System.out.print(s);
		}
	}
}
