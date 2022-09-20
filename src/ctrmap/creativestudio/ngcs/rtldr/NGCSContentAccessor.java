package ctrmap.creativestudio.ngcs.rtldr;

import ctrmap.formats.generic.interchange.CMIFFile;
import ctrmap.renderer.scene.Camera;
import ctrmap.renderer.scene.Light;
import ctrmap.renderer.scene.animation.AbstractAnimation;
import ctrmap.renderer.scene.animation.camera.CameraAnimation;
import ctrmap.renderer.scene.animation.material.MaterialAnimation;
import ctrmap.renderer.scene.animation.skeletal.SkeletalAnimation;
import ctrmap.renderer.scene.animation.visibility.VisibilityAnimation;
import ctrmap.renderer.scene.model.Model;
import ctrmap.renderer.scene.texturing.Texture;
import ctrmap.renderer.scenegraph.G3DResource;
import ctrmap.renderer.scenegraph.G3DSceneTemplate;
import xstandard.fs.FSFile;
import xstandard.util.ListenableList;
import java.util.List;

public interface NGCSContentAccessor {

	public void importFile(FSFile fsf);

	public void importResource(G3DResource res);
	
	public G3DResource getResource();

	public ListenableList<Model> getModels();

	public ListenableList<Texture> getTextures();

	public ListenableList<Light> getLights();

	public ListenableList<CMIFFile.OtherFile> getOthers();

	public List<AbstractAnimation> getAllAnimations();

	public ListenableList<MaterialAnimation> getMatAnime();

	public ListenableList<SkeletalAnimation> getSklAnime();

	public ListenableList<VisibilityAnimation> getVisAnime();

	public ListenableList<CameraAnimation> getCamAnime();

	public ListenableList<Camera> getCameras();

	public ListenableList<G3DSceneTemplate> getSceneTemplates();
	
	public Model getSupplementaryModelForExport(boolean skelOnly);	
}
