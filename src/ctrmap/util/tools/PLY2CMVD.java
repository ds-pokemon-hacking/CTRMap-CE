package ctrmap.util.tools;

import ctrmap.formats.internal.CMVD;
import ctrmap.formats.generic.ply.PLY;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Command line tool to convert PLY objects (in ASCII blender format) to CMVD
 * binaries.
 */
public class PLY2CMVD {

	public static void main(String[] args) {
		if (args.length < 2) {
			System.out.println("Usage:\n");
			System.out.println("<ply2cmvd-executable> input.ply output.cmvd\n");
			System.out.println("Part of the CTRMap toolset - github.com/HelloOO7/CTRMap");
			return;
		}
		try {
			OutputStream dst = new FileOutputStream(args[1]);
			CMVD cmvd = new CMVD();
			PLY ply = new PLY(new File(args[0]));

			for (PLY.PLYElementGroup.Element face : ply.faces.elements) {
				int[] f = PLY.getFace(face);
				cmvd.faces.add(new int[]{f[0], f[1], f[2]});
				if (f.length == 4) {
					cmvd.faces.add(new int[]{f[0], f[3], f[2]});
				}
			}

			for (PLY.PLYElementGroup.Element vertex : ply.vertices.elements) {
				CMVD.CMVDVertex v = new CMVD.CMVDVertex();
				v.col = PLY.getColor(vertex.getProperty("red"), vertex.getProperty("green"), vertex.getProperty("blue"), vertex.getProperty("alpha"));
				v.x = vertex.getProperty("x").getFloatValue();
				v.y = vertex.getProperty("y").getFloatValue();
				v.z = vertex.getProperty("z").getFloatValue();
				cmvd.vertices.add(v);
			}

			/*BufferedReader src = new BufferedReader(new InputStreamReader(new FileInputStream(new File(args[0]))));
			OutputStream dst = new FileOutputStream(args[1]);
			CMVD cmvd = new CMVD();
			int vcount = 0;
			int fcount = 0;
			//read PLY header
			String read;
			while (!"end_header".equals(read = src.readLine())){
				String[] commands = read.split("\\s+");
				if (commands.length > 0){
					if (commands[0].equals("element")){
						switch (commands[1]){
							case "vertex":
								vcount = Integer.parseInt(commands[2]);
								break;
							case "face":
								fcount = Integer.parseInt(commands[2]);
								break;
						}
					}
				}
			}
			for (int vertex = 0; vertex < vcount; vertex++){
				String[] params = src.readLine().split("\\s+");
				CMVD.CMVDVertex v = new CMVD.CMVDVertex();
				v.x = Float.parseFloat(params[0]);
				v.z = Float.parseFloat(params[1]); //blender has its coordinate system's Y and Z inverted
				v.y = Float.parseFloat(params[2]);
				int red = getInt(params[6]);
				int green = getInt(params[7]);
				int blue = getInt(params[8]);
				v.col = new Color(red, green, blue);
				cmvd.vertices.add(v);
			}
			for (int face = 0; face < fcount; face++){
				String[] params = src.readLine().split("\\s+");
				cmvd.faces.add(new int[]{getInt(params[1]), getInt(params[2]), getInt(params[3])});
				if (params[0].equals("4")){
					//quad, need to split to two faces, so we add the new one
					cmvd.faces.add(new int[]{getInt(params[1]), getInt(params[4]), getInt(params[3])});
				}
			}
			src.close();*/
			cmvd.write(dst);
			dst.close();
		} catch (IOException ex) {
			Logger.getLogger(PLY2CMVD.class.getName()).log(Level.SEVERE, null, ex);
		}
	}

	public static int getInt(String s) {
		return Integer.parseInt(s);
	}
}
