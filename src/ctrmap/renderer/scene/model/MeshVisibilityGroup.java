
package ctrmap.renderer.scene.model;

import ctrmap.renderer.scenegraph.NamedResource;

public class MeshVisibilityGroup implements NamedResource {
	public boolean isVisible = true;
	public String name;
	
	public MeshVisibilityGroup(String name) {
		this.name = name;
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
