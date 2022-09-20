package ctrmap.renderer.backends.base.shaderengine;

import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

public class ShaderExtensionSource {

	public final String code;

	public Map<String, String> extendedCodeData = new HashMap<>();

	private static final String REGION_TAG = "region";
	private static final String TARGET_TAG = "target";

	public ShaderExtensionSource(String shaderCode) {
		this.code = shaderCode;
		Scanner scanner = new Scanner(code);

		String line;
		while (scanner.hasNextLine()) {
			line = scanner.nextLine();
			if (line.startsWith("//")) {
				int startIndex = "//".length();
				int endIndex = line.indexOf(" ", startIndex);
				if (endIndex != -1) {
					String command = line.substring(startIndex, endIndex);
					switch (command) {
						case "EXTENSION-CODE":
							String regionAttr = getAttribute(line, REGION_TAG);
							if (regionAttr != null) {
								line = scanner.nextLine();
								if (line.startsWith("/*")) {
									StringBuilder codeBlock = new StringBuilder();
									while (!(line = scanner.nextLine()).startsWith("*/")) {
										codeBlock.append(line);
										codeBlock.append("\n");
									}
									extendedCodeData.put(regionAttr, codeBlock.toString());
								}
							}
							break;
					}
				}
			}
		}

		scanner.close();
	}

	public static String getAttribute(String source, String attName) {
		if (source.contains(attName)) {
			int beginIndex = source.indexOf("=", source.indexOf(attName));
			StringBuilder sb = new StringBuilder();
			for (int idx = beginIndex + 1; idx < source.length(); idx++) {
				char ch = source.charAt(idx);
				if (!Character.isWhitespace(ch)) {
					sb.append(ch);
				} else {
					break;
				}
			}
			return sb.toString();
		}
		return null;
	}

	public String getExtendedShader(String sourceShader) {
		StringBuilder out = new StringBuilder(sourceShader);

		boolean hasEffect = false;

		for (Map.Entry<String, String> extension : extendedCodeData.entrySet()) {
			String extensionTag = "//EXTENSION-" + extension.getKey();
			String extensionCode = extension.getValue();

			int index = out.indexOf(extensionTag);
			if (index != -1) {
				hasEffect = true;
				int endIndex = out.indexOf("\n", index);
				String cfgLine = out.substring(index, endIndex);
				String target = getAttribute(cfgLine, TARGET_TAG);
				if (target != null) {
					extensionCode = extensionCode.replaceAll(TARGET_TAG, target);
				}
				out.insert(index, extensionCode + "\n"); //insert before to preserve extension order when multiple extensions extend the same region
			}
		}

		if (!hasEffect) {
			return sourceShader;
		}

		//System.out.println(out.toString());
		return out.toString();
	}
}
