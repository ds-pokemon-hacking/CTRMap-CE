package ctrmap.util.tools;

import xstandard.fs.FSFile;
import xstandard.fs.accessors.DiskFile;
import java.util.Scanner;

public class MigratorCleaner {

	public static void main(String[] args) {
		clean(new DiskFile("D:\\Emugames\\3DS\\3dstools\\oras_ex2\\RomFS\\elf_dump\\sango.elf_symbols_20210707-142504.json"));
	}

	public static void clean(FSFile fsf) {
		Scanner s = new Scanner(fsf.getNativeInputStream());

		StringBuilder out = new StringBuilder();

		boolean funcStart = false;

		StringBuilder funcInfo = new StringBuilder();

		while (s.hasNextLine()) {
			String line = s.nextLine();
			if (!funcStart) {
				if (line.contains("\"functions\"")) {
					funcStart = true;
				}
				out.append(line);
				out.append("\n");
			} else {
				if (line.contains("]")) {
					funcStart = false;
					out.append(line);
					out.append("\n");

					String lastElem = "},";

					int lidx = out.lastIndexOf(lastElem);
					int lidxNormal = out.lastIndexOf("}");

					if (lidx == lidxNormal) {
						out.delete(lidx + 1, lidx + lastElem.length());
					}
				} else {
					funcInfo.append(line);
					funcInfo.append("\n");
					if (line.contains("}")) {
						String nameKey = "\"name\":";

						int nameIdx = funcInfo.indexOf(nameKey);
						int qIdx = funcInfo.indexOf("\"", nameIdx + nameKey.length());
						int qIdx2 = funcInfo.indexOf("\"", qIdx + 1);
						String name = funcInfo.substring(qIdx + 1, qIdx2);
						if (name.startsWith("j_") || name.startsWith("sub_") || name.startsWith("nullsub_") || name.startsWith("Dll") || name.startsWith("_static_")) {
							//omit the func
						} else {
							out.append(funcInfo);
						}
						funcInfo = new StringBuilder();
					}
				}
			}
		}

		s.close();

		fsf.setBytes(out.toString().getBytes());
	}
}
