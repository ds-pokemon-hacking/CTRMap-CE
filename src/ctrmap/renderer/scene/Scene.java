package ctrmap.renderer.scene;

import xstandard.INamed;
import ctrmap.renderer.scenegraph.G3DResource;
import ctrmap.renderer.scenegraph.NamedResource;
import ctrmap.renderer.scenegraph.G3DResourceInstance;
import ctrmap.renderer.scene.texturing.Texture;
import ctrmap.renderer.scene.model.ModelInstance;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Scene extends G3DResourceInstance implements NamedResource, Cloneable{

	public String name;

	public Scene(String n) {
		name = n;
		resource = new G3DResource();
	}

	public void addScene(Scene s) {
		addChild(s);
	}

	public void addModel(ModelInstance m) {
		addChild(m);
	}

	public void addTexture(Texture t) {
		resource.addTexture(t);
	}
	
	public void addTextures(Collection<Texture> textures){
		resource.addTextures(textures);
	}

	public static <T extends INamed> T getNamedObject(String name, List<T> objects) {
		if (name == null) {
			return null;
		}
		for (T o : objects) {
			if (name.equals(o.getName())) {
				return o;
			}
		}
		return null;
	}

	public static void cleanNamedDuplicates(List<? extends INamed> l) {
		List<String> processed = new ArrayList<>();
		for (int i = 0; i < l.size(); i++) {
			if (!processed.contains(l.get(i).getName())) {
				processed.add(l.get(i).getName());
			} else {
				l.remove(i);
				i--;
			}
		}
	}

	@Override
	public Scene clone() {
		try {
			super.clone();
			Scene ret = new Scene(name);
			ret.resource.merge(resource);
			return ret;
		} catch (CloneNotSupportedException ex) {
			Logger.getLogger(Scene.class.getName()).log(Level.SEVERE, null, ex);
			return null;
		}
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public void setName(String name) {
		this.name = name;
	}
	
	@Override
	public String toString() {
		return getName();
	}
}
