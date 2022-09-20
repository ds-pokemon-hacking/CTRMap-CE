package ctrmap.creativestudio.editors;

import ctrmap.CTRMapResources;
import ctrmap.renderer.scene.Scene;
import ctrmap.renderer.scene.metadata.uniforms.CustomUniformFloat;
import ctrmap.renderer.scene.metadata.uniforms.CustomUniformVec3;
import ctrmap.renderer.scene.model.Mesh;
import ctrmap.renderer.scene.model.Model;
import ctrmap.renderer.scene.model.ModelInstance;
import ctrmap.renderer.scene.model.PrimitiveType;
import ctrmap.renderer.scene.model.Vertex;
import ctrmap.renderer.scene.texturing.Material;
import ctrmap.renderer.scene.texturing.MaterialColorType;
import ctrmap.renderer.scene.texturing.TexEnvStage;
import ctrmap.renderer.util.generators.BoundingBoxGenerator;
import ctrmap.renderer.util.generators.GridGenerator;
import xstandard.gui.components.ComponentUtils;
import xstandard.math.vec.RGBA;
import xstandard.math.vec.Vec3f;
import xstandard.res.ResourceAccess;
import ctrmap.util.gui.cameras.OrthoCameraInputManager;

public class CurvePlayground extends javax.swing.JFrame {

	OrthoCameraInputManager cam = new OrthoCameraInputManager();
	
	Scene scene;
	
	Vec3f hermitePoint1 = new Vec3f(-10, 0, 0);
	Vec3f hermitePoint2 = new Vec3f(10, 0, 0);
	Vec3f hermiteTangent1 = new Vec3f(1, 0, 20);
	Vec3f hermiteTangent2 = new Vec3f(1, 0, 20);
	Vec3f hermiteSlope1 = new Vec3f(0, 0, 1);
	Vec3f hermiteSlope2 = new Vec3f(0, 0, 1);
	
	PointModel pmdl1 = new PointModel(hermitePoint1, hermiteTangent1, false);
	PointModel pmdl2 = new PointModel(hermitePoint2, hermiteTangent2, false);
	PointModel tmdl1 = new PointModel(hermitePoint1, hermiteTangent1, true);
	PointModel tmdl2 = new PointModel(hermitePoint2, hermiteTangent2, true);
	
	/**
	 * Creates new form CurvePlayground
	 */
	public CurvePlayground() {
		initComponents();
		
		CTRMapResources.load();
		
		renderer.getProgramManager().getUserShManager().addIncludeDirectory(ResourceAccess.getResourceFile("cs/shader"));
		
		scene = renderer.getScene();
		cam.addToScene(scene);
		cam.attachComponent(renderer);
		
		scene.resource.merge(GridGenerator.generateGrid(1f, -1f, 100, 2, new RGBA(50, 50, 50, 255), true));
		
		scene.resource.models.add(createNormalHermiteModel());
		scene.resource.models.add(createSlopeHermiteModel());
		
		scene.addModel(pmdl1);
		scene.addModel(pmdl2);
		scene.addModel(tmdl1);
		scene.addModel(tmdl2);
		
		cam.setTY(50f);
	}
	
	private Model createNormalHermiteModel() {
		Model mdl = new Model();
		mdl.name = "NormalHermite";
		
		Material mat = new Material();
		mat.name = "NormalHermiteMat";
		mat.addShaderExtension("CurvePlaygroundShaderNormal.vsh_ext");
		mat.tevStages.stages[0] = new TexEnvStage(TexEnvStage.TexEnvTemplate.CCOL);
		mat.tevStages.stages[0].constantColor = MaterialColorType.CONSTANT0;
		mat.getMaterialColor(MaterialColorType.CONSTANT0).set(1f, 1f, 1f, 1f);
		
		mdl.addMaterial(mat);
		
		Mesh mesh = new Mesh();
		mesh.materialName = mat.name;
		mesh.primitiveType = PrimitiveType.LINESTRIPS;
		
		for (int i = 0; i <= 100; i++) {
			Vertex v = new Vertex();
			v.position.x = i / 100f;
			mesh.vertices.add(v);
		}
		
		mesh.metaData.putValue(new CustomUniformVec3("p1") {
			@Override
			public Vec3f vec3Value() {
				return hermitePoint1;
			}
		});
		
		mesh.metaData.putValue(new CustomUniformVec3("p2") {
			@Override
			public Vec3f vec3Value() {
				return hermitePoint2;
			}
		});
		
		mesh.metaData.putValue(new CustomUniformVec3("t1") {
			@Override
			public Vec3f vec3Value() {
				return hermiteTangent1;
			}
		});
		mesh.metaData.putValue(new CustomUniformVec3("t2") {
			@Override
			public Vec3f vec3Value() {
				return hermiteTangent2;
			}
		});
		
		
		mdl.addMesh(mesh);
		
		return mdl;
	}
	
	private Model createSlopeHermiteModel() {
		Model mdl = new Model();
		mdl.name = "SlopeHermite";
		
		Material mat = new Material();
		mat.name = "SlopeHermiteMat";
		mat.addShaderExtension("CurvePlaygroundShaderSlope.vsh_ext");
		mat.tevStages.stages[0] = new TexEnvStage(TexEnvStage.TexEnvTemplate.CCOL);
		mat.tevStages.stages[0].constantColor = MaterialColorType.CONSTANT0;
		mat.getMaterialColor(MaterialColorType.CONSTANT0).set(0f, 1f, 0f, 1f);
		
		mdl.addMaterial(mat);
		
		Mesh mesh = new Mesh();
		mesh.materialName = mat.name;
		mesh.primitiveType = PrimitiveType.LINESTRIPS;
		
		for (int i = 0; i <= 100; i++) {
			Vertex v = new Vertex();
			v.position.x = i / 100f;
			mesh.vertices.add(v);
		}
		
		mesh.metaData.putValue(new CustomUniformVec3("p1") {
			@Override
			public Vec3f vec3Value() {
				return hermitePoint1;
			}
		});
		
		mesh.metaData.putValue(new CustomUniformVec3("p2") {
			@Override
			public Vec3f vec3Value() {
				return hermitePoint2;
			}
		});
		
		mesh.metaData.putValue(new CustomUniformVec3("t1") {
			@Override
			public Vec3f vec3Value() {
				return hermiteSlope1;
			}
		});
		mesh.metaData.putValue(new CustomUniformVec3("t2") {
			@Override
			public Vec3f vec3Value() {
				return hermiteSlope2;
			}
		});
		mesh.metaData.putValue(new CustomUniformFloat("diff") {
			@Override
			public float floatValue() {
				return 20f;
			}
		});
		
		
		mdl.addMesh(mesh);
		
		return mdl;
	}
	
	private static class PointModel extends ModelInstance {
		
		private final Vec3f point;
		private final Vec3f tangent;
		private final boolean isTangent;
		
		public PointModel(Vec3f point, Vec3f tangent, boolean isTangent) {
			this.point = point;
			this.tangent = tangent;
			this.isTangent = isTangent;
			Mesh mesh = BoundingBoxGenerator.generateBBox(1f, 1f, 1f, true, false, 0, isTangent ? new RGBA(0, 0, 255, 255) : new RGBA(255, 0, 0, 255));
			Model mdl = new Model();
			mdl.addMesh(mesh);
			resource.addModel(mdl);
		}
		
		@Override
		public Vec3f getPosition() {
			if (isTangent) {
				return point.clone().add(tangent);
			}
			return point;
		}
	}

	/**
	 * This method is called from within the constructor to initialize the form. WARNING: Do NOT modify this code. The content of this method is always regenerated by the Form Editor.
	 */
	@SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        renderer = new ctrmap.renderer.backends.RenderSurface();
        jLabel1 = new javax.swing.JLabel();
        tangent = new javax.swing.JFormattedTextField();
        jLabel2 = new javax.swing.JLabel();
        slope = new javax.swing.JFormattedTextField();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        jLabel1.setText("Tangent:");

        tangent.setFormatterFactory(new javax.swing.text.DefaultFormatterFactory(new javax.swing.text.NumberFormatter(new java.text.DecimalFormat("#0.00"))));

        jLabel2.setText("Slope:");

        slope.setFormatterFactory(new javax.swing.text.DefaultFormatterFactory(new javax.swing.text.NumberFormatter(new java.text.DecimalFormat("#0.00"))));

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(renderer, javax.swing.GroupLayout.DEFAULT_SIZE, 467, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel2)
                    .addComponent(slope, javax.swing.GroupLayout.PREFERRED_SIZE, 108, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(tangent, javax.swing.GroupLayout.PREFERRED_SIZE, 108, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel1))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(renderer, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel1)
                .addGap(5, 5, 5)
                .addComponent(tangent, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel2)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(slope, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(217, Short.MAX_VALUE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

	/**
	 * @param args the command line arguments
	 */
	public static void main(String args[]) {
		ComponentUtils.setSystemNativeLookAndFeel();

		/* Create and display the form */
		java.awt.EventQueue.invokeLater(new Runnable() {
			public void run() {
				new CurvePlayground().setVisible(true);
			}
		});
	}

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private ctrmap.renderer.backends.RenderSurface renderer;
    private javax.swing.JFormattedTextField slope;
    private javax.swing.JFormattedTextField tangent;
    // End of variables declaration//GEN-END:variables
}
