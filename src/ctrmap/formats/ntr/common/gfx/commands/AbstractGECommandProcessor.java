package ctrmap.formats.ntr.common.gfx.commands;

import ctrmap.formats.ntr.common.gfx.GETextureFormat;
import ctrmap.formats.ntr.common.gfx.commands.mtx.MtxMode;
import xstandard.math.vec.Matrix4;
import xstandard.math.vec.RGBA;
import xstandard.math.vec.Vec2f;
import xstandard.math.vec.Vec3f;
import java.util.HashMap;
import java.util.Map;
import org.joml.Matrix3f;
import org.joml.Matrix4f;
import org.joml.Matrix4fc;
import org.joml.Matrix4x3f;
import org.joml.Matrix4x3fc;

public abstract class AbstractGECommandProcessor implements IGECommandProcessor {

	private MtxMode.GEMatrixMode nowMatrixMode = MtxMode.GEMatrixMode.PROJECTION;

	private final MatrixStack.MatrixCtor matrixCtorNormal = new MatrixStack.MatrixCtor() {
		@Override
		public Matrix4f newMatrix(int index) {
			return new Matrix4f();
		}
	};

	private final MatrixStack projectionStack = new MatrixStack(1, matrixCtorNormal);
	private final MatrixStack textureStack = new MatrixStack(1, matrixCtorNormal);
	private final MatrixStack normalStack = new MatrixStack(31, matrixCtorNormal);

	private final MatrixStack.MatrixCtor matrixCtorComb = new MatrixStack.MatrixCtor() {
		@Override
		public Matrix4f newMatrix(int index) {
			if (index == -1) {
				return new CombMatrix(normalStack.cur);
			}
			return new CombMatrix(normalStack.stack[index]);
		}
	};

	private final CombMatrixStack modelViewStack = new CombMatrixStack(31, matrixCtorComb, normalStack);

	private final Map<MtxMode.GEMatrixMode, MatrixStack> matrixStacks = new HashMap<>();

	protected AbstractGECommandProcessor() {
		matrixStacks.put(MtxMode.GEMatrixMode.PROJECTION, projectionStack);
		matrixStacks.put(MtxMode.GEMatrixMode.TEXTURE, textureStack);
		matrixStacks.put(MtxMode.GEMatrixMode.MODELVIEW, modelViewStack);
		matrixStacks.put(MtxMode.GEMatrixMode.MODELVIEW_NORMAL, modelViewStack);
	}

	protected Matrix4f getCurMatrix() {
		return getCurMatrixStack().cur();
	}

	protected MatrixStack getCurMatrixStack() {
		return matrixStacks.get(nowMatrixMode);
	}
	
	public void mulVertex(Vec3f vert) {
		vert.mulPosition(modelViewStack.cur());
	}
	
	public void mulNormal(Vec3f nor) {
		nor.mulDirection(normalStack.cur());
	}
	
	public void absTexture(Vec2f tex) {
		if (nowTexW == 0 || nowTexH == 0) {
			throw new RuntimeException("No texture loaded! - org vector " + tex + " last vtx " + lastVertex);
		}
		tex.x /= nowTexW;
		tex.y = 1f - (tex.y / nowTexH);
	}
	
	public void mulTexture(Vec2f tex) {
		Vec3f vec3 = new Vec3f(tex.x, tex.y, 0f);
		vec3.mulPosition(textureStack.cur());
		tex.x = vec3.x;
		tex.y = vec3.y;
	}

	@Override
	public void matrixMode(MtxMode.GEMatrixMode mode) {
		nowMatrixMode = mode;
	}

	@Override
	public void pushMatrix() {
		getCurMatrixStack().push();
	}

	@Override
	public void popMatrix(int count) {
		getCurMatrixStack().pop(count);
	}

	@Override
	public void storeMatrix(int pos) {
		getCurMatrixStack().store(pos);
	}

	@Override
	public void loadMatrix(int pos) {
		getCurMatrixStack().load(pos);
	}

	@Override
	public void loadIdentity() {
		getCurMatrix().identity();
	}

	@Override
	public void loadMatrix4x3(Matrix4x3f matrix) {
		getCurMatrix().set4x3(matrix);
	}

	@Override
	public void loadMatrix4x4(Matrix4 matrix) {
		getCurMatrix().set(matrix);
	}

	@Override
	public void multMatrix3x3(Matrix3f matrix) {
		getCurMatrix().mul(new Matrix4f(matrix));
	}

	@Override
	public void multMatrix4x3(Matrix4x3f matrix) {
		getCurMatrix().mul(matrix);
	}

	@Override
	public void multMatrix4x4(Matrix4 matrix) {
		getCurMatrix().mul(matrix);
	}

	@Override
	public void scale(float x, float y, float z) {
		getCurMatrix().scale(x, y, z);
	}

	@Override
	public void translate(float x, float y, float z) {
		getCurMatrix().translate(x, y, z);
	}

	private int nowTexW;
	private int nowTexH;

	@Override
	public void texImage2D(int width, int height, GETextureFormat format, int offset) {
		nowTexW = width;
		nowTexH = height;
	}
	
	public abstract void vertexEx(Vec3f vertex);
	
	private final Vec3f lastVertex = new Vec3f(0, 0, 0);
	protected final RGBA currentColor = new RGBA(255, 255, 255, 255);
	protected final Vec3f currentNormal = new Vec3f(0f, 1f, 0f);
	protected final Vec2f currentTexcoord = new Vec2f(0f, 0f);
	
	@Override
	public void vertex(Vec3f vertex) {
		lastVertex.set(vertex);
		vertexEx(lastVertex.clone());
	}

	@Override
	public void vertexDiff(Vec3f diff) {
		lastVertex.add(diff);
		vertexEx(lastVertex.clone());
	}

	@Override
	public void vertexXY(float x, float y) {
		lastVertex.x = x;
		lastVertex.y = y;
		vertexEx(lastVertex.clone());
	}

	@Override
	public void vertexXZ(float x, float z) {
		lastVertex.x = x;
		lastVertex.z = z;
		vertexEx(lastVertex.clone());
	}

	@Override
	public void vertexYZ(float y, float z) {
		lastVertex.y = y;
		lastVertex.z = z;
		vertexEx(lastVertex.clone());
	}
	
	@Override
	public void color(RGBA color) {
		currentColor.set(color);
	}
	
	@Override
	public void normal(Vec3f normal) {
		currentNormal.set(normal);
	}
	
	@Override
	public void texCoord(Vec2f texcoord) {
		currentTexcoord.set(texcoord);
	}

	private class CombMatrix extends Matrix4f {

		private final Matrix4f normal;

		public CombMatrix(Matrix4f normal) {
			this.normal = normal;
		}
		
		@Override
		public Matrix4f set4x3(Matrix4x3fc m) {
			super.set4x3(m);
			if (nowMatrixMode == MtxMode.GEMatrixMode.MODELVIEW_NORMAL) {
				normal.set4x3(m);
			}
			return this;
		}
		
		@Override
		public Matrix4f set(Matrix4fc m) {
			super.set(m);
			if (nowMatrixMode == MtxMode.GEMatrixMode.MODELVIEW_NORMAL) {
				normal.set(m);
			}
			return this;
		}

		@Override
		public Matrix4f mul(Matrix4fc m) {
			super.mul(m);
			if (nowMatrixMode == MtxMode.GEMatrixMode.MODELVIEW_NORMAL) {
				normal.mul(m);
			}
			return this;
		}

		@Override
		public Matrix4f mul(Matrix4x3fc m) {
			super.mul(m);
			if (nowMatrixMode == MtxMode.GEMatrixMode.MODELVIEW_NORMAL) {
				normal.mul(m);
			}
			return this;
		}

		@Override
		public Matrix4f translate(float x, float y, float z) {
			super.translate(x, y, z);
			if (nowMatrixMode == MtxMode.GEMatrixMode.MODELVIEW_NORMAL) {
				normal.translate(x, y, z);
			}
			return this;
		}

		@Override
		public Matrix4f identity() {
			super.identity();
			if (nowMatrixMode == MtxMode.GEMatrixMode.MODELVIEW_NORMAL) {
				normal.identity();
			}
			return this;
		}

		//scaling not applied to normal matrix
	}

	protected static class MatrixStack {

		private Matrix4f cur;

		private Matrix4f[] stack;
		private int index = 0;

		public MatrixStack(int capacity, MatrixCtor matrixCtor) {
			cur = matrixCtor.newMatrix(-1);
			stack = new Matrix4f[capacity];
			for (int i = 0; i < capacity; i++) {
				stack[i] = matrixCtor.newMatrix(i);
			}
		}

		public Matrix4f cur() {
			return cur;
		}

		public void load(int index) {
			cur.set(stack[index]);
		}

		public void store(int index) {
			stack[index].set(cur);
		}

		public void push() {
			store(index);
			index++;
		}

		public void pop(int count) {
			if (count >= 0) {
				index -= count;
				load(index);
			} else {
				throw new RuntimeException("Negative stack pop not handled.");
			}
		}

		private static interface MatrixCtor {

			public Matrix4f newMatrix(int index);
		}
	}

	private static class CombMatrixStack extends MatrixStack {

		private final MatrixStack normal;

		public CombMatrixStack(int capacity, MatrixStack.MatrixCtor matrixCtor, MatrixStack normal) {
			super(capacity, matrixCtor);
			this.normal = normal;
		}

		@Override
		public void load(int index) {
			super.load(index);
			normal.load(index);
		}

		@Override
		public void store(int index) {
			super.store(index);
			normal.store(index);
		}

		@Override
		public void push() {
			super.push();
			normal.push();
		}

		@Override
		public void pop(int count) {
			super.pop(count);
			normal.pop(count);
		}
	}
}
