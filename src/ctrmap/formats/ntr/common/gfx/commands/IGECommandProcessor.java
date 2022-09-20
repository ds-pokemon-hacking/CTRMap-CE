package ctrmap.formats.ntr.common.gfx.commands;

import ctrmap.formats.ntr.common.gfx.GETextureFormat;
import ctrmap.formats.ntr.common.gfx.commands.mat.MatTexImageParamSet;
import ctrmap.formats.ntr.common.gfx.commands.mtx.MtxMode;
import ctrmap.formats.ntr.common.gfx.commands.poly.PolyAttrSet;
import ctrmap.renderer.scene.model.PrimitiveType;
import xstandard.math.vec.Matrix4;
import xstandard.math.vec.RGBA;
import xstandard.math.vec.Vec2f;
import xstandard.math.vec.Vec3f;
import org.joml.Matrix3f;
import org.joml.Matrix4x3f;

public interface IGECommandProcessor {
	public void matrixMode(MtxMode.GEMatrixMode mode);
	public void pushMatrix();
	public void popMatrix(int count);
	public void storeMatrix(int pos);
	public void loadMatrix(int pos);
	public void loadIdentity();
	public void loadMatrix4x3(Matrix4x3f matrix);
	public void loadMatrix4x4(Matrix4 matrix);
	public void multMatrix3x3(Matrix3f matrix);
	public void multMatrix4x3(Matrix4x3f matrix);
	public void multMatrix4x4(Matrix4 matrix);
	public void scale(float x, float y, float z);
	public void translate(float x, float y, float z);
	
	public void matDiffuseAmbient(RGBA dif, RGBA amb);
	public void matSpecularEmissive(RGBA spec, RGBA emi);
	
	public void texImage2D(int width, int height, GETextureFormat format, int offset);
	public void texPaletteBase(int base);
	public void texMap(boolean repeatU, boolean repeatV, boolean mirrorU, boolean mirrorV);
	public void texGenMode(MatTexImageParamSet.GETexcoordGenMode mode);
	public void texColor0Transparent(boolean transparent);
	
	public void begin(PrimitiveType primitiveMode);
	public void end();
	public void color(RGBA color);
	public void normal(Vec3f normal);
	public void texCoord(Vec2f texcoord);
	public void vertex(Vec3f vertex);
	public void vertexDiff(Vec3f diff);
	public void vertexXY(float x, float y);
	public void vertexXZ(float x, float z);
	public void vertexYZ(float y, float z);
	
	public void polygonId(int polyId);
	public void polygonAlpha(float alpha);
	public void setFogEnable(boolean enable);
	public void setLightEnable(int index, boolean enable);
	public void polygonMode(PolyAttrSet.GEPolygonMode mode);
	public void cullFace(boolean drawFront, boolean drawBack);
	public void depthFunc(PolyAttrSet.GEDepthFunction func);
	public void dot1OverMode(PolyAttrSet.GE1DotOverMode mode);
	public void farClipMode(PolyAttrSet.GEFarClipMode mode);
	public void xluDepthMode(PolyAttrSet.GEXLUDepthMode mode);
}
