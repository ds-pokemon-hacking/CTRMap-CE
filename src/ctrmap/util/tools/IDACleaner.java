package ctrmap.util.tools;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Scanner;

/**
 *
 */
public class IDACleaner {

	public static void scriptNamesCleanOutput(File source) throws IOException {
		Scanner src = new Scanner(source);
		BufferedWriter dst = new BufferedWriter(new FileWriter(source + "_clean.txt"));

		while (src.hasNextLine()) {
			String line = src.nextLine();
			if (line.contains(";") && !line.contains("XREF") && !line.contains(":off")) {
				dst.write(line.substring(line.indexOf(";") + 2).replace("\"", ""));
				dst.newLine();
			}
		}

		dst.close();
	}

	public static void stringsCleanOutput(File source) throws IOException {
		Scanner src = new Scanner(source);
		BufferedWriter dst = new BufferedWriter(new FileWriter(source + "_clean.txt"));

		while (src.hasNextLine()) {
			String line = src.nextLine();
			int idx = line.indexOf("C (16 bits)");
			if (idx == -1) {
				idx = line.indexOf("	C	") + 3;
			} else {
				idx += 12;
			}
			String out = line.substring(idx);
			dst.write(out);
			dst.newLine();
		}

		dst.close();
	}
}
