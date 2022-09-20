package ctrmap.creativestudio.ngcs.tree;

public class CSNodeContentCapabilities {
	public static final int CAP_EXPORT = (1 << 0);
	public static final int CAP_IMPORT_REPLACE = (1 << 1);
	
	public static final int CAP_ALL = CAP_EXPORT | CAP_IMPORT_REPLACE;
}
