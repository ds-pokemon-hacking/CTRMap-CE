package ctrmap.formats.generic.source;

import ctrmap.renderer.scene.Scene;
import ctrmap.renderer.scene.animation.AbstractBoneTransform;
import ctrmap.renderer.scene.animation.AnimatedValue;
import xstandard.math.vec.Vec3f;
import ctrmap.renderer.scene.animation.KeyFrame;
import ctrmap.renderer.scene.animation.KeyFrameList;
import ctrmap.renderer.scene.animation.skeletal.SkeletalAnimation;
import ctrmap.renderer.scene.animation.skeletal.SkeletalAnimationFrame;
import ctrmap.renderer.scene.animation.skeletal.SkeletalAnimationTransformRequest;
import ctrmap.renderer.scene.animation.skeletal.SkeletalBoneTransform;
import ctrmap.renderer.scene.model.Joint;
import ctrmap.renderer.scene.model.Mesh;
import ctrmap.renderer.scene.model.Model;
import ctrmap.renderer.scene.model.PrimitiveType;
import ctrmap.renderer.scene.model.Skeleton;
import ctrmap.renderer.scene.model.Vertex;
import ctrmap.renderer.scene.texturing.Material;
import ctrmap.renderer.scene.texturing.TexEnvStage;
import ctrmap.renderer.scene.texturing.Texture;
import ctrmap.renderer.scene.texturing.TextureMapper;
import ctrmap.renderer.scenegraph.G3DResource;
import ctrmap.renderer.util.AnimeProcessor;
import ctrmap.renderer.util.MaterialProcessor;
import ctrmap.renderer.util.PrimitiveConverter;
import ctrmap.renderer.util.VBOProcessor;
import ctrmap.renderer.util.texture.TextureConverter;
import xstandard.fs.FSFile;
import xstandard.fs.FSUtil;
import xstandard.gui.file.CommonExtensionFilters;
import xstandard.gui.file.ExtensionFilter;
import xstandard.math.geom.Trianglef;
import xstandard.math.vec.Matrix4;
import xstandard.math.vec.Vec2f;
import xstandard.text.StringEx;
import xstandard.util.ArraysEx;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Scanner;

public class SMD {

	public static final ExtensionFilter EXTENSION_FILTER = new ExtensionFilter("Source Engine StudioMdl", "*.smd");

	public static final int SMD_MAX_COMPAT_VERSION = 1;

	public static final String SMD_CMD_VERSION = "version";
	public static final String SMD_CMD_NODEARR = "nodes";
	public static final String SMD_CMD_TRIANGLES = "triangles";
	public static final String SMD_CMD_ANIMEDATA = "skeleton";
	public static final String SMD_CMD_BEGINFRAME = "time";
	public static final String SMD_CMD_SECTIONEND = "end";

	public String name;

	public int frameCount = 0;

	public Map<Integer, Joint> skeleton = new HashMap<>();
	public Map<Integer, SkeletalBoneTransform> bones = new HashMap<>();
	public Map<String, List<Vertex>> triangles = new HashMap<>();
	public Map<String, Texture> materialTextures = new HashMap<>();

	public SMD(Skeleton skl, SkeletalAnimation anm) {
		name = anm.name;
		frameCount = (int) Math.ceil(anm.frameCount);

		int jidx = 0;
		for (Joint j : skl) {
			skeleton.put(jidx, j);
			SkeletalBoneTransform bt = (SkeletalBoneTransform) anm.getBoneTransform(j.name);
			if (bt != null) {
				bones.put(jidx, bt);
			}
			jidx++;
		}
	}

	public SMD(Model model) {
		this(ArraysEx.asList(model), null);
		name = model.name;
	}

	public SMD(List<Model> models, List<Texture> textureLib) {
		for (Model model : models) {
			if (skeleton.isEmpty()) {
				//only allow one skeleton
				int jidx = 0;
				for (Joint j : model.skeleton) {
					skeleton.put(jidx, j);
					SkeletalBoneTransform dummyBT = new SkeletalBoneTransform();
					dummyBT.pushFullBakedFrame(0f, j.position, j.rotation, j.scale);
					bones.put(jidx, dummyBT);
					jidx++;
				}
			}

			for (Mesh mesh : model.meshes) {
				Mesh orgMesh = mesh;

				mesh = PrimitiveConverter.getTriMesh(mesh);

				if (mesh.useIBO) {
					if (mesh == orgMesh) {
						mesh = new Mesh(mesh);
					}
					VBOProcessor.makeInline(mesh, false);
				}

				List<Vertex> vl = triangles.get(mesh.materialName);
				if (vl == null) {
					vl = new ArrayList<>();
					triangles.put(mesh.materialName, vl);
				}
				vl.addAll(mesh.vertices);

				if (textureLib != null) {
					Material mat = model.getMaterialByName(mesh.materialName);
					if (mat != null) {
						if (!mat.textures.isEmpty()) {
							Texture tex = Scene.getNamedObject(mat.textures.get(0).textureName, textureLib);
							if (tex != null) {
								materialTextures.put(mesh.materialName, tex);
							}
						}
					}
				}
			}
		}
	}

	public SMD(FSFile smd) {
		name = smd.getName();

		Scanner s = new Scanner(smd.getNativeInputStream());

		SMDCommand version = getCommand(s);

		if (!version.getCommand().equals(SMD_CMD_VERSION)) {
			throw new IllegalArgumentException("This is not a standard SMD animation file.");
		} else if (version.getArg(0).getInt() > SMD_MAX_COMPAT_VERSION) {
			throw new UnsupportedOperationException("Cannot parse SMD files with version > 1");
		}

		String section = null;

		int frame = 0;

		List<Vertex> currentVL = null;
		String currentMatName = null;

		List<? extends FSFile> siblings = smd.getParent().listFiles();
		List<String> siblingNames = new ArrayList<>();

		for (FSFile f : siblings) {
			siblingNames.add(f.getName());
		}

		HashSet<String> unloadableMaterials = new HashSet<>();

		//found node tree
		while (s.hasNextLine()) {
			SMDCommand cmd = getCommand(s);

			String cmdStr = cmd.getCommand();

			switch (cmdStr) {
				case SMD_CMD_SECTIONEND:
					section = null;
					continue;
			}

			if (section != null) {
				switch (section) {
					case SMD_CMD_NODEARR: {
						int boneIndex = Integer.parseInt(cmd.getCommand());

						String boneName = cmd.getArg(0).getStr();
						int parentIndex = cmd.getArg(1).getInt();

						Joint j = new Joint();
						j.name = boneName;
						Joint parent = skeleton.get(parentIndex);
						j.parentName = parent != null ? parent.name : null;

						skeleton.put(boneIndex, j);

						SkeletalBoneTransform bt = new SkeletalBoneTransform();
						bt.name = boneName;
						bones.put(boneIndex, bt);
						break;
					}
					case SMD_CMD_ANIMEDATA: {
						if (cmdStr.equals(SMD_CMD_BEGINFRAME)) {
							frame = cmd.getArg(0).getInt();
						} else {
							int boneIndex = Integer.parseInt(cmd.getCommand());

							SkeletalBoneTransform bt = bones.get(boneIndex);

							bt.rx.add(new KeyFrame(frame, cmd.getArg(3).getFloat()));
							bt.ry.add(new KeyFrame(frame, cmd.getArg(4).getFloat()));
							bt.rz.add(new KeyFrame(frame, cmd.getArg(5).getFloat()));

							bt.tx.add(new KeyFrame(frame, cmd.getArg(0).getFloat()));
							bt.ty.add(new KeyFrame(frame, cmd.getArg(1).getFloat()));
							bt.tz.add(new KeyFrame(frame, cmd.getArg(2).getFloat()));
						}
						break;
					}
					case SMD_CMD_TRIANGLES: {
						if (!cmd.args.isEmpty()) {
							//vertex data
							if (currentVL != null) {
								int parentBoneIdx = Integer.parseInt(cmdStr);

								Vertex vtx = new Vertex();
								vtx.position.x = cmd.getArg(0).getFloat();
								vtx.position.y = cmd.getArg(1).getFloat();
								vtx.position.z = cmd.getArg(2).getFloat();

								vtx.normal = new Vec3f();
								vtx.normal.x = cmd.getArg(3).getFloat();
								vtx.normal.y = cmd.getArg(4).getFloat();
								vtx.normal.z = cmd.getArg(5).getFloat();

								vtx.uv[0] = new Vec2f(cmd.getArg(6).getFloat(), cmd.getArg(7).getFloat());

								if (cmd.args.size() >= 9) {
									int linkCount = cmd.getArg(8).getInt();
									float weightSum = 0f;
									for (int i = 0; i < linkCount; i++) {
										vtx.boneIndices.add(cmd.getArg(9 + i * 2).getInt());
										float weight = cmd.getArg(10 + i * 2).getFloat();
										weightSum += weight;
										vtx.weights.add(weight);
									}
									if (1f - weightSum > 0.001f) {
										vtx.boneIndices.add(parentBoneIdx);
										vtx.weights.add(1f - weightSum);
									}
								}

								currentVL.add(vtx);
							}
						} else {
							//material name
							String matName = cmdStr;
							if (!Objects.equals(matName, currentMatName)) {
								currentMatName = matName;
								currentVL = triangles.get(matName);
								if (currentVL == null) {
									currentVL = new ArrayList<>();
									triangles.put(matName, currentVL);
								}
							}

							Texture tex = materialTextures.get(matName);
							if (tex == null) {
								if (!unloadableMaterials.contains(matName)) {
									for (String fileName : siblingNames) {
										if (fileName.equals(matName) || FSUtil.getFileNameWithoutExtension(fileName).equals(matName)) {
											try {
												tex = TextureConverter.readTextureFromFile(siblings.get(siblingNames.indexOf(fileName)));
											} catch (Exception ex) {

											}

											break;
										}
									}
									if (tex == null) {
										for (String fileName : siblingNames) {
											if (fileName.startsWith(matName)) {
												try {
													tex = TextureConverter.readTextureFromFile(siblings.get(siblingNames.indexOf(fileName)));
												} catch (Exception ex) {

												}

												break;
											}
										}
									}
									if (tex != null) {
										materialTextures.put(matName, tex);
									} else {
										unloadableMaterials.add(matName);
									}
								}
							}
						}
						break;
					}
				}
			} else {
				switch (cmdStr) {
					case SMD_CMD_TRIANGLES:
					case SMD_CMD_NODEARR:
					case SMD_CMD_ANIMEDATA:
						section = cmdStr;
						break;
					default:
						System.err.println("Unknown SMD command: " + cmdStr);
						break;
				}
			}
		}

		if (frame == 0 && !triangles.isEmpty()) {
			//bind pose
			for (Map.Entry<Integer, Joint> e : skeleton.entrySet()) {
				SkeletalBoneTransform bt = bones.get(e.getKey());
				Joint apply = e.getValue();
				SkeletalAnimationFrame frm = bt.getFrame(0);

				apply.position = new Vec3f(frm.tx.value, frm.ty.value, frm.tz.value);
				apply.rotation = new Vec3f(frm.rx.value, frm.ry.value, frm.rz.value);
			}
		}

		frameCount = Math.max(1, frame);
		s.close();
	}

	public boolean hasModel() {
		return !triangles.isEmpty();
	}

	public boolean hasAnimation() {
		return !bones.isEmpty() && !hasModel();
	}

	public G3DResource toGeneric() {
		G3DResource res = new G3DResource();

		if (hasModel()) {
			Model mdl = new Model();
			mdl.name = name;

			mdl.skeleton = getGenericSkeleton();

			for (Map.Entry<String, List<Vertex>> vertices : triangles.entrySet()) {
				Mesh mesh = new Mesh();
				mesh.materialName = vertices.getKey();
				mesh.name = FSUtil.getFileNameWithoutExtension(mesh.materialName) + "_mesh";
				mesh.vertices.addAll(vertices.getValue());
				mesh.primitiveType = PrimitiveType.TRIS;
				mesh.hasNormal = true;
				mesh.hasUV[0] = true;

				for (Vertex v : vertices.getValue()) {
					if (!v.boneIndices.isEmpty()) {
						mesh.hasBoneIndices = true;
						mesh.hasBoneWeights = true;
						mesh.skinningType = Mesh.SkinningType.SMOOTH;
						break;
					}
				}

				mdl.addMesh(mesh);

				Material mat = mdl.getMaterialByName(mesh.materialName);

				if (mat == null) {
					mat = new Material();
					mat.name = mesh.materialName;
					mat.tevStages.stages[0] = new TexEnvStage(TexEnvStage.TexEnvTemplate.TEX0);
					MaterialProcessor.enableFragmentLighting(mat);

					Texture tex = materialTextures.get(mat.name);
					if (tex != null) {
						mat.textures.add(new TextureMapper(tex.name));

						if (!res.textures.contains(tex)) {
							res.addTexture(tex);
						}
					}

					mdl.addMaterial(mat);
				}
			}
			res.addModel(mdl);
			MaterialProcessor.setAutoAlphaBlendByTexture(res);
		}
		if (hasAnimation()) {
			res.addSklAnime(toGenericAnimation());
		}

		return res;
	}

	public SkeletalAnimation toGenericAnimation() {
		SkeletalAnimation a = new SkeletalAnimation();

		a.frameCount = frameCount;
		a.isLooped = false;
		a.name = name;

		a.bones.addAll(bones.values());

		AnimeProcessor.optimizeSkeletalAnimation(a, true);
		return a;
	}

	public void writeToFile(FSFile fsf) {
		writeToFile(fsf, null);
	}

	public void writeToFile(FSFile fsf, FSFile textureDir) {
		PrintStream out = new PrintStream(fsf.getNativeOutputStream());

		printCmd(out, SMD_CMD_VERSION, SMD_MAX_COMPAT_VERSION);

		printCmd(out, SMD_CMD_NODEARR);

		List<Map.Entry<Integer, Joint>> skeletonSorted = new ArrayList<>(skeleton.entrySet());
		skeletonSorted.sort(Map.Entry.comparingByKey());
		List<Map.Entry<Integer, SkeletalBoneTransform>> transformsSorted = new ArrayList<>(bones.entrySet());
		transformsSorted.sort(Map.Entry.comparingByKey());

		for (Map.Entry<Integer, Joint> e : skeletonSorted) {
			Joint j = e.getValue();
			int pidx = j.parentSkeleton.getJointIndex(j.parentName);
			printCmd(out, e.getKey(), '"' + j.name + '"', pidx);
		}

		printCmd(out, SMD_CMD_SECTIONEND);

		printCmd(out, SMD_CMD_ANIMEDATA);

		Vec3f t = new Vec3f();
		Vec3f r = new Vec3f();

		SkeletalAnimationTransformRequest rotReq = new SkeletalAnimationTransformRequest(0);
		rotReq.scale = false;
		rotReq.translation = false;

		for (SkeletalBoneTransform bt : bones.values()) {
			if (KeyFrameList.existAnyRounddownFrame(frameCount, bt.tx, bt.ty, bt.tz, bt.rx, bt.ry, bt.rz)) {
				frameCount++;
				break;
			}
		}

		for (int frame = 0; frame < frameCount; frame++) {
			printCmd(out, SMD_CMD_BEGINFRAME, frame);

			for (Map.Entry<Integer, SkeletalBoneTransform> bte : transformsSorted) {
				int index = bte.getKey();
				SkeletalBoneTransform bt = bte.getValue();
				Joint bindJnt = skeleton.get(index);

				boolean existTrans = KeyFrameList.existAnyRounddownFrame(frame, bt.tx, bt.ty, bt.tz);
				boolean existRot = KeyFrameList.existAnyRounddownFrame(frame, bt.rx, bt.ry, bt.rz);

				if (existTrans || existRot) {
					if (existTrans) {
						AnimatedValue tx = AbstractBoneTransform.getValueAt(bt.tx, frame);
						AnimatedValue ty = AbstractBoneTransform.getValueAt(bt.ty, frame);
						AnimatedValue tz = AbstractBoneTransform.getValueAt(bt.tz, frame);
						t.x = tx.exists ? tx.value : bindJnt.position.x;
						t.y = ty.exists ? ty.value : bindJnt.position.y;
						t.z = tz.exists ? tz.value : bindJnt.position.z;
					} else {
						t.set(bindJnt.position);
					}
					if (existRot) {
						rotReq.frame = frame;
						rotReq.bindJoint = bindJnt;

						Matrix4 matrix = bt.getTransformMatrix(rotReq);
						matrix.getRotationTo(r);
					} else {
						r.set(bindJnt.rotation);
					}

					printCmd(out, index, t.x, t.y, t.z, r.x, r.y, r.z);
				}
			}
		}

		printCmd(out, SMD_CMD_SECTIONEND);

		if (hasModel()) {
			printCmd(out, SMD_CMD_TRIANGLES);

			Trianglef triangleTemp = new Trianglef();

			Vec3f dmyNormal = new Vec3f();
			Vec2f dmyUV = new Vec2f();

			for (Map.Entry<String, List<Vertex>> mesh : triangles.entrySet()) {
				List<Vertex> verts = mesh.getValue();
				String materialName = mesh.getKey();

				Texture tex = materialTextures.get(materialName);

				if (!materialName.endsWith(CommonExtensionFilters.PNG.getPrimaryExtension())) {
					materialName = FSUtil.getFileNameWithoutExtension(materialName) + CommonExtensionFilters.PNG.getPrimaryExtension();
				}

				if (tex != null && textureDir != null) {
					FSFile texDest = textureDir.getChild(materialName);
					if (!texDest.isDirectory()) {
						TextureConverter.writeTextureToFile(texDest, "png", tex);
					}
				}

				for (int i = 0; i < verts.size(); i += 3) {
					printCmd(out, materialName);

					triangleTemp.setPoint(0, verts.get(i + 0).position);
					triangleTemp.setPoint(1, verts.get(i + 1).position);
					triangleTemp.setPoint(2, verts.get(i + 2).position);

					for (int vIdx = 0; vIdx < 3; vIdx++) {
						Vertex vtx = verts.get(i + vIdx);

						Vec3f normal = vtx.normal == null ? triangleTemp.normal(dmyNormal) : vtx.normal;
						Vec2f uv = vtx.uv[0] == null ? dmyUV : vtx.uv[0];

						int weightCount = vtx.getActiveWeightCount();

						Object[] cmdParams = new Object[9 + weightCount * 2];

						cmdParams[0] = vtx.position.x;
						cmdParams[1] = vtx.position.y;
						cmdParams[2] = vtx.position.z;

						cmdParams[3] = normal.x;
						cmdParams[4] = normal.y;
						cmdParams[5] = normal.z;

						cmdParams[6] = uv.x;
						cmdParams[7] = uv.y;

						cmdParams[8] = weightCount;

						float weightSum = 0f;

						for (int bIdx = 0, cmdIdx = 9; bIdx < weightCount; bIdx++, cmdIdx += 2) {
							cmdParams[cmdIdx + 0] = vtx.boneIndices.get(bIdx);
							float weight = bIdx < vtx.weights.size() ? vtx.weights.get(bIdx) : (1f - weightSum) / (weightCount - vtx.weights.size());
							cmdParams[cmdIdx + 1] = weight;
							weightSum += weight;
						}

						printCmd(out, 0, cmdParams);
					}
				}
			}

			printCmd(out, SMD_CMD_SECTIONEND);
		}

		out.close();
	}

	private static void printCmd(PrintStream strm, Object cmd, Object... args) {
		strm.print(cmd);
		for (Object arg : args) {
			strm.print(" ");
			strm.print(makeSafeString(String.valueOf(arg)));
		}
		strm.println();
	}

	private static String makeSafeString(String str) {
		if (str.indexOf(' ') != -1) {
			return '"' + str + '"';
		}
		return str;
	}

	public Skeleton getGenericSkeleton() {
		Skeleton s = new Skeleton();

		int jidxMax = -1;

		for (int jidx : skeleton.keySet()) {
			if (jidx > jidxMax) {
				jidxMax = jidx;
			}
		}

		for (int i = 0; i <= jidxMax; i++) {
			Joint j = skeleton.get(i);
			if (j == null) {
				j = new Joint();
				j.name = "Joint_" + i;
			}
			s.addJoint(j);
		}

		return s;
	}

	private SMDCommand getCommand(Scanner s) {
		return new SMDCommand(s.nextLine());
	}

	private static class SMDCommand {

		private String cmd;
		private List<Argument> args = new ArrayList<>();

		public SMDCommand(String line) {
			String[] src = StringEx.splitOnecharFastNoBlank(line, ' ', '"');
			cmd = src[0];
			for (int i = 1; i < src.length; i++) {
				if (src[i].length() > 0) {
					args.add(new Argument(src[i]));
				}
			}
		}

		public String getCommand() {
			return cmd;
		}

		public Argument getArg(int num) {
			return args.get(num);
		}

		private static class Argument {

			private final String value;

			public Argument(String s) {
				value = s;
			}

			public String getStr() {
				return value;
			}

			public int getInt() {
				return Integer.parseInt(value);
			}

			public float getFloat() {
				return Float.parseFloat(value);
			}

			public float getRotationDeg() {
				return (float) Math.toDegrees(getFloat());
			}

			@Override
			public String toString() {
				return getStr();
			}
		}
	}
}
