
package ctrmap.renderer.backends.base.flow;

import ctrmap.renderer.backends.base.shaderengine.ShaderProgram;
import ctrmap.renderer.scene.Light;
import ctrmap.renderer.scene.model.Mesh;
import ctrmap.renderer.scene.texturing.Material;
import xstandard.math.vec.Matrix4;
import xstandard.math.vec.Vec4f;
import org.joml.Matrix3f;

public interface IShaderAdapter {
	public void setUpMatrices(Matrix4 model, Matrix4 view, Matrix4 projection, Matrix3f normal, ShaderProgram program, IRenderDriver driver);
	
	public void setUpTextureAssignments(int[] assignments, IRenderDriver driver, ShaderProgram program);
	
	public void setUpLUTAssignments(int[] assignments, IRenderDriver driver, ShaderProgram program);
	public void setLUTNeedsTangentUniform(boolean value, IRenderDriver driver, ShaderProgram program);

	public void setUpMeshUVAssignments(int[] assignments, IRenderDriver driver, ShaderProgram program);	
	public void setUpMeshBoolUniforms(Mesh mesh, IRenderDriver driver, ShaderProgram program);
	public void setUpMeshBlendWeight(float value, IRenderDriver driver, ShaderProgram program);
	
	public void setUpTextureTransforms(Matrix4[] matrices, IRenderDriver driver, ShaderProgram program);
	public void setUpSkeletalTransforms(Matrix4[] matrices, IRenderDriver driver, ShaderProgram program);
	
	public void setMaterialShadingStageCount(int count, IRenderDriver driver, ShaderProgram program);
	public void setUpMaterialUniforms(Material mat, IRenderDriver driver, ShaderProgram program);
	public void setUpMaterialLightingColors(Vec4f[] colorVectors, IRenderDriver driver, ShaderProgram program);
	public void setUpMaterialConstantColors(Vec4f[] colorVectors, IRenderDriver driver, ShaderProgram program);
	
	public void setUpLights(Light[] lights, Matrix4[] lightMatrices, IRenderDriver driver, ShaderProgram program);
}
