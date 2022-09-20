package ctrmap.formats.generic.xobj;

import ctrmap.renderer.scene.Scene;
import ctrmap.renderer.scene.model.Model;
import ctrmap.renderer.scene.texturing.Material;
import ctrmap.renderer.scene.texturing.MaterialParams;
import ctrmap.renderer.scene.texturing.Texture;
import ctrmap.renderer.scene.texturing.TextureMapper;
import ctrmap.renderer.scenegraph.G3DResource;
import ctrmap.renderer.util.texture.TextureConverter;
import xstandard.fs.FSFile;
import xstandard.fs.FSUtil;
import xstandard.gui.file.CommonExtensionFilters;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;

public class MTLFile {

	public List<Material> materials = new ArrayList<>();

	public MTLFile(FSFile f) {
		if (f == null || !f.exists()) {
			return;
		}
		Scanner scanner = new Scanner(f.getNativeInputStream());
		Material currentMaterial = null;

		while (scanner.hasNextLine()) {
			String[] cmds = scanner.nextLine().split(" ", 2);
			if (cmds.length == 0) {
				continue;
			}

			switch (cmds[0]) {
				case "newmtl":
					if (currentMaterial != null) {
						materials.add(currentMaterial);
					}
					currentMaterial = new Material();
					currentMaterial.name = cmds[1];
					break;
				case "map_Kd":
					currentMaterial.textures.add(new TextureMapper(cmds[1].replace('\\', '/').replaceAll("//", "/")));
					break;
			}
		}
		if (currentMaterial != null) {
			materials.add(currentMaterial);
		}
		scanner.close();
	}

	public void write(FSFile f, List<Texture> textures, OBJExportSettings settings) {
		FSFile texDir = f.getParent();
		if (texDir != null) {
			PrintStream out = new PrintStream(f.getNativeOutputStream());

			if (settings.TEX_DIR_SEPARATE) {
				texDir = texDir.getChild("/Textures");
				texDir.mkdirs();
			}

			for (Material mat : materials) {
				out.print("newmtl ");
				out.println(mat.name);

				if (!mat.textures.isEmpty()) {
					out.print("map_Kd ");
					TextureMapper mapper = mat.textures.get(0);
					String textureName = FSUtil.getFileNameWithoutExtension(mapper.textureName) + CommonExtensionFilters.PNG.getPrimaryExtension();
					String texturePath = (settings.TEX_DIR_SEPARATE ? "Textures/" : "") + textureName;
					out.println(texturePath);
					Texture texture = (Texture) Scene.getNamedObject(mapper.textureName, textures);
					if (texture != null) {
						TextureConverter.writeTextureToFile(texDir.getChild(textureName), "png", texture);
					}

					if (mat.alphaTest.enabled || mat.blendOperation.colorDstFunc == MaterialParams.BlendFunction.ONE_MINUS_SRC_ALPHA) {
						out.print("map_d ");
						out.println(texturePath);
					}
				} else {
					out.println("Ka 1.0 1.0 1.0");
				}

				out.println();
			}

			out.close();
		}
	}

	public MTLFile(G3DResource res) {
		for (Model model : res.models) {
			materials.addAll(model.materials);
		}
	}
}
