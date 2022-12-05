package ctrmap.util.tools.cont;

import java.util.ArrayList;
import java.util.List;

public class AGFCIdentifyResult {

	public int index;
	public IContentType contentType;
	public String magicReserve;
	public List<String> contents = new ArrayList<>();

	public void guessMagic(byte[] bytes) {
		if (bytes.length >= 4) {
			String test = new String(bytes, 0, 4);
			boolean hasUpper = false;
			boolean upper = false;
			if (test.length() != 4) {
				return;
			}
			for (int i = 0; i < test.length(); i++) {
				char c = test.charAt(i);
				if (Character.isLetterOrDigit(c)) {
					if (!Character.isDigit(c)) {
						if (!hasUpper) {
							upper = Character.isUpperCase(c);
							hasUpper = true;
						} else if (Character.isUpperCase(c) != upper) {
							return;
						}
					}
				} else {
					return;
				}
			}
			magicReserve = test;
		}
	}

	public String getFullText() {
		StringBuilder sb = new StringBuilder();
		if (contents.isEmpty()) {
			return getFileName();
		} else {
			sb.append(index);
			sb.append(" - ");
			for (int i = 0; i < contents.size(); i++) {
				if (i != 0) {
					sb.append(", ");
				}
				sb.append(contents.get(i));
			}
		}
		return sb.toString();
	}

	public String getFileName() {
		StringBuilder sb = new StringBuilder();
		if (contents.size() != 1) {
			sb.append(index);
		} else {
			sb.append(contents.get(0));
		}
		String extension = null;
		if (contentType != null) {
			extension = contentType.getExtensionWithoutDot();
		}
		if (extension == null) {
			extension = magicReserve;
		}
		if (extension != null) {
			sb.append(".");
			sb.append(extension.toLowerCase());
		}
		return sb.toString();
	}

	public static interface IContentType {

		public String getExtensionWithoutDot();
	}
}
