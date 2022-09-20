package ctrmap.missioncontrol_base;

public class McSubLogger extends McLogger {

	private final McLogger log;
	private final McLogger.ILogMsgSource source;

	public McSubLogger(McLogger log, McLogger.ILogMsgSource source) {
		this.log = log;
		this.source = source;
	}
	
	public McSubLogger(McLogger log, String sourceName) {
		this(log, new StringLogMsgSource(sourceName));
	}

	private McLogger.ILogMsgSource[] getFullLogPath(McLogger.ILogMsgSource[] extra) {
		McLogger.ILogMsgSource[] joint = new McLogger.ILogMsgSource[extra.length + 1];
		joint[0] = source;
		System.arraycopy(extra, 0, joint, 1, extra.length);
		return joint;
	}

	@Override
	protected void printLnImpl(String type, String msg, String extraInfo, ILogMsgSource... sources) {
		log.printLnImpl(type, msg, extraInfo, getFullLogPath(sources));
	}

	@Override
	protected void print(String s) {
		log.print(s);
	}
	
	private static class StringLogMsgSource implements ILogMsgSource {

		private final String name;
		
		public StringLogMsgSource(String name) {
			this.name = name;
		}
		
		@Override
		public String name() {
			return name;
		}
	}
}
