package ctrmap.renderer.scenegraph;

import ctrmap.renderer.scene.Camera;
import ctrmap.renderer.scene.Light;
import xstandard.math.vec.Vec3f;
import xstandard.INamed;
import ctrmap.renderer.scene.Scene;
import ctrmap.renderer.scene.animation.AbstractAnimation;
import ctrmap.renderer.scene.animation.camera.CameraAnimation;
import ctrmap.renderer.scene.animation.material.MaterialAnimation;
import ctrmap.renderer.scene.animation.skeletal.SkeletalAnimation;
import ctrmap.renderer.scene.animation.visibility.VisibilityAnimation;
import ctrmap.renderer.scene.metadata.MetaData;
import ctrmap.renderer.scene.model.Mesh;
import ctrmap.renderer.scene.model.MeshVisibilityGroup;
import ctrmap.renderer.scene.model.Model;
import ctrmap.renderer.scene.model.ModelInstance;
import ctrmap.renderer.scene.model.Vertex;
import ctrmap.renderer.scene.texturing.Material;
import ctrmap.renderer.scene.texturing.Texture;
import ctrmap.renderer.scene.texturing.TextureMapper;
import xstandard.util.ListenableList;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;

public class G3DResource {

	public ListenableList<Model> models = new ListenableList<>();
	public ListenableList<Texture> textures = new ListenableList<>();
	public ListenableList<Camera> cameras = new ListenableList<>();
	public ListenableList<Light> lights = new ListenableList<>();

	public ListenableList<MaterialAnimation> materialAnimations = new ListenableList<>();
	public ListenableList<SkeletalAnimation> skeletalAnimations = new ListenableList<>();
	public ListenableList<VisibilityAnimation> visibilityAnimations = new ListenableList<>();
	public ListenableList<CameraAnimation> cameraAnimations = new ListenableList<>();

	public ListenableList<G3DSceneTemplate> sceneTemplates = new ListenableList<>();

	public Vec3f maxVector = new Vec3f();
	public Vec3f minVector = new Vec3f();

	public MetaData metaData = new MetaData();

	public G3DResource() {

	}

		
	public String getModelName() {
		if (!models.isEmpty()) {
			return models.get(0).name;
		}
		return "<none>";
	}
	
	public List<NamedResource> getAllResources() {
		List<NamedResource> l = new ArrayList<>();
		l.addAll(models);
		l.addAll(textures);
		l.addAll(cameras);
		l.addAll(lights);
		l.addAll(materialAnimations);
		l.addAll(skeletalAnimations);
		l.addAll(visibilityAnimations);
		l.addAll(cameraAnimations);
		l.addAll(sceneTemplates);
		return l;
	}

	public List<Vertex> getCollectiveVertices() {
		List<Vertex> l = new ArrayList<>();
		for (Model mdl : models) {
			l.addAll(mdl.getCollectiveVertices());
		}
		return l;
	}

	public Vec3f getCenterVector() {
		Vec3f result = minVector.clone();
		result.add(maxVector).mul(0.5f);
		return result;
	}
	
	public Vec3f getDimVector() {
		return new Vec3f(maxVector.x - minVector.x, maxVector.y - minVector.y, maxVector.z - minVector.z);
	}

	public void updateBBox() {
		updateBBox(true);
	}

	public void updateBBox(boolean rebuildSubs) {
		if (!models.isEmpty()) {
			minVector = null;
			maxVector = null;

			for (Model m : models) {
				if (rebuildSubs) {
					m.genBbox();
				}
				if (maxVector == null) {
					maxVector = m.maxVector;
				}
				if (minVector == null) {
					minVector = m.minVector;
				}
				maxVector.x = Math.max(m.maxVector.x, maxVector.x);
				maxVector.y = Math.max(m.maxVector.y, maxVector.y);
				maxVector.z = Math.max(m.maxVector.z, maxVector.z);
				minVector.x = Math.min(m.minVector.x, minVector.x);
				minVector.y = Math.min(m.minVector.y, minVector.y);
				minVector.z = Math.min(m.minVector.z, minVector.z);
			}
		} else {
			minVector = new Vec3f();
			maxVector = new Vec3f();
		}
	}

	public G3DResource(NamedResource... sources) {
		for (NamedResource r : sources) {
			addResource(r);
		}
	}

	public void addResource(NamedResource r) {
		if (r != null) {
			if (r instanceof Model) {
				addModel((Model) r);
			} else if (r instanceof Texture) {
				addTexture((Texture) r);
			} else if (r instanceof MaterialAnimation) {
				addMatAnime((MaterialAnimation) r);
			} else if (r instanceof SkeletalAnimation) {
				addSklAnime((SkeletalAnimation) r);
			} else if (r instanceof VisibilityAnimation) {
				addVisAnime((VisibilityAnimation) r);
			} else if (r instanceof CameraAnimation) {
				addCamAnime((CameraAnimation) r);
			} else if (r instanceof Light) {
				addLight((Light) r);
			} else if (r instanceof Camera) {
				addCamera((Camera) r);
			}
		}
	}

	public ModelInstance createInstance() {
		ModelInstance i = new ModelInstance();
		i.setResource(this);
		return i;
	}

	public List<G3DResourceInstance> unpackAllSceneTemplates() {
		List<G3DResourceInstance> list = new ArrayList<>();
		for (G3DSceneTemplate temp : sceneTemplates) {
			G3DResourceInstance i = unpackSceneTemplate(temp);
			if (i != null) {
				list.add(i);
			}
		}
		return list;
	}

	public G3DResourceInstance unpackSceneTemplate(G3DSceneTemplate template) {
		if (template != null) {
			return template.createScene(this);
		}
		return null;
	}
	
	public boolean isTextureUsed(String name) {
		for (Material mat : materials()) {
			for (TextureMapper m : mat.textures) {
				if (Objects.equals(m.textureName, name)) {
					return true;
				}
			}
		}
		return false;
	}

	public List<AbstractAnimation> getAnimations() {
		List<AbstractAnimation> l = new ArrayList<>();
		l.addAll(materialAnimations);
		l.addAll(skeletalAnimations);
		l.addAll(cameraAnimations);
		l.addAll(visibilityAnimations);
		return l;
	}

	public AbstractAnimation[] getAnimationsArray() {
		List<AbstractAnimation> l = getAnimations();
		return l.toArray(new AbstractAnimation[l.size()]);
	}

	public Iterable<Material> materials() {
		return new Iterable<Material>() {
			@Override
			public Iterator<Material> iterator() {
				return new Iterator<Material>() {

					final Iterator<Model> mdlIt = models.iterator();
					Iterator<Material> matIt = null;

					@Override
					public boolean hasNext() {
						if (matIt == null || !matIt.hasNext()) {
							if (mdlIt.hasNext()) {
								matIt = mdlIt.next().materials.iterator();
							}
							else {
								return false;
							}
						}
						return matIt.hasNext();
					}

					@Override
					public Material next() {
						return matIt.next();
					}
				};
			}
		};
	}

	public Iterable<Mesh> meshes() {
		return new Iterable<Mesh>() {
			@Override
			public Iterator<Mesh> iterator() {
				return new Iterator<Mesh>() {

					final Iterator<Model> mdlIt = models.iterator();
					Iterator<Mesh> meshIt = null;

					@Override
					public boolean hasNext() {
						if (meshIt == null || !meshIt.hasNext()) {
							if (mdlIt.hasNext()) {
								meshIt = mdlIt.next().meshes.iterator();
							}
							else {
								return false;
							}
						}
						return meshIt.hasNext();
					}

					@Override
					public Mesh next() {
						return meshIt.next();
					}
				};
			}
		};
	}
	
	public Iterable<MeshVisibilityGroup> visGroups() {
		return new Iterable<MeshVisibilityGroup>() {
			@Override
			public Iterator<MeshVisibilityGroup> iterator() {
				return new Iterator<MeshVisibilityGroup>() {

					final Iterator<Model> mdlIt = models.iterator();
					Iterator<MeshVisibilityGroup> visgroupIt = null;

					@Override
					public boolean hasNext() {
						if (visgroupIt == null || !visgroupIt.hasNext()) {
							if (mdlIt.hasNext()) {
								visgroupIt = mdlIt.next().visGroups.iterator();
							}
							else {
								return false;
							}
						}
						return visgroupIt.hasNext();
					}

					@Override
					public MeshVisibilityGroup next() {
						return visgroupIt.next();
					}
				};
			}
		};
	}

	public void addTextures(Collection<Texture> tex) {
		for (Texture t : tex) {
			addTexture(t);
		}
	}

	public void addModels(Collection<Model> models) {
		for (Model m : models) {
			addModel(m);
		}
	}

	public void addLights(Collection<? extends Light> lights) {
		for (Light l : lights) {
			addLight(l);
		}
	}

	public void addSceneTemplates(Collection<? extends G3DSceneTemplate> templates) {
		for (G3DSceneTemplate l : templates) {
			addSceneTemplate(l);
		}
	}

	public void addMatAnimes(Collection<MaterialAnimation> animes) {
		for (MaterialAnimation a : animes) {
			addMatAnime(a);
		}
	}

	public void addSklAnimes(Collection<SkeletalAnimation> animes) {
		for (SkeletalAnimation a : animes) {
			addSklAnime(a);
		}
	}

	public void addVisAnimes(Collection<VisibilityAnimation> animes) {
		for (VisibilityAnimation a : animes) {
			addVisAnime(a);
		}
	}

	public void addCamAnimes(Collection<CameraAnimation> animes) {
		for (CameraAnimation a : animes) {
			addCamAnime(a);
		}
	}

	public void addCameras(Collection<Camera> cams) {
		for (Camera cam : cams) {
			addCamera(cam);
		}
	}

	public void addAnimes(Collection<? extends AbstractAnimation> animes) {
		if (animes != null) {
			for (AbstractAnimation a : animes) {
				addAnime(a);
			}
		}
	}

	public void addAnime(AbstractAnimation anm) {
		if (anm != null) {
			if (anm instanceof SkeletalAnimation) {
				addSklAnime((SkeletalAnimation) anm);
			} else if (anm instanceof MaterialAnimation) {
				addMatAnime((MaterialAnimation) anm);
			} else if (anm instanceof VisibilityAnimation) {
				addVisAnime((VisibilityAnimation) anm);
			} else if (anm instanceof CameraAnimation) {
				addCamAnime((CameraAnimation) anm);
			}
		}
	}

	public void addTexture(Texture t) {
		if (t != null) {
			removeOldINamed(t, textures);
			textures.add(t);
		}
	}

	public void addLight(Light l) {
		if (l != null) {
			removeOldINamed(l, lights);
			lights.add(l);
		}
	}

	public void addSceneTemplate(G3DSceneTemplate template) {
		if (template != null) {
			removeOldINamed(template, sceneTemplates);
			sceneTemplates.add(template);
		}
	}

	public void addModel(Model m) {
		if (m != null) {
			removeOldINamed(m, models);
			models.add(m);
		}
		updateBBox(false);
	}

	public void addMatAnime(MaterialAnimation m) {
		if (m != null) {
			removeOldINamed(m, materialAnimations);
			materialAnimations.add(m);
		}
	}

	public void addVisAnime(VisibilityAnimation v) {
		if (v != null) {
			removeOldINamed(v, visibilityAnimations);
			visibilityAnimations.add(v);
		}
	}

	public void addCamAnime(CameraAnimation c) {
		if (c != null) {
			removeOldINamed(c, cameraAnimations);
			cameraAnimations.add(c);
		}
	}

	public void addSklAnime(SkeletalAnimation s) {
		if (s != null) {
			removeOldINamed(s, skeletalAnimations);
			skeletalAnimations.add(s);
		}
	}

	public void addCamera(Camera cam) {
		if (cam != null) {
			removeOldINamed(cam, cameras);
			cameras.add(cam);
		}
	}

	public void merge(G3DResource res) {
		if (res != null) {
			addTextures(res.textures);
			addMatAnimes(res.materialAnimations);
			addSklAnimes(res.skeletalAnimations);
			addVisAnimes(res.visibilityAnimations);
			addCameras(res.cameras);
			addModels(res.models);
			addLights(res.lights);
			addSceneTemplates(res.sceneTemplates);
			metaData.putValues(res.metaData.getValues());
		}
	}

	public void unmerge(G3DResource res) {
		if (res != null) {
			textures.removeAll(res.textures);
			models.removeAll(res.models);
			cameras.removeAll(res.cameras);
			cameraAnimations.removeAll(res.cameraAnimations);
			skeletalAnimations.removeAll(res.skeletalAnimations);
			materialAnimations.removeAll(res.materialAnimations);
			visibilityAnimations.removeAll(res.visibilityAnimations);
			lights.removeAll(res.lights);
			sceneTemplates.removeAll(res.sceneTemplates);
		}
	}

	public void mergeFull(G3DResource res) {
		if (res != null) {
			addListPrededupe(models, res.models, "Texture");
			addListPrededupe(textures, res.textures, "Model");
			addListPrededupe(cameras, res.cameras, "Camera");
			addListPrededupe(lights, res.lights, "Light");
			addListPrededupe(skeletalAnimations, res.skeletalAnimations, "SkeletalAnimation");
			addListPrededupe(materialAnimations, res.materialAnimations, "MaterialAnimation");
			addListPrededupe(visibilityAnimations, res.visibilityAnimations, "VisibilityAnimation");
			addListPrededupe(cameraAnimations, res.cameraAnimations, "CameraAnimation");
			addListPrededupe(sceneTemplates, res.sceneTemplates, "SceneTemplate");
			metaData.putValues(res.metaData.getValues());
		}
	}

	public static <T extends NamedResource> void addListPrededupe(List<T> dest, List<T> source, String defaultName) {
		HashSet<String> usedNames = new HashSet<>();
		for (T exist : dest) {
			usedNames.add(exist.getName());
		}
		for (T elem : source) {
			if (elem.getName() == null) {
				elem.setName(defaultName);
			}

			String base = elem.getName();

			String name = base;

			int index = 1;
			while (usedNames.contains(name)) {
				name = base + "_" + index;
				index++;
			}
			if (!base.equals(name)) {
				elem.setName(name);
			}

			usedNames.add(name);
			dest.add(elem);
		}
	}

	public void renameAllDuplicates() {
		renameDuplicates(models, "Model");
		renameDuplicates(lights, "Light");
		renameDuplicates(textures, "Texture");
		renameDuplicates(materialAnimations, "MaterialAnimation");
		renameDuplicates(skeletalAnimations, "SkeletalAnimation");
		renameDuplicates(visibilityAnimations, "VisibilityAnimation");
		renameDuplicates(cameraAnimations, "CameraAnimation");
		renameDuplicates(cameras, "Camera");
		renameDuplicates(sceneTemplates, "SceneTemplate");
	}

	public static void renameDuplicates(List<? extends NamedResource> l, String defName) {
		for (int i = 0; i < l.size(); i++) {
			NamedResource a = l.get(i);
			if (a == null) {
				l.remove(a);
				i--;
				continue;
			}
			if (l.indexOf(a) != l.lastIndexOf(a)) {
				l.remove(l.lastIndexOf(a));
			}
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

	public void clear() {
		textures.clear();
		models.clear();
		materialAnimations.clear();
		skeletalAnimations.clear();
		visibilityAnimations.clear();
		cameraAnimations.clear();
		cameras.clear();
		lights.clear();
		sceneTemplates.clear();
	}

	public NamedResource getNamedResource(String name, G3DResourceType t) {
		List l = null;
		switch (t) {
			case ANIME_M:
				l = materialAnimations;
				break;
			case ANIME_S:
				l = skeletalAnimations;
				break;
			case ANIME_V:
				l = visibilityAnimations;
				break;
			case MODEL:
				l = models;
				break;
			case TEXTURE:
				l = textures;
				break;
			case CAMERA:
				l = cameras;
				break;
			case ANIME_CAM:
				l = cameraAnimations;
				break;
			case SCENE_TEMPLATE:
				l = sceneTemplates;
				break;
			case LIGHT:
				l = lights;
				break;
		}
		if (l != null) {
			return (NamedResource) Scene.getNamedObject(name, l);
		}
		return null;
	}

	private static void removeOldINamed(INamed in, List<? extends INamed> l) {
		if (in != null) {
			INamed old = Scene.getNamedObject(in.getName(), l);
			if (old != null) {
				l.remove(old);
			}
		}
	}
}
