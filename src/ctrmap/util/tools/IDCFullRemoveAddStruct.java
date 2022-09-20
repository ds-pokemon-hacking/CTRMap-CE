
package ctrmap.util.tools;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class IDCFullRemoveAddStruct {
	public static void main(String[] args) {
		String idcPath = "D:\\Emugames\\DS\\hacking_stuff\\IDB\\ex_symbols\\arm9-white2_decompressed_U.idc";
		if (args.length > 0) {
			idcPath = args[0];
		}
		try {
			Path path = Paths.get(idcPath);
			List<String> lines = Files.readAllLines(path);
			
			for (int index = 0; index < lines.size(); index++) {
				String line = lines.get(index).trim();
				if (line.startsWith("id = add_struc(")) {
					int idxStrucNameStart = line.indexOf('"');
					int idxStrucNameEnd = line.indexOf('"', idxStrucNameStart + 1);
					String strucName = line.substring(idxStrucNameStart, idxStrucNameEnd + 1);
					lines.add(index, "\tdel_struc(get_struc(get_struc_id(" + strucName + ")));");
					index++;
				}
				else if (line.startsWith("id = add_enum(")) {
					int idxEnumNameStart = line.indexOf('"');
					int idxEnumNameEnd = line.indexOf('"', idxEnumNameStart + 1);
					String enumName = line.substring(idxEnumNameStart, idxEnumNameEnd + 1);
					lines.add(index, "\tdel_enum(get_enum(" + enumName + "));");
					index++;
				}
			}
			
			Files.write(path, lines);
		} catch (IOException ex) {
			Logger.getLogger(IDCFullRemoveAddStruct.class.getName()).log(Level.SEVERE, null, ex);
		}
	}
}
