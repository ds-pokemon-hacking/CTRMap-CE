
package ctrmap.util.tools;

import xstandard.fs.accessors.DiskFile;
import xstandard.io.util.IndentedPrintStream;
import rpm.elfconv.ESDBAddress;
import rpm.elfconv.ExternalSymbolDB;

public class ESDB2MigratorJson {
	public static void main(String[] args) {
		ExternalSymbolDB esdb = new ExternalSymbolDB(new DiskFile("D:\\_REWorkspace\\pokescript_genv\\codeinjection_new\\esdb_field.yml"));
		IndentedPrintStream out = new IndentedPrintStream(new DiskFile("D:\\_REWorkspace\\pokescript_genv\\codeinjection_new\\names.json").getNativeOutputStream());
		out.setIndentIsTabs(false);
		out.println("{");
		out.incrementIndentLevel();
		out.println("\"bpe_info\": {\n" +
			"    \"arch\": \"x32_bit\",\n" +
			"    \"base_addr\": \"0x00000000\",\n" +
			"    \"db_path\": \"C:\\\\exe.exe\",\n" +
			"    \"exe\": \"exe.exe\",\n" +
			"    \"file_type\": \"Binary file\"\n" +
			"},");
		out.println("\"functions\": [");
		out.incrementIndentLevel();
		
		boolean start = true;
		for (ESDBAddress adr : esdb.getAddresses()) {
			String name = esdb.getNameOfAddress(adr);
			if (!name.startsWith("nullsub_") && !name.startsWith("sub_") && !name.startsWith("jpt_") && !name.startsWith("def_")) {
				if (!start) {
					out.println(",");
				}
				else {
					start = false;
				}
				out.println("{");
				out.incrementIndentLevel();

				out.println("\"address\": \"0x" + Integer.toHexString(adr.address & 0xFFFFFFFE) + "\","); //omit thumb bit
				out.println("\"name\": \"" + name + "\",");
				out.println("\"type\": \"\"");

				out.decrementIndentLevel();
				out.print("}");
			}
		}
		out.println();
		
		out.decrementIndentLevel();
		out.println("],");
		out.println("\"functions_count\": " + esdb.getAddresses().size());
		
		out.decrementIndentLevel();
		out.println("}");
		
		out.close();
	}
}
