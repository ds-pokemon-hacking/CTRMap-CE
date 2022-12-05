package ctrmap.renderer.util;

import ctrmap.renderer.util.texture.TextureConverter;
import ctrmap.renderer.util.texture.TextureCodec;
import com.jogamp.opengl.glu.gl2.GLUgl2;
import ctrmap.renderer.backends.base.AbstractBackend;
import ctrmap.renderer.backends.base.ViewportInfo;
import ctrmap.renderer.scenegraph.G3DResourceInstance;
import ctrmap.renderer.scene.model.Mesh;
import ctrmap.renderer.scene.model.Vertex;
import ctrmap.renderer.scene.texturing.Material;
import ctrmap.renderer.scene.texturing.Texture;
import ctrmap.renderer.scene.texturing.formats.TextureFormatHandler;
import ctrmap.renderer.util.generators.BoundingBoxGenerator;
import ctrmap.renderer.util.texture.TextureProcessor;
import xstandard.math.vec.Matrix4;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Polygon;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;

public class ObjectSelection {

	public static final String OBJSEL_NEW_SHADER_NAME = "ObjectSelectionShader.fsh_ext";
	public static final String OBJSEL_RT_NAME = "RT_OBJSEL";

	public static final int WIN_POS_ARR_STRIDE = 3;

	private static long lastPassTime = 0;

	//yes, this is hardcoded, but it's solely for my own debugging purposes and I only have a fullHD monitor, okay?
	private static BufferedImage imgBuf;
	private static Graphics g;

	public static final boolean OBJSEL_DEBUG = false;

	static {
		if (OBJSEL_DEBUG) {
			imgBuf = new BufferedImage(1920, 1080, BufferedImage.TYPE_INT_RGB);
			g = imgBuf.getGraphics();
		}
	}

	public static void enableObjSelSHA(AbstractBackend backend) {
		backend.addRenderTarget(OBJSEL_RT_NAME, TextureFormatHandler.RGBA8);
		backend.getProgramManager().loadExtension("NoObjectSelectionShader.fsh_ext");
	}

	public static void enableObjSelSHA(Material material) {
		material.addShaderExtension(OBJSEL_NEW_SHADER_NAME);
	}

	public static int getObjectSelectionRenderTargetRawValue(MouseEvent evt, AbstractBackend backend) {
		ByteBuffer buf = ByteBuffer.allocate(Float.BYTES);
		buf.order(ByteOrder.LITTLE_ENDIAN);
		backend.readPixels(evt.getX(), backend.getViewportInfo().surfaceDimensions.height - evt.getY() - 1, 1, 1, OBJSEL_RT_NAME, buf);

		int intResult = buf.getInt();
		return intResult;
	}
	
	public static int getSelectedObjectIDSHA(MouseEvent evt, AbstractBackend backend) {
		//The ObjId output from the shader is as follows:
		// 31 ............. 24    23    22 .... 17  16 ............. 0
		// Object ID High bits  Active  Type flags  Object ID Low bits
		//The reason for this is that unrendered fragments have the framebuffer alpha defaulted to an undefined behavior, usually 0xFF
		//This ensures that the component used for determining the presence of the ObjID is untampered with by the GPU
		//However, since the alpha is blended by the alpha blend function, we can't really use its channel for storing data
		//As a result, the high bits are unused and unusable
		int intResult = getObjectSelectionRenderTargetRawValue(evt, backend);

		if (OBJSEL_DEBUG) {
			System.out.println("ReadPixels objsel resulted to int result " + Integer.toHexString(intResult));

			ViewportInfo vi = backend.getViewportInfo();
			Texture tex = new Texture(vi.surfaceDimensions.width, vi.surfaceDimensions.height, TextureFormatHandler.RGBA8);
			ByteBuffer bb = ByteBuffer.allocate(tex.width * tex.height * tex.format.getNativeBPP());
			backend.readPixels(0, 0, tex.width, tex.height, OBJSEL_RT_NAME, bb);
			tex.data = TextureProcessor.flipImageData(tex.width, tex.height, bb.array(), tex.format);

			TextureConverter.writeTextureToFile(new File("D:\\_REWorkspace\\shader\\debug\\objsel.png"), "png", tex);
		}

		if ((intResult & 0xFF800000) != 0xFF800000) {
			return -1;
		}

		return (intResult & 0x007FFFFF);
	}

	public static int makeSelectionOBJID(int objId, Enum type) {
		//The ObjId input to the shader is as follows:
		//   24    23 .... 16  15 .... 0
		// Active  Type flags  Object ID
		int result = objId | 0x800000;
		if (type != null) {
			result |= type.ordinal() << 16;
		}
		return result;
	}

	public static boolean getIsObjSelected(MouseEvent evt, G3DResourceInstance instance, AbstractBackend backend) {
		if (instance.resource == null) {
			return false;
		}

		if (OBJSEL_DEBUG) {
			if (System.currentTimeMillis() - lastPassTime > 100 && lastPassTime != 0) {
				try {
					//flush the image buffer
					ImageIO.write(imgBuf, "png", new File("D:/_REWorkspace/projection_debug/debugout_" + System.currentTimeMillis() + ".png"));
					g.setColor(Color.WHITE);
					g.fillRect(0, 0, 1920, 1080);
				} catch (IOException ex) {
					Logger.getLogger(ObjectSelection.class.getName()).log(Level.SEVERE, null, ex);
				}
			}

			g.setColor(Color.BLUE);
			g.drawRect(evt.getX() - 1, evt.getY() - 1, 3, 3);
			g.setColor(Color.RED);
			g.drawRect(evt.getX(), evt.getY(), 1, 1);
		}

		ViewportInfo vi = backend.getViewportInfo();

		GLUgl2 glu = new GLUgl2();

		Matrix4 modelViewMatrix = instance.getAbsoluteModelViewMatrix();
		int[] viewMatrix = vi.getViewportMatrix();
		Matrix4 projectionMatrix = instance.getAbsoluteProjectionMatrix();

		//Project individual faces of the bounding box
		Mesh bboxMesh = BoundingBoxGenerator.generateBBox(instance.resource);

		float[] screenPosArray = new float[WIN_POS_ARR_STRIDE * bboxMesh.vertices.size()];

		//The vertex array is now composed of quads
		for (int i = 0; i < bboxMesh.vertices.size(); i++) {
			Vertex v = bboxMesh.vertices.get(i);
			glu.gluProject(v.position.x, v.position.y, v.position.z, modelViewMatrix.getMatrix(), 0, projectionMatrix.getMatrix(), 0, viewMatrix, 0, screenPosArray, WIN_POS_ARR_STRIDE * i);
			screenPosArray[WIN_POS_ARR_STRIDE * i + 1] = vi.surfaceDimensions.height - screenPosArray[WIN_POS_ARR_STRIDE * i + 1];
		}

		for (int face = 0; face < bboxMesh.vertices.size() / 4; face++) {
			Polygon poly = getPolygon(screenPosArray, face * 4 * 3);
			if (OBJSEL_DEBUG) {
				if (poly != null) {
					g.drawPolygon(poly);
				}
			}
			if (poly != null && poly.contains(evt.getPoint())) {
				return true;
			}
		}
		lastPassTime = System.currentTimeMillis();

		return false;
	}

	private static Polygon getPolygon(float[] posArray, int posArrayOffs) {
		Polygon poly = new Polygon();
		boolean isPolyValid = true;
		for (int i = 0; i < 4; i++) {
			poly.addPoint((int) posArray[posArrayOffs + i * WIN_POS_ARR_STRIDE], (int) posArray[posArrayOffs + i * WIN_POS_ARR_STRIDE + 1]);
			isPolyValid &= posArray[posArrayOffs + i * WIN_POS_ARR_STRIDE + 2] < 1f;
		}
		if (!isPolyValid) {
			return null;
		}
		return poly;
	}
}
