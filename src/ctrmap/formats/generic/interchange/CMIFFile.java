package ctrmap.formats.generic.interchange;

import ctrmap.renderer.scene.Camera;
import ctrmap.renderer.scene.Light;
import xstandard.io.InvalidMagicException;
import xstandard.io.util.StringIO;
import ctrmap.renderer.scenegraph.G3DResource;
import xstandard.INamed;
import ctrmap.renderer.scene.Scene;
import ctrmap.renderer.scene.animation.camera.CameraAnimation;
import ctrmap.renderer.scene.animation.material.MaterialAnimation;
import ctrmap.renderer.scene.animation.skeletal.SkeletalAnimation;
import ctrmap.renderer.scene.animation.visibility.VisibilityAnimation;
import ctrmap.renderer.scene.metadata.ReservedMetaData;
import ctrmap.renderer.scenegraph.NamedResource;
import ctrmap.renderer.scene.model.Model;
import ctrmap.renderer.scene.texturing.Texture;
import ctrmap.renderer.scenegraph.G3DSceneTemplate;
import xstandard.fs.FSFile;
import xstandard.fs.accessors.DiskFile;
import xstandard.io.structs.PointerTable;
import xstandard.io.structs.TemporaryOffset;
import xstandard.gui.file.ExtensionFilter;
import xstandard.io.base.iface.IOStream;
import xstandard.io.base.impl.access.MemoryStream;
import xstandard.io.base.impl.ext.data.DataIOStream;
import xstandard.io.structs.StringTable;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * The Common Interchange Format is a simple yet effective unified file format for passing data to external utilities. Said utilities, currently limited to SPICA, have to implement the format reader according to the specification.
 */
public class CMIFFile {

	public static final ExtensionFilter EXTENSION_FILTER = new ExtensionFilter("Common Interchange Format", "*.cmif");

	public static final String MAGIC = "CMIF";

	public static final int IF_PADDING = 0x10;

	public List<Model> models = new ArrayList<>();
	public List<Texture> textures = new ArrayList<>();
	public List<Camera> cameras = new ArrayList<>();
	public List<Light> lights = new ArrayList<>();
	public List<SkeletalAnimation> skeletalAnime = new ArrayList<>();
	public List<MaterialAnimation> materialAnime = new ArrayList<>();
	public List<VisibilityAnimation> visAnime = new ArrayList<>();
	public List<CameraAnimation> cameraAnime = new ArrayList<>();

	public List<OtherFile> other = new ArrayList<>();

	public List<G3DSceneTemplate> sceneTemplates = new ArrayList<>();

	public CMIFFile() {

	}

	public CMIFFile(Scene source) {
		this(source, true);
	}

	public CMIFFile(Scene source, boolean addSceneTemplate) {
		G3DResource resStorage = source.getAllResources();
		if (addSceneTemplate) {
			resStorage.addSceneTemplate(new G3DSceneTemplate(source, resStorage));
		}
		merge(resStorage);
	}

	public CMIFFile(G3DResource res) {
		merge(res);
	}

	public void merge(G3DResource res) {
		models.addAll(res.models);
		textures.addAll(res.textures);
		cameras.addAll(res.cameras);
		materialAnime.addAll(res.materialAnimations);
		skeletalAnime.addAll(res.skeletalAnimations);
		visAnime.addAll(res.visibilityAnimations);
		cameraAnime.addAll(res.cameraAnimations);
		sceneTemplates.addAll(res.sceneTemplates);
		lights.addAll(res.lights);
	}

	public CMIFFile(File f) {
		this(new DiskFile(f));
	}

	public CMIFFile(FSFile fsf) {
		this(fsf.getIO());
	}

	public CMIFFile(byte[] b) {
		this(new MemoryStream(b));
	}

	public CMIFFile(IOStream strm) {
		try {
			DataIOStream dis = new DataIOStream(strm);

			if (!StringIO.checkMagic(dis, MAGIC)) {
				throw new InvalidMagicException("Invalid CMIF magic.");
			}

			int version = dis.readInt();
			int backwardsCompatibility = version;
			if (version >= Revisions.REV_BACK_COMPAT) {
				backwardsCompatibility = dis.readInt();
			}
			if (backwardsCompatibility > Revisions.REV_CURRENT_BW_COMPAT) {
				throw new UnsupportedOperationException("File version is too new for this reader.");
			}

			int stringTableOffset = dis.readInt();
			int contentTableOffset = dis.readInt();

			dis.seek(contentTableOffset);
			int modelsPointerTableOffset = dis.readInt();
			int texturesPointerTableOffset = dis.readInt();
			int sklAnmPointerTableOffset = dis.readInt();
			int matAnmPointerTableOffset = dis.readInt();
			int othersPointerTableOffset = -1;
			int camPointerTableOffset = -1;
			int camAnmPointerTableOffset = -1;
			int lightsPointerTableOffset = -1;
			int sceneTemplatesPointerTableOffset = -1;
			if (version >= Revisions.REV_OTHER_FILES) {
				othersPointerTableOffset = dis.readInt();
			}
			if (version >= Revisions.REV_CAMERA) {
				camPointerTableOffset = dis.readInt();
				camAnmPointerTableOffset = dis.readInt();
			}
			if (version >= Revisions.REV_LIGHTS) {
				lightsPointerTableOffset = dis.readInt();
			}
			if (version >= Revisions.REV_SCENE_TEMPLATES) {
				sceneTemplatesPointerTableOffset = dis.readInt();
			}

			readPointerTableAt(dis, modelsPointerTableOffset, ((io) -> {
				models.add(ModelUtil.readModel(io, version));
			}));

			readPointerTableAt(dis, texturesPointerTableOffset, ((io) -> {
				textures.add(TextureUtil.readTexture(io, version));
			}));

			readPointerTableAt(dis, sklAnmPointerTableOffset, ((io) -> {
				skeletalAnime.add(AnimeUtil.readSkeletalAnime(io, version));
			}));

			readPointerTableAt(dis, matAnmPointerTableOffset, ((io) -> {
				materialAnime.add(AnimeUtil.readMaterialAnime(io, version));
			}));

			if (othersPointerTableOffset != -1) {
				readPointerTableAt(dis, othersPointerTableOffset, ((io) -> {
					other.add(OtherFileUtil.readOtherFile(io));
				}));
			}
			if (camPointerTableOffset != -1) {
				readPointerTableAt(dis, camPointerTableOffset, ((io) -> {
					cameras.add(CameraUtil.readCamera(io, version));
				}));
			}
			if (camAnmPointerTableOffset != -1) {
				readPointerTableAt(dis, camAnmPointerTableOffset, (io) -> {
					cameraAnime.add(AnimeUtil.readCameraAnime(io, version));
				});
			}
			if (lightsPointerTableOffset != -1) {
				readPointerTableAt(dis, lightsPointerTableOffset, (io) -> {
					lights.add(LightUtil.readLight(io, version));
				});
			}
			if (sceneTemplatesPointerTableOffset != -1) {
				readPointerTableAt(dis, sceneTemplatesPointerTableOffset, (io) -> {
					sceneTemplates.add(SceneTemplateUtil.readSceneTemplate(io, version));
				});
			}
		} catch (IOException ex) {
			Logger.getLogger(CMIFFile.class.getName()).log(Level.SEVERE, null, ex);
		}
	}
	
	private void readPointerTableAt(DataIOStream io, int offset, PointerTable.PointerTableCallback callback) throws IOException {
		io.seek(offset);
		new PointerTable(io).forEach(callback);
	}

	public void removeLUTTextures() {
		removeNonLUTTextures(true);
	}

	public void removeNonLUTTextures() {
		removeNonLUTTextures(false);
	}

	public void removeNonLUTTextures(boolean invert) {
		for (int i = 0; i < textures.size(); i++) {
			if (!ReservedMetaData.isLUT(textures.get(i)) ^ invert) {
				textures.remove(i);
				i--;
			}
		}
	}

	public OtherFile getOtherFileByName(String name) {
		for (OtherFile f : other) {
			if (f.name.equals(name)) {
				return f;
			}
		}
		return null;
	}

	public G3DResource toGeneric() {
		G3DResource res = new G3DResource();
		res.addModels(models);
		res.addTextures(textures);
		res.addMatAnimes(materialAnime);
		res.addCameras(cameras);
		res.addSklAnimes(skeletalAnime);
		res.addVisAnimes(visAnime);
		res.addCamAnimes(cameraAnime);
		res.addSceneTemplates(sceneTemplates);
		res.addLights(lights);
		for (OtherFile o : other) {
			res.metaData.putValue(o.name, o.data);
		}
		return res;
	}

	public void handleDuplicateNames() {
		removeExactDuplicates(textures);

		Random r = new Random();
		for (Texture t : textures) {
			if (t.getName() == null) {
				t.setName("Texture_" + r.nextInt(1000));
			}
		}

		List<String> processedNames = new ArrayList<>();
		for (int i = 0; i < textures.size(); i++) {
			if (!processedNames.contains(textures.get(i).name)) {
				processedNames.add(textures.get(i).name);
			} else {
				textures.remove(i);
				i--;
			}
		}

		for (Model model : models) {
			removeExactDuplicates(model.meshes);
			removeExactDuplicates(model.materials);
			removeExactDuplicates(model.skeleton.getJoints());
			renameDuplicates(model.meshes, "Mesh");
			renameDuplicates(model.materials, "Material");
			renameDuplicates(model.skeleton.getJoints(), "Joint");
		}

		removeExactDuplicates(models);
		removeExactDuplicates(skeletalAnime);
		removeExactDuplicates(materialAnime);
		removeExactDuplicates(cameraAnime);
		removeExactDuplicates(visAnime);
		removeExactDuplicates(lights);
		removeExactDuplicates(sceneTemplates);
		renameDuplicates(models, "Model");
		renameDuplicates(skeletalAnime, "Motion");
		renameDuplicates(visAnime, "VisAnim");
		renameDuplicates(cameraAnime, "CamAnim");
		renameDuplicates(materialAnime, "MatAnim");
		renameDuplicates(lights, "Light");
		renameDuplicates(sceneTemplates, "SceneTemplate");
	}

	private static void removeExactDuplicates(List<? extends NamedResource> l) {
		for (int i = 0; i < l.size(); i++) {
			if (l.indexOf(l.get(i)) != i) {
				l.remove(i);
				i--;
			}
		}
	}

	public static void renameDuplicates(List<? extends NamedResource> l, String defName) {
		for (INamed a : l) {
			if (a.getName() == null) {
				a.setName(defName);
			}
			if (Scene.getNamedObject(a.getName(), l) != a) {
				String newName;
				int num = 1;
				while (true) {
					newName = a.getName() + "_" + String.valueOf(num);
					INamed obj = Scene.getNamedObject(newName, l);
					if (obj == null || obj == a) {
						break;
					}
					num++;
				}
				a.setName(newName);
			}
		}
	}

	public static int getStringTableOffs(String s, Map<String, Integer> table) {
		if (table != null) {
			if (table.containsKey(s)) {
				return table.get(s);
			}
		}
		return 0;
	}

	public static StringTable getEmptyLevel0StrTable(DataIOStream out, INamed root) {
		StringTable table = new StringTable(out);
		table.putINamed(root);
		return table;
	}

	public static CMIFWriter writeLevel0File(String magic) throws IOException {
		CMIFWriter dos = new CMIFWriter();
		dos.writeStringUnterminated(magic);
		dos.writeInt(0xFFFFFFFF);
		dos.writeInt(Revisions.REV_CURRENT);
		TemporaryOffset dataOffs = new TemporaryOffset(dos);
		dos.pad(IF_PADDING);
		dataOffs.setHere();
		return dos;
	}

	public static Level0Info readLevel0File(File f, String magic) throws IOException {
		DataIOStream bais = new DataIOStream(f);

		return readLevel0Section(bais, magic);
	}

	public static Level0Info readLevel0File(FSFile f, String magic) throws IOException {
		DataIOStream bais = f.getDataIOStream();

		return readLevel0Section(bais, magic);
	}

	public static Level0Info readLevel0Section(DataIOStream bais, String magic) throws IOException {
		if (!StringIO.checkMagic(bais, magic)) {
			throw new InvalidMagicException("Invalid Level0 " + magic + " magic.");
		}

		int dataStart = bais.readInt();
		int version = Revisions.REV_LEVEL0_VERSION - 1;
		if (dataStart == 0xFFFFFFFF) {
			//This is a versioned (CMIF-4) Level0 file
			version = bais.readInt();
			dataStart = bais.readInt();
		}
		bais.seek(dataStart);
		Level0Info r = new Level0Info();
		r.io = bais;
		r.fileVersion = version;
		return r;
	}

	public byte[] getBinaryData() {
		return getBinaryData(new CMIFWriteConfig());
	}

	public byte[] getBinaryData(CMIFWriteConfig cfg) {
		//handle duplicates
		handleDuplicateNames();

		MemoryStream out = new MemoryStream();
		write(out, cfg);
		return out.toByteArray();
	}

	public void write(IOStream io, CMIFWriteConfig cfg) {
		try {
			CMIFWriter dos = new CMIFWriter(io);

			dos.writeStringUnterminated(MAGIC);				//Format magic
			dos.writeInt(Revisions.REV_CURRENT);			//Converter version
			dos.writeInt(Revisions.REV_CURRENT_BW_COMPAT);	//Minimal version of the reader

			dos.setOffsetToStringTableHere();
			TemporaryOffset contentTableOffset = new TemporaryOffset(dos);

			//pad to 16 bytes cause why not
			dos.pad(IF_PADDING);

			//CONTENT TABLE
			contentTableOffset.setHere();
			TemporaryOffset modelsPointerTableOffset = new TemporaryOffset(dos);
			TemporaryOffset texturesPointerTableOffset = new TemporaryOffset(dos);
			TemporaryOffset sklAnmPointerTableOffset = new TemporaryOffset(dos);
			TemporaryOffset mtAnmPointerTableOffset = new TemporaryOffset(dos);
			TemporaryOffset othersPointerTableOffset = new TemporaryOffset(dos);
			TemporaryOffset cameraPointerTableOffset = new TemporaryOffset(dos);
			TemporaryOffset camAnmPointerTableOffset = new TemporaryOffset(dos);
			TemporaryOffset lightsPointerTableOffset = new TemporaryOffset(dos);
			TemporaryOffset scnTempPointerTableOffset = new TemporaryOffset(dos);

			//that's 16 bytes yet again. no need to pad
			//POINTER TABLES
			modelsPointerTableOffset.setHere();
			List<TemporaryOffset> modelOffsets = PointerTable.allocatePointerTable(models.size(), dos);
			texturesPointerTableOffset.setHere();
			List<TemporaryOffset> textureOffsets = PointerTable.allocatePointerTable(textures.size(), dos);
			sklAnmPointerTableOffset.setHere();
			List<TemporaryOffset> sklAnmOffsets = PointerTable.allocatePointerTable(skeletalAnime.size(), dos);
			mtAnmPointerTableOffset.setHere();
			List<TemporaryOffset> mtAnmOffsets = PointerTable.allocatePointerTable(materialAnime.size(), dos);
			othersPointerTableOffset.setHere();
			List<TemporaryOffset> othersOffsets = PointerTable.allocatePointerTable(other.size(), dos);
			cameraPointerTableOffset.setHere();
			List<TemporaryOffset> camerasOffsets = PointerTable.allocatePointerTable(cameras.size(), dos);
			camAnmPointerTableOffset.setHere();
			List<TemporaryOffset> camAnmOffsets = PointerTable.allocatePointerTable(cameraAnime.size(), dos);
			lightsPointerTableOffset.setHere();
			List<TemporaryOffset> lightOffsets = PointerTable.allocatePointerTable(lights.size(), dos);
			scnTempPointerTableOffset.setHere();
			List<TemporaryOffset> sceneTemplateOffsets = PointerTable.allocatePointerTable(sceneTemplates.size(), dos);

			//that's it for the tables. Now write the data
			//I mean OOP is a thing et cetera, but I would like to separate the Mission Control scene graph from the format library
			//for that reason, everything will be written statically from this accessor class
			dos.pad(IF_PADDING);

			for (int i = 0; i < models.size(); i++) {
				modelOffsets.get(i).setHere();
				ModelUtil.writeModel(models.get(i), dos);

				dos.pad(IF_PADDING);
			}

			for (int i = 0; i < textures.size(); i++) {
				textureOffsets.get(i).setHere();
				TextureUtil.writeTexture(textures.get(i), cfg.LZ_TEXTURE_COMPRESS, dos);

				dos.pad(IF_PADDING);
			}

			for (int i = 0; i < skeletalAnime.size(); i++) {
				sklAnmOffsets.get(i).setHere();
				AnimeUtil.writeSklAnime(skeletalAnime.get(i), dos);

				dos.pad(IF_PADDING);
			}

			for (int i = 0; i < materialAnime.size(); i++) {
				mtAnmOffsets.get(i).setHere();
				AnimeUtil.writeMatAnime(materialAnime.get(i), dos);

				dos.pad(IF_PADDING);
			}

			for (int i = 0; i < other.size(); i++) {
				othersOffsets.get(i).setHere();
				OtherFileUtil.writeOtherFile(other.get(i), dos);

				dos.pad(IF_PADDING);
			}

			for (int i = 0; i < cameras.size(); i++) {
				camerasOffsets.get(i).setHere();
				CameraUtil.writeCamera(cameras.get(i), dos);

				dos.pad(IF_PADDING);
			}

			for (int i = 0; i < cameraAnime.size(); i++) {
				camAnmOffsets.get(i).setHere();
				AnimeUtil.writeCameraAnime(cameraAnime.get(i), dos);

				dos.pad(IF_PADDING);
			}

			for (int i = 0; i < lights.size(); i++) {
				lightOffsets.get(i).setHere();
				LightUtil.writeLight(lights.get(i), dos);

				dos.pad(IF_PADDING);
			}

			for (int i = 0; i < sceneTemplates.size(); i++) {
				sceneTemplateOffsets.get(i).setHere();
				SceneTemplateUtil.writeSceneTemplate(sceneTemplates.get(i), dos);

				dos.pad(IF_PADDING);
			}

			dos.close();
		} catch (IOException ex) {
			Logger.getLogger(CMIFFile.class.getName()).log(Level.SEVERE, null, ex);
		}
	}

	public void write(OutputStream out) {
		try {
			out.write(getBinaryData());
			out.close();
		} catch (IOException ex) {
			Logger.getLogger(CMIFFile.class.getName()).log(Level.SEVERE, null, ex);
		}
	}

	public void write(File f) {
		write(new DiskFile(f));
	}

	public void write(FSFile fsf) {
		handleDuplicateNames();
		write(fsf.getIO(), new CMIFWriteConfig());
	}

	public static void serializeStringTableHere(StringTable stringTable, DataIOStream dos) throws IOException {
		dos.writeInt(stringTable.getStringCount());

		stringTable.writeTable();
	}

	public static class OtherFile implements NamedResource {

		public String name;
		public byte[] data;

		public OtherFile() {

		}

		public OtherFile(String name, byte[] data) {
			this.name = name;
			this.data = data;
		}

		@Override
		public String getName() {
			return name;
		}

		@Override
		public void setName(String name) {
			this.name = name;
		}
	}

	public static class Level0Info {

		public DataIOStream io;
		public int fileVersion;
	}
}
