package ctrmap.formats.generic.collada.structs;

import ctrmap.formats.generic.collada.XmlFormat;
import ctrmap.renderer.scene.Camera;
import xstandard.math.MathEx;
import xstandard.text.FormattingUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class DAECamera implements DAEIDAble, DAESerializable {

	private String id;
	private boolean blender;

	public String name;

	public DAECameraType type;

	public DAECameraProjection proj;

	public DAECamera(Camera cam) {
		type = cam.projMode == Camera.ProjectionMode.ORTHO ? DAECameraType.ORTHO : DAECameraType.PERSPECTIVE;
		name = XmlFormat.sanitizeName(cam.name);

		if (type == DAECameraType.PERSPECTIVE) {
			DAECameraProjectionPerspective persp = new DAECameraProjectionPerspective();
			persp.fovY = cam.FOV;
			proj = persp;
		} else {
			DAECameraProjectionOrtho ortho = new DAECameraProjectionOrtho();
			ortho.xMag = 16f;
			ortho.yMag = 9f;
			proj = ortho;
		}

		proj.aspect = 16f / 9f;
		proj.near = cam.zNear;
		proj.far = cam.zFar;
	}

	public DAECamera(Element elem, boolean blender) {
		this.blender = blender;
		id = elem.getAttribute("id");
		name = elem.getAttribute("name");
		Element optics = XmlFormat.getParamElement(elem, "optics");
		if (optics != null) {
			Element technique = XmlFormat.getParamElement(optics, "technique_common");

			if (technique != null) {
				Element persp = XmlFormat.getParamElement(technique, "perspective");
				if (persp != null) {
					type = DAECameraType.PERSPECTIVE;
					DAECameraProjectionPerspective p = new DAECameraProjectionPerspective();

					setProjectionCmn(p, elem);

					float fov = XmlFormat.getParamNodeValueFloat(persp, -1f, "xfov");
					if (fov != -1) {
						fov = MathEx.toDegreesf(Camera.fovXToFovY(MathEx.toRadiansf(fov), p.aspect));
					} else {
						fov = XmlFormat.getParamNodeValueFloat(persp, 45f, "yfov");
					}
					p.fovY = fov;

					proj = p;
				} else {
					Element ortho = XmlFormat.getParamElement(technique, "orthographic");
					if (ortho != null) {
						type = DAECameraType.ORTHO;
						DAECameraProjectionOrtho o = new DAECameraProjectionOrtho();

						setProjectionCmn(o, elem);

						o.xMag = XmlFormat.getParamNodeValueFloat(ortho, 16f, "xmag");
						o.yMag = XmlFormat.getParamNodeValueFloat(ortho, 9f, "ymag");

						proj = o;
					}
				}
			}
		}
	}

	@Override
	public Element createElement(Document doc) {
		Element elem = doc.createElement("camera");
		elem.setAttribute("id", id);
		XmlFormat.setAttributeNonNull(elem, "name", name);

		Element optics = doc.createElement("optics");
		Element tech = doc.createElement("technique_common");

		Element proj = null;

		if (type == DAECameraType.PERSPECTIVE) {
			proj = doc.createElement("perspective");

			XmlFormat.setParamNodeWithSID(doc, proj, "yfov", ((DAECameraProjectionPerspective) this.proj).fovY);
		} else {
			proj = doc.createElement("orthographic");
			
			DAECameraProjectionOrtho ortho = (DAECameraProjectionOrtho) this.proj;
			
			XmlFormat.setParamNodeWithSID(doc, proj, "xmag", ortho.xMag);
			XmlFormat.setParamNodeWithSID(doc, proj, "ymag", ortho.yMag);
		}

		if (proj != null) {
			XmlFormat.setParamNodeWithSID(doc, proj, "aspect_ratio", this.proj.aspect);
			XmlFormat.setParamNodeWithSID(doc, proj, "znear", this.proj.near);
			XmlFormat.setParamNodeWithSID(doc, proj, "zfar", this.proj.far);
			tech.appendChild(proj);
		}

		optics.appendChild(tech);

		elem.appendChild(optics);
		return elem;
	}

	public boolean valid() {
		return proj != null && proj.aspect > 0f;
	}

	private void setProjectionCmn(DAECameraProjection proj, Element mainElem) {
		proj.aspect = XmlFormat.getParamNodeValueFloat(mainElem, 16f / 9f, "aspect_ratio");
		proj.near = XmlFormat.getParamNodeValueFloat(mainElem, 0.1f, "znear");
		proj.far = XmlFormat.getParamNodeValueFloat(mainElem, 1000f, "zfar");
	}

	@Override
	public String getID() {
		return id;
	}

	public String getAnimationTargetID() {
		if (blender) {
			//Due to possibly a programming oversight in Blender (or maybe OpenCOLLADA), the camera animation target IDs are formatted as
			//<camera name>-<camera ID>
			//while the camera themselves are IDd as
			//<node name>-<camera name>
			//causing them to be mismatched and not imported
			//This occurs even if a Blender 2.9 camera is exported and imported back to Blender 2.9, unless the collada
			//file is hand-edited to have the correct camera name.
			//This method ensures that <camera name>-<camera ID> is obtained if the camera is from a Blender exported collada file

			return FormattingUtils.getStrWithoutNonAlphanumeric(name, '-') + "-" + id;
		}
		return id;
	}

	@Override
	public void setID(String id) {
		this.id = id;
	}

	public enum DAECameraType {
		PERSPECTIVE,
		ORTHO
	}

	public static abstract class DAECameraProjection {

		public float aspect;
		public float near;
		public float far;

		public void setCamera(Camera cam) {
			cam.zFar = far;
			cam.zNear = near;
			setCameraEx(cam);
		}

		protected abstract void setCameraEx(Camera cam);
	}

	public static class DAECameraProjectionPerspective extends DAECameraProjection {

		public float fovY;

		@Override
		protected void setCameraEx(Camera cam) {
			cam.FOV = fovY;
			cam.projMode = Camera.ProjectionMode.PERSPECTIVE;
		}
	}

	public static class DAECameraProjectionOrtho extends DAECameraProjection {

		public float xMag;
		public float yMag;

		@Override
		protected void setCameraEx(Camera cam) {
			cam.translation.y = xMag;
			cam.projMode = Camera.ProjectionMode.ORTHO;
		}
	}
}
