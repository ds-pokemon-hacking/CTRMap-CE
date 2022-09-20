package ctrmap.renderer.scene;

import ctrmap.renderer.scene.metadata.MetaData;
import ctrmap.renderer.scenegraph.NamedResource;
import xstandard.math.vec.RGBA;
import xstandard.math.vec.Vec3f;
import java.util.Objects;

public class Light implements NamedResource{
	
	public String name = "Light";
	public int setIndex = 0;
	
	public boolean directional = true;
	public Vec3f position = new Vec3f();
	public Vec3f direction = new Vec3f(0f, -1f, 0f);
	public RGBA ambientColor = new RGBA(RGBA.WHITE);
	public RGBA diffuseColor = new RGBA(RGBA.WHITE);
	public RGBA specular0Color = new RGBA(RGBA.BLACK);
	public RGBA specular1Color = new RGBA(RGBA.BLACK);
	
	public MetaData metaData = new MetaData();
	
	public Light(String name){
		setName(name);
	}

	public Light(Light l){
		loadValues(l);
	}
	
	public void setDirectionByOriginIllumPosition(Vec3f pos) {
		direction.set(pos);
		direction.invert();
		direction.normalize();
	}
	
	@Override
	public String toString() {
		return name + "@" + setIndex;
	}
	
	public void loadValues(Light l){
		name = l.name;
		setIndex = l.setIndex;
		position = new Vec3f(l.position);
		direction = new Vec3f(l.direction);
		ambientColor = new RGBA(l.ambientColor);
		diffuseColor = new RGBA(l.diffuseColor);
		specular0Color = new RGBA(l.specular0Color);
		specular1Color = new RGBA(l.specular1Color);
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
	public int hashCode() {
		int hash = 3;
		hash = 79 * hash + this.setIndex;
		hash = 79 * hash + (this.directional ? 1 : 0);
		hash = 79 * hash + Objects.hashCode(this.position);
		hash = 79 * hash + Objects.hashCode(this.direction);
		hash = 79 * hash + Objects.hashCode(this.ambientColor);
		hash = 79 * hash + Objects.hashCode(this.diffuseColor);
		hash = 79 * hash + Objects.hashCode(this.specular0Color);
		hash = 79 * hash + Objects.hashCode(this.specular1Color);
		return hash;
	}
}
