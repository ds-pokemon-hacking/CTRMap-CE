package ctrmap.renderer.backends.houston.common;

import ctrmap.renderer.backends.base.flow.IRenderDriver;
import ctrmap.renderer.backends.base.shaderengine.ShaderProgram;
import ctrmap.renderer.scene.Light;
import ctrmap.renderer.scene.model.Mesh;
import ctrmap.renderer.scene.texturing.Material;
import xstandard.math.vec.Matrix4;
import xstandard.math.vec.Vec3f;
import xstandard.math.vec.Vec4f;
import org.joml.Matrix3f;
import ctrmap.renderer.backends.base.flow.IShaderAdapter;

public interface HoustonShaderAdapter extends IShaderAdapter {

	public static final HoustonShaderAdapter INSTANCE = new HoustonShaderAdapter() {
	};

	@Override
	public default void setUpMatrices(Matrix4 model, Matrix4 view, Matrix4 projection, Matrix3f normal, ShaderProgram program, IRenderDriver driver) {
		driver.uniformMatrix4fv(program.getUniformLocation(HoustonUniforms.MTX_MODEL, driver), model);
		driver.uniformMatrix4fv(program.getUniformLocation(HoustonUniforms.MTX_VIEW, driver), view);
		driver.uniformMatrix4fv(program.getUniformLocation(HoustonUniforms.MTX_PROJECTION, driver), projection);
		driver.uniformMatrix3fv(program.getUniformLocation(HoustonUniforms.MTX_NORMAL, driver), normal);
	}

	@Override
	public default void setUpTextureTransforms(Matrix4[] matrices, IRenderDriver driver, ShaderProgram program) {
		driver.uniformMatrix4fv(program.getUniformLocation(HoustonUniforms.MTA_TRANSFORM, driver), matrices);
	}

	@Override
	public default void setUpMeshUVAssignments(int[] assignments, IRenderDriver driver, ShaderProgram program) {
		driver.uniform1iv(program.getUniformLocation(HoustonUniforms.MESH_UV_ASSIGNMENT, driver), assignments);
	}

	@Override
	public default void setUpTextureAssignments(int[] assignments, IRenderDriver driver, ShaderProgram program) {
		driver.uniform1iv(program.getUniformLocation(HoustonUniforms.TEX_SAMPLERS, driver), assignments);
	}

	@Override
	public default void setUpMaterialConstantColors(Vec4f[] colorVectors, IRenderDriver driver, ShaderProgram program) {
		driver.uniform4fv(program.getUniformLocation(HoustonUniforms.TEV_CONST_COLOR, driver), colorVectors);
	}

	@Override
	public default void setUpLUTAssignments(int[] assignments, IRenderDriver driver, ShaderProgram program) {
		driver.uniform1iv(program.getUniformLocation(HoustonUniforms.TEX_SAMPLER_LUT, driver), assignments);
	}

	@Override
	public default void setUpMeshBoolUniforms(Mesh mesh, IRenderDriver driver, ShaderProgram program) {
		int[] boolUniforms = new int[HoustonUniforms.MESH_BOOLUNIFORMS_COUNT];
		boolUniforms[HoustonUniforms.MESH_BOOLUNIFORMS_COLOR_IDX] = mesh.hasColor ? 1 : 0;
		boolUniforms[HoustonUniforms.MESH_BOOLUNIFORMS_NORMAL_IDX] = mesh.hasNormal ? 1 : 0;
		boolUniforms[HoustonUniforms.MESH_BOOLUNIFORMS_TANGENT_IDX] = mesh.hasTangent ? 1 : 0;
		boolUniforms[HoustonUniforms.MESH_BOOLUNIFORMS_SKINNING_IDX] = mesh.hasBoneIndices ? 1 : 0;
		driver.uniform1iv(program.getUniformLocation(HoustonUniforms.MESH_BOOLUNIFORMS, driver), boolUniforms);
	}

	@Override
	public default void setLUTNeedsTangentUniform(boolean value, IRenderDriver driver, ShaderProgram program) {
		driver.uniform1i(program.getUniformLocation(HoustonUniforms.SHA_NEEDS_TANGENT, driver), value ? 1 : 0);
	}

	@Override
	public default void setUpSkeletalTransforms(Matrix4[] matrices, IRenderDriver driver, ShaderProgram program) {
		if (matrices != null && matrices.length > 0) {
			driver.uniform1i(program.getUniformLocation(HoustonUniforms.SKA_TRANSFORM_COUNT, driver), matrices.length);
			driver.uniform1i(program.getUniformLocation(HoustonUniforms.SKA_TRANSFORM_ENABLE, driver), 1);

			driver.uniformMatrix4fv(program.getUniformLocation(HoustonUniforms.SKA_TRANSFORMS, driver), matrices);
		} else {
			driver.uniform1i(program.getUniformLocation(HoustonUniforms.SKA_TRANSFORM_ENABLE, driver), 0);
		}
	}

	@Override
	public default void setUpLights(Light[] lights, Matrix4[] lightMatrices, IRenderDriver driver, ShaderProgram program) {
		if (lights != null) {
			for (int li = 0; li < lights.length; li++) {
				Light l = lights[li];

				Vec3f lightDirMVP = new Vec3f(new Vec3f(l.direction).mulDirection(lightMatrices[li]));
				lightDirMVP.normalize();
				Vec3f lightPosMVP = new Vec3f(new Vec3f(l.position).mulDirection(lightMatrices[li]));
				if (l.directional) {
					lightPosMVP.normalize();
				}

				driver.uniform1i(program.getUniformLocation(HoustonUniforms.getLightAttribName(li, HoustonUniforms.LIGHT_DIRECTIONAL), driver), l.directional ? 1 : 0);
				driver.uniform3fv(program.getUniformLocation(HoustonUniforms.getLightAttribName(li, HoustonUniforms.LIGHT_DIRECTION), driver), lightDirMVP);
				driver.uniform3fv(program.getUniformLocation(HoustonUniforms.getLightAttribName(li, HoustonUniforms.LIGHT_POSITION), driver), lightPosMVP);

				driver.uniform4fv(program.getUniformLocation(HoustonUniforms.getLightAttribName(li, HoustonUniforms.LIGHT_COLORS), driver), l.ambientColor, l.diffuseColor, l.specular0Color, l.specular1Color);
			}
		}

		driver.uniform1i(program.getUniformLocation(HoustonUniforms.LIGHT_COUNT, driver), lights == null ? 0 : lights.length);
	}

	@Override
	public default void setUpMaterialLightingColors(Vec4f[] colors, IRenderDriver driver, ShaderProgram program) {
		driver.uniform4fv(
			program.getUniformLocation(HoustonUniforms.LIGHTING_COLORS, driver),
			colors
		);
	}

	@Override
	public default void setUpMaterialUniforms(Material mat, IRenderDriver driver, ShaderProgram program) {
		passTextureMapperUniforms(mat, program, driver);
		/*if (mat.vertexShaderName.contains("RailRender")){
			((GL2RenderDriver)driver).gl.glPolygonMode(GL2.GL_FRONT_AND_BACK, GL2.GL_LINE);
		}
		else {
			((GL2RenderDriver)driver).gl.glPolygonMode(GL2.GL_FRONT_AND_BACK, GL2.GL_FILL);
		}*/
	}

	@Override
	public default void setMaterialShadingStageCount(int count, IRenderDriver driver, ShaderProgram program) {
		driver.uniform1i(program.getUniformLocation(HoustonUniforms.TEV_ACTIVE_STAGE_COUNT_OVERRIDE, driver), count);
	}

	public static void passTextureMapperUniforms(Material mat, ShaderProgram program, IRenderDriver gl) {
		int[] mapModes = new int[Math.min(4, mat.textures.size())];
		for (int i = 0; i < mapModes.length; i++) {
			mapModes[i] = mat.textures.get(i).mapMode.ordinal();
		}
		if (mapModes.length > 0) {
			gl.uniform1iv(program.getUniformLocation(HoustonUniforms.TEX_SAMPLER_MAP_MODES, gl), mapModes);
		}
	}
}
