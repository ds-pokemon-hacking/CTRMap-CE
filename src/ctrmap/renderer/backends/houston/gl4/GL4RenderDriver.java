package ctrmap.renderer.backends.houston.gl4;

import com.jogamp.opengl.GL4;
import com.jogamp.opengl.GL4bc;
import ctrmap.renderer.backends.base.RenderState;
import ctrmap.renderer.backends.base.shaderengine.ShaderProgram;
import ctrmap.renderer.scene.model.Mesh;
import ctrmap.renderer.scene.model.PrimitiveType;
import ctrmap.renderer.scene.model.draw.vtxlist.VertexListUsage;
import ctrmap.renderer.scene.model.draw.buffers.Buffer;
import ctrmap.renderer.backends.base.flow.IRenderDriver;
import static ctrmap.renderer.backends.houston.common.HoustonConstants.*;
import ctrmap.renderer.scene.metadata.ReservedMetaData;
import ctrmap.renderer.scene.model.draw.buffers.BufferComponent;
import ctrmap.renderer.scene.model.draw.buffers.mesh.MeshVertexBuffer;
import ctrmap.renderer.scene.model.draw.vtxlist.IVertexListMulti;
import ctrmap.renderer.scene.texturing.Material;
import ctrmap.renderer.scene.texturing.MaterialParams;
import ctrmap.renderer.scene.texturing.TextureMapper;
import ctrmap.renderer.scene.texturing.formats.TextureFormatHandler;
import xstandard.math.vec.Matrix4;
import xstandard.math.vec.RGBA;
import xstandard.math.vec.Vec3f;
import xstandard.math.vec.Vec4f;
import java.nio.charset.StandardCharsets;
import org.joml.Matrix3f;

public class GL4RenderDriver implements IRenderDriver {

	private GL4 gl;
	private HoustonGL4 backend;

	public GL4RenderDriver(HoustonGL4 backend) {
		this.backend = backend;
	}

	public void setGL(GL4 gl) {
		this.gl = gl;
	}

	@Override
	public Object getIdentity() {
		return backend.getIdentity();
	}

	@Override
	public void beginDrawEx(RenderState state) {
		backend.setUpFramebuffer(gl, this, state);
	}

	@Override
	public void finishDrawEx(RenderState state) {
		backend.flushFramebuffer(gl, this, state);
	}

	private final int[] bufferGenTemp = new int[1];

	@Override
	public synchronized int genBuffer() {
		gl.glGenBuffers(1, bufferGenTemp, 0);
		return bufferGenTemp[0];
	}

	@Override
	public void bindBuffer(Buffer.BufferTarget target, int ptr) {
		gl.glBindBuffer(getGL4BufferTarget(target), ptr);
	}

	@Override
	public void allocBuffer(Buffer.BufferTarget target, int size, VertexListUsage usage) {
		gl.glBufferData(getGL4BufferTarget(target), size, null, getGL4BufferUsage(usage));
	}

	@Override
	public void uploadBufferData(Buffer.BufferTarget target, java.nio.Buffer buffer, int offset, int size) {
		gl.glBufferSubData(getGL4BufferTarget(target), offset, size, buffer);
	}

	public static int getGL4BufferUsage(VertexListUsage usage) {
		switch (usage) {
			case DYNAMIC:
				return GL4.GL_DYNAMIC_DRAW;
			case STATIC:
				return GL4.GL_STATIC_DRAW;
			case STREAM:
				return GL4.GL_STREAM_DRAW;
		}
		throw new IllegalArgumentException();
	}

	public static int getGL4BufferTarget(Buffer.BufferTarget tgt) {
		switch (tgt) {
			case ARRAY_BUFFER:
				return GL4.GL_ARRAY_BUFFER;
			case ELEMENT_ARRAY_BUFFER:
				return GL4.GL_ELEMENT_ARRAY_BUFFER;
		}
		throw new IllegalArgumentException();
	}
	
	public static int getGL4BufferComponentTypeUnsigned(BufferComponent.BufferComponentType t) {
		switch (t) {
			case BYTE:
				return GL4.GL_UNSIGNED_BYTE;
			case FLOAT:
				return GL4.GL_FLOAT;
			case INT:
				return GL4.GL_UNSIGNED_INT;
			case SHORT:
				return GL4.GL_UNSIGNED_SHORT;
		}
		throw new IllegalArgumentException();
	}

	public static int getGL4PrimitiveType(PrimitiveType t) {
		switch (t) {
			case LINES:
				return GL4.GL_LINES;
			case TRIS:
				return GL4.GL_TRIANGLES;
			case QUADS:
				return GL4.GL_QUADS;
			case QUADSTRIPS:
				return GL4bc.GL_QUAD_STRIP;
			case TRIFANS:
				return GL4.GL_TRIANGLE_FAN;
			case TRISTRIPS:
				return GL4.GL_TRIANGLE_STRIP;
			case LINESTRIPS:
				return GL4.GL_LINE_STRIP;
		}
		return GL4.GL_TRIANGLES;
	}

	@Override
	public void clearColor(RGBA color) {
		gl.glColorMask(true, true, true, true);
		gl.glClearColor(color.getR(), color.getG(), color.getB(), color.getA());
	}

	@Override
	public void clearDepth(float depth) {
		gl.glDepthMask(true);
		gl.glClearDepthf(depth);
	}

	@Override
	public void clearFramebuffer() {
		gl.glClear(GL4.GL_COLOR_BUFFER_BIT | GL4.GL_DEPTH_BUFFER_BIT | GL4.GL_STENCIL_BUFFER_BIT);
	}

	@Override
	public void flush() {
		gl.glFlush();
	}

	private final int[] texBuf = new int[1];

	@Override
	public synchronized int genTexture() {
		gl.glGenTextures(1, texBuf, 0);
		return texBuf[0];
	}

	@Override
	public void bindTexture(int name) {
		gl.glBindTexture(GL4.GL_TEXTURE_2D, name);
	}

	@Override
	public void activeTexture(int unitIndex) {
		gl.glActiveTexture(GL4.GL_TEXTURE0 + unitIndex);
	}

	@Override
	public void texImage2D(TextureFormatHandler format, int w, int h, java.nio.Buffer data) {
		gl.glTexImage2D(
			GL4.GL_TEXTURE_2D,
			0,
			GL4Material.getGL4InternalTextureFormat(format.originFormat, backend.getSettings(), backend.getExtensions()),
			w,
			h,
			0,
			GL4Material.getGL4ExternalTextureFormat(format.nativeFormat, backend.getExtensions()),
			GL4Material.getGL4TextureFormatDataType(format.nativeFormat),
			data
		);
	}
	
	private static final int[] ATTR_MAP = new int[] {
		ATTRLOC_POSITION_A,
		ATTRLOC_POSITION_B,
		ATTRLOC_COLOR,
		ATTRLOC_NORMAL_A,
		ATTRLOC_NORMAL_B,
		ATTRLOC_TANGENT_A,
		ATTRLOC_TANGENT_B,
		ATTRLOC_UV0,
		ATTRLOC_UV1,
		ATTRLOC_UV2,
		ATTRLOC_BONE_IDX,
		ATTRLOC_BONE_WEIGHTS
	};

	@Override
	public void drawMesh(Mesh mesh) {
		mesh.buffers.bindBuffers(this);

		if (mesh.primitiveType == PrimitiveType.LINES) {
			gl.glLineWidth(ReservedMetaData.getLineWidth(mesh.metaData, 1f));
		}
		
		for (int a : ATTR_MAP) {
			gl.glEnableVertexAttribArray(a);
		}

		MeshVertexBuffer vbo = mesh.buffers.vbo;
		gl.glVertexAttribPointer(ATTRLOC_POSITION_A, vbo.posA.getElementCount(), GL4.GL_FLOAT, false, vbo.posA.getStride(), vbo.posA.getOffset());
		gl.glVertexAttribPointer(ATTRLOC_POSITION_B, vbo.posB.getElementCount(), GL4.GL_FLOAT, false, vbo.posB.getStride(), vbo.posB.getOffset());
		gl.glVertexAttribPointer(ATTRLOC_NORMAL_A, vbo.nrmA.getElementCount(), GL4.GL_FLOAT, false, vbo.nrmA.getStride(), vbo.nrmA.getOffset());
		gl.glVertexAttribPointer(ATTRLOC_NORMAL_B, vbo.nrmB.getElementCount(), GL4.GL_FLOAT, false, vbo.nrmB.getStride(), vbo.nrmB.getOffset());
		gl.glVertexAttribPointer(ATTRLOC_TANGENT_A, vbo.tgtA.getElementCount(), GL4.GL_FLOAT, false, vbo.tgtA.getStride(), vbo.tgtA.getOffset());
		gl.glVertexAttribPointer(ATTRLOC_TANGENT_B, vbo.tgtB.getElementCount(), GL4.GL_FLOAT, false, vbo.tgtB.getStride(), vbo.tgtB.getOffset());
		gl.glVertexAttribPointer(ATTRLOC_COLOR, vbo.col.getElementCount(), GL4.GL_UNSIGNED_BYTE, true, vbo.col.getStride(), vbo.col.getOffset());
		for (int i = 0; i < vbo.uv.length; i++) {
			gl.glVertexAttribPointer(ATTRLOC_UV0 + i, vbo.uv[i].getElementCount(), GL4.GL_FLOAT, false, vbo.uv[i].getStride(), mesh.buffers.vbo.uv[i].getOffset());
		}
		
		gl.glVertexAttribPointer(ATTRLOC_BONE_IDX, vbo.bidx.getElementCount(), GL4.GL_UNSIGNED_BYTE, false, vbo.bidx.getStride(), vbo.bidx.getOffset());
		gl.glVertexAttribPointer(ATTRLOC_BONE_WEIGHTS, vbo.bwgt.getElementCount(), GL4.GL_FLOAT, false, vbo.bwgt.getStride(), vbo.bwgt.getOffset());

		if (mesh.useIBO) {
			gl.glDrawElements(
				getGL4PrimitiveType(mesh.primitiveType),
				mesh.buffers.indexCount(),
				getGL4BufferComponentTypeUnsigned(mesh.buffers.ibo.idxBuf.getType()),
				0
			);
		} else {
			switch (mesh.vertices.getType()) {
				case SINGLE:
				case MORPH:
					gl.glDrawArrays(getGL4PrimitiveType(mesh.primitiveType), 0, mesh.buffers.vertexCount());
					break;
				case MULTI:
					IVertexListMulti ml = (IVertexListMulti) mesh.vertices;
					int[] first = ml.first();
					gl.glMultiDrawArrays(getGL4PrimitiveType(mesh.primitiveType), first, 0, ml.count(), 0, first.length);
					break;
			}
		}

		for (int a : ATTR_MAP) {
			gl.glDisableVertexAttribArray(a);
		}
	}

	@Override
	public void setUpTextureMapper(TextureMapper mapper) {
		gl.glTexParameteri(GL4.GL_TEXTURE_2D, GL4.GL_TEXTURE_WRAP_S, GL4Material.getGL4TextureMapMode(mapper.mapU));
		gl.glTexParameteri(GL4.GL_TEXTURE_2D, GL4.GL_TEXTURE_WRAP_T, GL4Material.getGL4TextureMapMode(mapper.mapV));
		gl.glTexParameteri(GL4.GL_TEXTURE_2D, GL4.GL_TEXTURE_MAG_FILTER, GL4Material.getGL4TextureMagFilter(mapper.textureMagFilter));
		gl.glTexParameteri(GL4.GL_TEXTURE_2D, GL4.GL_TEXTURE_MIN_FILTER, GL4Material.getFallbackGL4TextureMinFilter(mapper.textureMinFilter));
	}

	@Override
	public void setTexturingEnable(boolean value) {
		if (value) {
			gl.glEnable(GL4.GL_TEXTURE_2D);

		} else {
			gl.glDisable(GL4.GL_TEXTURE_2D);
		}
	}

	@Override
	public void useProgram(ShaderProgram program) {
		gl.glUseProgram(program == null ? 0 : program.handle.get(this));
	}

	@Override
	public void deleteProgram(ShaderProgram program) {
		if (program != null) {
			gl.glDeleteProgram(program.handle.get(this));
		}
	}

	@Override
	public ShaderProgram getShaderProgram(Material mat) {
		return backend.getProgramManager().getShaderProgram(this, mat);
	}

	private final float[] matrix4Fast = new float[256 * 16];

	@Override
	public synchronized void uniformMatrix4fv(int location, Matrix4... matrices) {
		float[] array;
		if (matrices.length * 16 <= matrix4Fast.length) {
			array = matrix4Fast;
		} else {
			array = new float[matrices.length * 16];
		}
		for (int i = 0; i < matrices.length; i++) {
			matrices[i].get(array, i * 16);
		}
		gl.glUniformMatrix4fv(location, matrices.length, false, array, 0);
	}
	
	private final float[] matrix3Fast = new float[256 * 16];

	@Override
	public synchronized void uniformMatrix3fv(int location, Matrix3f... matrices) {
		float[] array;
		if (matrices.length * 12 <= matrix3Fast.length) {
			array = matrix3Fast;
		} else {
			array = new float[matrices.length * 12];
		}
		for (int i = 0; i < matrices.length; i++) {
			matrices[i].get(array, i * 12);
		}
		gl.glUniformMatrix3fv(location, matrices.length, false, array, 0);
	}

	@Override
	public void uniform1i(int location, int value) {
		gl.glUniform1i(location, value);
	}

	@Override
	public void uniform1iv(int location, int... values) {
		gl.glUniform1iv(location, values.length, values, 0);
	}
	
	private final float[] vec4fast = new float[256 * 4];

	@Override
	public synchronized void uniform4fv(int location, Vec4f... values) {
		float[] floats;
		if (values.length * 4 <= vec4fast.length) {
			floats = vec4fast;
		}
		else {
			floats = new float[values.length * 4];
		}
		for (int i = 0; i < values.length; i++) {
			values[i].get(floats, i * 4);
		}
		gl.glUniform4fv(location, values.length, floats, 0);
	}

	@Override
	public int getUniformLocation(String name, ShaderProgram program) {
		return gl.glGetUniformLocation(program.handle.get(this), name);
	}

	@Override
	public void uniform1fv(int location, float... values) {
		gl.glUniform1fv(location, values.length, values, 0);
	}
	
	private final float[] vec3fast = new float[256 * 3];

	@Override
	public synchronized void uniform3fv(int location, Vec3f... values) {
		float[] floats;
		if (values.length * 3 <= vec3fast.length) {
			floats = vec3fast;
		}
		else {
			floats = new float[values.length * 3];
		}
		for (int i = 0; i < values.length; i++) {
			values[i].get(floats, i * 3);
		}
		gl.glUniform3fv(location, values.length, floats, 0);
	}

	@Override
	public void resetMaterialState() {
		gl.glEnable(GL4.GL_DEPTH_TEST);
		gl.glDepthFunc(GL4.GL_LEQUAL);
		gl.glDepthMask(true);
		gl.glColorMask(true, true, true, true);
		gl.glDisable(GL4.GL_BLEND);
	}

	@Override
	public void setUpBlend(MaterialParams.BlendOperation blendOperation) {
		if (blendOperation.enabled) {
			gl.glEnable(GL4.GL_BLEND);
			gl.glBlendColor(blendOperation.blendColor.getR(),
				blendOperation.blendColor.getG(),
				blendOperation.blendColor.getB(),
				blendOperation.blendColor.getA()
			);
			gl.glBlendFuncSeparate(
				GL4Material.getGL4BlendFunction(blendOperation.colorSrcFunc),
				GL4Material.getGL4BlendFunction(blendOperation.colorDstFunc),
				GL4Material.getGL4BlendFunction(blendOperation.alphaSrcFunc),
				GL4Material.getGL4BlendFunction(blendOperation.alphaDstFunc)
			);
			gl.glBlendEquationSeparate(
				GL4Material.getGL4BlendEquation(blendOperation.colorEquation),
				GL4Material.getGL4BlendEquation(blendOperation.alphaEquation)
			);
		} else {
			gl.glDisable(GL4.GL_BLEND);
		}
	}

	@Override
	public void setUpStencilTest(MaterialParams.StencilTest stencilTest, MaterialParams.StencilOperation stencilOperation) {
		if (stencilTest.enabled) {
			gl.glEnable(GL4.GL_STENCIL_TEST);
			gl.glStencilFunc(
				GL4Material.getGL4TestFunc(stencilTest.testFunction),
				stencilTest.reference,
				stencilTest.funcMask
			);
			gl.glStencilMask(stencilTest.bufferMask);
			gl.glStencilOp(
				GL4Material.getGL4StencilOp(stencilOperation.fail),
				GL4Material.getGL4StencilOp(stencilOperation.zFail),
				GL4Material.getGL4StencilOp(stencilOperation.zPass)
			);
		} else {
			gl.glDisable(GL4.GL_STENCIL_TEST);
		}
	}

	@Override
	public void setUpDepthTest(MaterialParams.DepthColorMask depthColorMask) {
		if (depthColorMask.enabled) {
			gl.glEnable(GL4.GL_DEPTH_TEST);
			gl.glDepthFunc(GL4Material.getGL4TestFunc(depthColorMask.depthFunction));
			gl.glDepthMask(depthColorMask.depthWrite);
			gl.glColorMask(depthColorMask.redWrite, depthColorMask.greenWrite, depthColorMask.blueWrite, depthColorMask.alphaWrite);
		} else {
			gl.glDisable(GL4.GL_DEPTH_TEST);
		}
	}

	@Override
	public void setUpAlphaTest(MaterialParams.AlphaTest alphaTest) {

	}

	@Override
	public void setUpFaceCulling(MaterialParams.FaceCulling faceCulling) {
		if (faceCulling == MaterialParams.FaceCulling.NEVER || !backend.getSettings().BACKFACE_CULLING) {
			gl.glDisable(GL4.GL_CULL_FACE);
		} else {
			gl.glEnable(GL4.GL_CULL_FACE);
			gl.glCullFace(faceCulling == MaterialParams.FaceCulling.FRONT_FACE ? GL4.GL_FRONT : GL4.GL_BACK);
		}
	}

	@Override
	public void uniform1f(int location, float value) {
		gl.glUniform1f(location, value);
	}

	@Override
	public void deleteTextures(int... textures) {
		gl.glDeleteTextures(textures.length, textures, 0);
	}

	@Override
	public void deleteBuffers(int... buffers) {
		gl.glDeleteBuffers(buffers.length, buffers, 0);
	}

	@Override
	public int compileShader(String source, ShaderType type) {
		int sh = gl.glCreateShader(type == ShaderType.VERTEX_SHADER ? GL4.GL_VERTEX_SHADER : GL4.GL_FRAGMENT_SHADER);
		gl.glShaderSource(sh, 1, new String[]{source}, new int[]{source.length()}, 0);

		gl.glCompileShader(sh);
		if (printShaderError(gl, sh)) {
			System.err.println("--- VERTEX SHADER SOURCE DUMP ---");
			System.err.println(source);
			System.err.println("-----");
		}
		return sh;
	}

	public static boolean printShaderError(GL4 gl, int shader) {
		int[] status = new int[1];
		gl.glGetShaderiv(shader, GL4.GL_COMPILE_STATUS, status, 0);
		if (status[0] == GL4.GL_FALSE) {
			int[] maxErrLen = new int[1];
			gl.glGetShaderiv(shader, GL4.GL_INFO_LOG_LENGTH, maxErrLen, 0);
			if (maxErrLen[0] > 0) {
				byte[] infoLog = new byte[maxErrLen[0]];
				gl.glGetShaderInfoLog(shader, maxErrLen[0], maxErrLen, 0, infoLog, 0);
				System.err.println(new String(infoLog, StandardCharsets.US_ASCII));
				return true;
			}
		}
		return false;
	}

	@Override
	public int linkProgram(int vsh, int fsh) {
		int programHandle = gl.glCreateProgram();
		gl.glAttachShader(programHandle, vsh);
		gl.glAttachShader(programHandle, fsh);

		gl.glBindAttribLocation(programHandle, ATTRLOC_POSITION_A, HOUSTON_ATTRIB_POSITION_A);
		gl.glBindAttribLocation(programHandle, ATTRLOC_POSITION_B, HOUSTON_ATTRIB_POSITION_B);
		gl.glBindAttribLocation(programHandle, ATTRLOC_NORMAL_A, HOUSTON_ATTRIB_NORMAL_A);
		gl.glBindAttribLocation(programHandle, ATTRLOC_NORMAL_B, HOUSTON_ATTRIB_NORMAL_B);
		gl.glBindAttribLocation(programHandle, ATTRLOC_TANGENT_A, HOUSTON_ATTRIB_TANGENT_A);
		gl.glBindAttribLocation(programHandle, ATTRLOC_TANGENT_B, HOUSTON_ATTRIB_TANGENT_B);
		gl.glBindAttribLocation(programHandle, ATTRLOC_COLOR, HOUSTON_ATTRIB_COLOR);
		gl.glBindAttribLocation(programHandle, ATTRLOC_UV0, HOUSTON_ATTRIB_UV0);
		gl.glBindAttribLocation(programHandle, ATTRLOC_UV1, HOUSTON_ATTRIB_UV1);
		gl.glBindAttribLocation(programHandle, ATTRLOC_UV2, HOUSTON_ATTRIB_UV2);
		gl.glBindAttribLocation(programHandle, ATTRLOC_BONE_IDX, HOUSTON_ATTRIB_BONE_IDX);
		gl.glBindAttribLocation(programHandle, ATTRLOC_BONE_WEIGHTS, HOUSTON_ATTRIB_BONE_WEIGHTS);

		gl.glLinkProgram(programHandle);

		//printProgramError(gl, programHandle); //very slow, blocking method. use printShaderError instead
		gl.glDetachShader(programHandle, vsh);
		gl.glDetachShader(programHandle, fsh);

		return programHandle;
	}

	@Override
	public void deleteShader(int shader) {
		gl.glDeleteShader(shader);
	}
}
