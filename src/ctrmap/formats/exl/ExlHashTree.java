package ctrmap.formats.exl;

import xstandard.io.serialization.annotations.Inline;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ExlHashTree {

	@Inline
	public List<ExlHashTreeNode> nodes;

	public ExlHashTree(List<String> names) {
		nodes = new ArrayList<>();
		for (int i = 0; i < names.size(); i++) {
			nodes.add(new ExlHashTreeNode(names.get(i), i));
		}
		Collections.sort(nodes);
	}
	
	public ExlHashTree() {
		
	}

	@Inline
	public static class ExlHashTreeNode implements Comparable<ExlHashTreeNode> {

		public int hash;
		public int value;

		public ExlHashTreeNode(String name, int index) {
			hash = getNameHash(name);
			value = index;
		}
		
		public ExlHashTreeNode() {
			
		}

		public static int getNameHash(String name) {
			if (name == null) {
				return 0;
			}
			//FNV1a-32
			int hash = 0x811C9DC5;
			int len = name.length();
			for (int i = 0; i < len; i++) {
				hash = (hash ^ name.charAt(i)) * 16777619;
			}
			return hash;
		}

		@Override
		public int compareTo(ExlHashTreeNode o) {
			return Integer.compareUnsigned(hash, o.hash);
		}
	}
}
