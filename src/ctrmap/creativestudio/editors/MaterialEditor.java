package ctrmap.creativestudio.editors;

import xstandard.math.vec.RGBA;
import static ctrmap.creativestudio.editors.MaterialEditorTextRsrc.*;
import ctrmap.creativestudio.ngcs.tree.CSNode;
import ctrmap.editor.gui.editors.scenegraph.editors.IScenegraphEditor;
import ctrmap.renderer.scene.Scene;
import ctrmap.renderer.scene.metadata.GFLMetaData;
import ctrmap.renderer.scene.metadata.MetaDataValue;
import ctrmap.renderer.scene.model.Mesh;
import ctrmap.renderer.scene.texturing.LUT;
import ctrmap.renderer.scene.texturing.Material;
import ctrmap.renderer.scene.texturing.MaterialColorType;
import ctrmap.renderer.scene.texturing.MaterialParams;
import ctrmap.renderer.scene.texturing.TexEnvConfig;
import ctrmap.renderer.scene.texturing.TexEnvStage;
import ctrmap.renderer.scene.texturing.TextureMapper;
import ctrmap.renderer.util.MaterialProcessor;
import xstandard.gui.components.ComponentUtils;
import xstandard.gui.components.listeners.DocumentAdapterEx;
import xstandard.gui.components.listeners.ToggleableActionListener;
import xstandard.gui.components.listeners.ToggleableChangeListener;
import xstandard.gui.components.listeners.ToggleableDocumentAdapter;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.util.Objects;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JColorChooser;
import javax.swing.event.ChangeEvent;
import javax.swing.event.DocumentEvent;

public class MaterialEditor extends javax.swing.JPanel implements IEditor, ISceneBound {

	private Scene scene;
	private CSNode node;

	private DefaultComboBoxModel<TextureMapper> textureMappersModel = new DefaultComboBoxModel<>();

	private ToggleableActionListener tevAlAll = new ToggleableActionListener() {
		@Override
		public void actionPerformedImpl(ActionEvent e) {
			saveShading();
		}
	};

	private ToggleableActionListener texAlAll = new ToggleableActionListener() {
		@Override
		public void actionPerformedImpl(ActionEvent e) {
			saveTexture();
		}
	};
	private ToggleableChangeListener texClAll = new ToggleableChangeListener() {
		@Override
		public void onApprovedStateChange(ChangeEvent e) {
			saveTexture();
		}
	};
	private ToggleableChangeListener lightClAll = new ToggleableChangeListener() {
		@Override
		public void onApprovedStateChange(ChangeEvent e) {
			saveLighting();
		}
	};
	private ToggleableActionListener rsAlAll = new ToggleableActionListener() {
		@Override
		public void actionPerformedImpl(ActionEvent e) {
			saveRenderState();
		}
	};

	private ToggleableDocumentAdapter texSRTListener = new ToggleableDocumentAdapter() {
		@Override
		public void textChangedUpdateImpl(DocumentEvent e) {
			if (mat != null) {
				if (currentTexIdx >= 0 && currentTexIdx < mat.textures.size()) {
					TextureMapper tm = mat.textures.get(currentTexIdx);

					tm.bindTranslation.x = ComponentUtils.getFloatFromDocument(textureTX);
					tm.bindTranslation.y = ComponentUtils.getFloatFromDocument(textureTY);
					tm.bindRotation = ComponentUtils.getFloatFromDocument(textureRot);
					tm.bindScale.x = ComponentUtils.getFloatFromDocument(textureSX);
					tm.bindScale.y = ComponentUtils.getFloatFromDocument(textureSY);
				}
			}
		}
	};

	private ToggleableDocumentAdapter rsIntegerListener = new ToggleableDocumentAdapter() {
		@Override
		public void textChangedUpdateImpl(DocumentEvent e) {
			if (mat != null) {
				mat.alphaTest.reference = ComponentUtils.getIntFromDocument(alphaTestReference);
				mat.stencilTest.bufferMask = ComponentUtils.getIntFromDocument(stencilBufMask);
				mat.stencilTest.funcMask = ComponentUtils.getIntFromDocument(stencilMask);
				mat.stencilTest.reference = ComponentUtils.getIntFromDocument(stencilRef);
			}
		}
	};

	public MaterialEditor() {
		this(null);
	}

	public MaterialEditor(Scene scene) {
		initComponents();
		this.scene = scene;

		ComponentUtils.setNFValueClass(Float.class, textureTX, textureTY, textureRot, textureSX, textureSY);
		ComponentUtils.setNFValueClass(Integer.class, alphaTestReference, stencilBufMask, stencilMask, stencilRef, edgeGroup);
		saveCtrl.setCallback(this::save);
		textureName.setLUTOrNot(false);
		textureName.bindScene(scene);
		lutTextureName.setLUTOrNot(true);
		lutTextureName.bindScene(scene);
		bakeTexName.setLUTOrNot(false);
		bakeTexName.bindScene(scene);

		textureUV.loadComboBoxValues(new String[]{"UV0", "UV1", "UV2", "Cube Map", "Sphere map", "Projection map"});

		materialName.getDocument().addDocumentListener(new DocumentAdapterEx() {
			@Override
			public void textChangedUpdate(DocumentEvent e) {
				if (mat != null) {
					String newName = ComponentUtils.getDocTextFromField(materialName);
					if (!newName.equals(mat.name) && mat.parentModel != null) {
						for (Mesh mesh : mat.parentModel.meshes) {
							if (Objects.equals(mesh.materialName, mat.name)) {
								mesh.materialName = newName;
							}
						}
						mat.name = newName;
						if (node != null) {
							node.updateThis();
						}
					}
				}
			}
		});

		ComponentUtils.addActionListener(tevAlAll, cModeA, cModeRgb, cSrc0A, cSrc0Rgb, cSrc1A, cSrc1Rgb, cSrc2A, cSrc2Rgb, cOp0A, cOp0Rgb, cOp1A, cOp1Rgb, cOp2A, cOp2Rgb, cScaleA, cScaleRgb, constantAsgn);
		ComponentUtils.addActionListener(tevAlAll, cUpdateA, cUpdateRgb);
		shaderArcName.addActionListener(tevAlAll);

		ComponentUtils.addDocumentListenerToTFs(texSRTListener, textureRot, textureSX, textureSY, textureTX, textureTY);

		ComponentUtils.addActionListener(texAlAll, textureMagFilter, textureMinFilter, textureWrapU, textureWrapV);
		textureUV.addChangeListener(texClAll);

		ComponentUtils.addChangeListener(lightClAll, lightLayer, lightSetIndex);
		ComponentUtils.addActionListener(rsAlAll, depthFunc, boDstFuncAlpha, boDstFuncRgb, boEqtAlpha, boEqtRgb, boSrcFuncAlpha, boSrcFuncRgb, stencilFunc, soFail, soPass, soZPass, faceCulling, alphaTestFunc);
		ComponentUtils.addActionListener(rsAlAll, dtAlphaWrite, dtBlueWrite, dtDepthWrite, dtGreenWrite, dtRedWrite, dtEnabled, blendOpEnabled, stencilEnabled, alphaTestEnabled);
		ComponentUtils.addDocumentListenerToTFs(rsIntegerListener, stencilBufMask, stencilMask, stencilRef, alphaTestReference);
	}

	private Material mat;

	@Override
	public void handleObject(Object o) {
		currentStageIdx = -1;
		currentTexIdx = -1;

		if (IEditor.checkIsCompatibleNG(o, Material.class) || o instanceof Material) {
			if (o instanceof CSNode) {
				node = (CSNode) o;
				mat = (Material) node.getContent();
			} else {
				mat = (Material) o;
			}
			materialName.setText(mat.name);
			buildTexMapperBox();
			textureMapper.setSelectedIndex(Math.min(0, textureMapper.getItemCount() - 1));
			showTexture();
			showRenderState();
			showMetaData();
			showLighting();
			shadingStage.setSelectedIndex(Math.max(0, shadingStage.getSelectedIndex()));
		} else {
			mat = null;
			ComponentUtils.clearComponents(
				textureName, textureUV, textureMagFilter, textureMinFilter, textureWrapU, textureWrapV,
				textureTX, textureTY, textureRot, textureMapper,
				alphaTestEnabled, alphaTestFunc, alphaTestReference, dtEnabled, depthFunc, dtRedWrite, dtGreenWrite, dtBlueWrite, dtAlphaWrite,
				dtDepthWrite, blendOpEnabled, boEqtRgb, boEqtAlpha, boSrcFuncRgb, boSrcFuncAlpha, boDstFuncRgb, boDstFuncAlpha,
				shadingStage, cModeRgb, cModeA, cEqtPreviewRgb, cEqtPreviewA, cUpdateRgb, cUpdateA, cScaleRgb, cScaleA, cSrc0Rgb,
				cSrc0A, cSrc1Rgb, cSrc1A, cSrc2Rgb, cSrc2A, cOp0Rgb, cOp0A, cOp1Rgb, cOp1A, cOp2Rgb, cOp2A, stencilBufMask, stencilEnabled,
				stencilFunc, stencilMask, stencilRef, soFail, soPass, soZPass, bumpMode, btnBumpMap, shaderArcName, lightSetIndex, faceCulling, lutInput, lutTextureName
			);
			materialName.setText(null);
			textureMapper.removeAllItems();
			textureSX.setValue(1f);
			textureSY.setValue(1f);
			setBlendColor();
			setInputBufferColor();
			setLightingColors();
			setTEVConstantColor(0);
			edgeGroup.setValue(255);
			isEdgeEnabled.setSelected(true);
			metaDataEditor.loadMetaData(null);
		}
	}

	@Override
	public void save() {
		if (mat != null) {
			saveTexture();
			saveRenderState();
			saveShading();
			saveLighting();
		}
	}

	private int currentStageIdx = -1;
	private int currentTexIdx = -1;

	private void saveTexture() {
		if (mat != null) {
			int i = currentTexIdx;

			if (i != -1 && i < mat.textures.size()) {
				TextureMapper m = mat.textures.get(i);
				String texName = textureName.getTextureName();
				m.textureName = texName;
				m.uvSetNo = textureUV.getValueSpinner();
				textureMapper.repaint();
				//Since CreativeStudio is a 3DS-focused tool, we use the same UvSetNo attributes as H3D
				switch (m.uvSetNo) {
					case 3:
						m.mapMode = MaterialParams.TextureMapMode.CUBE_MAP;
						break;
					case 4:
						m.mapMode = MaterialParams.TextureMapMode.SPHERE_MAP;
						break;
					case 5:
						m.mapMode = MaterialParams.TextureMapMode.PROJECTION_MAP;
						break;
					default:
						m.mapMode = MaterialParams.TextureMapMode.UV_MAP;
						break;
				}
				/*m.bindTranslation.x = (Float) textureTX.getValue();
				m.bindTranslation.y = (Float) textureTY.getValue();
				m.bindRotation = (Float) textureRot.getValue();
				m.bindScale.x = (Float) textureSX.getValue();
				m.bindScale.y = (Float) textureSY.getValue();*/
				m.mapU = MaterialParams.TextureWrap.values()[textureWrapU.getSelectedIndex()];
				m.mapV = MaterialParams.TextureWrap.values()[textureWrapV.getSelectedIndex()];
				m.textureMagFilter = MaterialParams.TextureMagFilter.values()[textureMagFilter.getSelectedIndex()];
				m.textureMinFilter = MaterialParams.TextureMinFilter.values()[textureMinFilter.getSelectedIndex()];
			}
		}
	}

	private void saveLighting() {
		if (mat != null) {
			mat.lightSetIndex = ((Number) lightSetIndex.getValue()).intValue();
			mat.lightingLayer = ((Number) lightLayer.getValue()).intValue();
		}
	}

	private void saveRenderState() {
		if (mat != null) {
			mat.alphaTest.enabled = alphaTestEnabled.isSelected();
			//mat.alphaTest.reference = (Integer) alphaTestReference.getValue();
			mat.alphaTest.testFunction = MaterialParams.TestFunction.values()[alphaTestFunc.getSelectedIndex()];

			mat.depthColorMask.enabled = dtEnabled.isSelected();
			mat.depthColorMask.depthFunction = MaterialParams.TestFunction.values()[depthFunc.getSelectedIndex()];
			mat.depthColorMask.redWrite = dtRedWrite.isSelected();
			mat.depthColorMask.greenWrite = dtGreenWrite.isSelected();
			mat.depthColorMask.blueWrite = dtBlueWrite.isSelected();
			mat.depthColorMask.alphaWrite = dtAlphaWrite.isSelected();
			mat.depthColorMask.depthWrite = dtDepthWrite.isSelected();

			mat.blendOperation.enabled = blendOpEnabled.isSelected();
			mat.blendOperation.colorEquation = MaterialParams.BlendEquation.values()[boEqtRgb.getSelectedIndex()];
			mat.blendOperation.alphaEquation = MaterialParams.BlendEquation.values()[boEqtAlpha.getSelectedIndex()];
			mat.blendOperation.colorSrcFunc = MaterialParams.BlendFunction.values()[boSrcFuncRgb.getSelectedIndex()];
			mat.blendOperation.alphaSrcFunc = MaterialParams.BlendFunction.values()[boSrcFuncAlpha.getSelectedIndex()];
			mat.blendOperation.colorDstFunc = MaterialParams.BlendFunction.values()[boDstFuncRgb.getSelectedIndex()];
			mat.blendOperation.alphaDstFunc = MaterialParams.BlendFunction.values()[boDstFuncAlpha.getSelectedIndex()];

			mat.stencilTest.enabled = stencilEnabled.isSelected();
			mat.stencilTest.testFunction = MaterialParams.TestFunction.values()[stencilFunc.getSelectedIndex()];
			//mat.stencilTest.reference = (Integer) stencilRef.getValue();
			//mat.stencilTest.funcMask = (Integer) stencilMask.getValue();
			//mat.stencilTest.bufferMask = (Integer) stencilBufMask.getValue();

			mat.stencilOperation.fail = MaterialParams.StencilOp.values()[soFail.getSelectedIndex()];
			mat.stencilOperation.zFail = MaterialParams.StencilOp.values()[soPass.getSelectedIndex()];
			mat.stencilOperation.zPass = MaterialParams.StencilOp.values()[soZPass.getSelectedIndex()];

			mat.faceCulling = MaterialParams.FaceCulling.values()[faceCulling.getSelectedIndex()];
		}
	}

	private void showLighting() {
		if (mat != null) {
			lightClAll.setAllowEvents(false);
			lightSetIndex.setValue(mat.lightSetIndex);
			lightLayer.setValue(mat.lightingLayer);
			lightClAll.setAllowEvents(true);
			setLightingColors();
			showLUT();
		}
	}

	private void buildTexMapperBox() {
		textureMapper.removeAllItems();
		if (mat != null) {
			int idx = 0;
			for (TextureMapper m : mat.textures) {
				textureMappersModel.addElement(m);
				idx++;
			}
		}
	}

	private void showTexture() {
		saveTexture();
		int idx = textureMapper.getSelectedIndex();
		currentTexIdx = idx;
		if (idx != -1 && idx < mat.textures.size()) {
			TextureMapper m = mat.textures.get(idx);
			int uv = m.uvSetNo;
			switch (m.mapMode) {
				case CUBE_MAP:
					uv = 3;
					break;
				case SPHERE_MAP:
					uv = 4;
					break;
				case PROJECTION_MAP:
					uv = 5;
					break;
			}
			textureName.setTextureName(m.textureName);
			if (scene != null) {
				texturePreview.showTexture(scene.getResTexture(m.textureName));
			}
			texClAll.setAllowEvents(false);
			texAlAll.setAllowEvents(false);
			texSRTListener.setAllowEvents(false);
			textureUV.setValue(uv);
			textureTX.setValue(m.bindTranslation.x);
			textureTY.setValue(m.bindTranslation.y);
			textureSX.setValue(m.bindScale.x);
			textureSY.setValue(m.bindScale.y);
			textureRot.setValue(m.bindRotation);
			texSRTListener.setAllowEvents(true);
			textureWrapU.setSelectedIndex(m.mapU.ordinal());
			textureWrapV.setSelectedIndex(m.mapV.ordinal());
			textureMagFilter.setSelectedIndex(m.textureMagFilter.ordinal());
			textureMinFilter.setSelectedIndex(m.textureMinFilter.ordinal());
			texAlAll.setAllowEvents(true);
			texClAll.setAllowEvents(true);
			btnBumpMap.setSelected(mat.bumpTextureIndex == idx && mat.bumpMode != MaterialParams.BumpMode.NONE);
			if (mat.bumpMode == MaterialParams.BumpMode.TANGENT) {
				bumpMode.setSelectedIndex(1);
			} else {
				bumpMode.setSelectedIndex(0);
			}
			setBumpModeEnabled();
		}
		texEnableSeq();
		texBtnEnableSeq();
	}

	private void saveShading() {
		if (mat != null) {
			if (currentStageIdx != -1) {
				mat.vertexShaderName = shaderArcName.getText();
				TexEnvStage s = mat.tevStages.stages[currentStageIdx];
				s.rgbCombineOperator = TexEnvConfig.PICATextureCombinerMode.values()[cModeRgb.getSelectedIndex()];
				s.alphaCombineOperator = TexEnvConfig.PICATextureCombinerMode.values()[cModeA.getSelectedIndex()];
				s.rgbScale = TexEnvConfig.Scale.values()[cScaleRgb.getSelectedIndex()];
				s.alphaScale = TexEnvConfig.Scale.values()[cScaleA.getSelectedIndex()];
				s.writeColorBuffer = cUpdateRgb.isSelected();
				s.writeAlphaBuffer = cUpdateA.isSelected();
				s.rgbSource[0] = getSourceEnumForStrIndex(cSrc0Rgb.getSelectedIndex());
				s.rgbSource[1] = getSourceEnumForStrIndex(cSrc1Rgb.getSelectedIndex());
				s.rgbSource[2] = getSourceEnumForStrIndex(cSrc2Rgb.getSelectedIndex());
				s.alphaSource[0] = getSourceEnumForStrIndex(cSrc0A.getSelectedIndex());
				s.alphaSource[1] = getSourceEnumForStrIndex(cSrc1A.getSelectedIndex());
				s.alphaSource[2] = getSourceEnumForStrIndex(cSrc2A.getSelectedIndex());
				s.rgbOperand[0] = getColorOpEnumForStrIndex(cOp0Rgb.getSelectedIndex());
				s.rgbOperand[1] = getColorOpEnumForStrIndex(cOp1Rgb.getSelectedIndex());
				s.rgbOperand[2] = getColorOpEnumForStrIndex(cOp2Rgb.getSelectedIndex());
				s.alphaOperand[0] = TexEnvConfig.PICATextureCombinerAlphaOp.values()[cOp0A.getSelectedIndex()];
				s.alphaOperand[1] = TexEnvConfig.PICATextureCombinerAlphaOp.values()[cOp1A.getSelectedIndex()];
				s.alphaOperand[2] = TexEnvConfig.PICATextureCombinerAlphaOp.values()[cOp2A.getSelectedIndex()];
			}
		}
	}

	private void showRenderState() {
		if (mat != null) {
			rsAlAll.setAllowEvents(false);
			rsIntegerListener.setAllowEvents(false);

			alphaTestEnabled.setSelected(mat.alphaTest.enabled);
			alphaTestFunc.setSelectedIndex(mat.alphaTest.testFunction.ordinal());
			alphaTestReference.setValue(mat.alphaTest.reference);

			dtEnabled.setSelected(mat.depthColorMask.enabled);
			depthFunc.setSelectedIndex(mat.depthColorMask.depthFunction.ordinal());
			dtRedWrite.setSelected(mat.depthColorMask.redWrite);
			dtGreenWrite.setSelected(mat.depthColorMask.greenWrite);
			dtBlueWrite.setSelected(mat.depthColorMask.blueWrite);
			dtAlphaWrite.setSelected(mat.depthColorMask.alphaWrite);
			dtDepthWrite.setSelected(mat.depthColorMask.depthWrite);

			blendOpEnabled.setSelected(mat.blendOperation.enabled);
			boEnableSeq();
			setBlendColor();
			boEqtRgb.setSelectedIndex(mat.blendOperation.colorEquation.ordinal());
			boEqtAlpha.setSelectedIndex(mat.blendOperation.alphaEquation.ordinal());
			boSrcFuncRgb.setSelectedIndex(mat.blendOperation.colorSrcFunc.ordinal());
			boSrcFuncAlpha.setSelectedIndex(mat.blendOperation.alphaSrcFunc.ordinal());
			boDstFuncRgb.setSelectedIndex(mat.blendOperation.colorDstFunc.ordinal());
			boDstFuncAlpha.setSelectedIndex(mat.blendOperation.alphaDstFunc.ordinal());

			stencilEnabled.setSelected(mat.stencilTest.enabled);
			stencilFunc.setSelectedIndex(mat.stencilTest.testFunction.ordinal());
			stencilRef.setValue(mat.stencilTest.reference);
			stencilMask.setValue(mat.stencilTest.funcMask);
			stencilBufMask.setValue(mat.stencilTest.bufferMask);

			soFail.setSelectedIndex(mat.stencilOperation.fail.ordinal());
			soPass.setSelectedIndex(mat.stencilOperation.zFail.ordinal());
			soZPass.setSelectedIndex(mat.stencilOperation.zPass.ordinal());

			faceCulling.setSelectedIndex(mat.faceCulling.ordinal());

			rsAlAll.setAllowEvents(true);
			rsIntegerListener.setAllowEvents(true);
		}
	}

	private void showTEV(int stage) {
		showTEV(stage, false);
	}

	private void showTEV(int stage, boolean noSave) {
		if (!noSave) {
			saveShading();
		}
		if (mat != null) {
			shaderArcName.setText(mat.vertexShaderName);

			currentStageIdx = stage;
			setInputBufferColor();

			tevAlAll.setAllowEvents(false);

			TexEnvStage s = mat.tevStages.stages[stage];
			cModeRgb.setSelectedIndex(s.rgbCombineOperator.ordinal());
			cModeA.setSelectedIndex(s.alphaCombineOperator.ordinal());
			setTEVEquations(stage);

			cScaleRgb.setSelectedIndex(s.rgbScale.ordinal());
			cScaleA.setSelectedIndex(s.alphaScale.ordinal());
			cUpdateRgb.setSelected(s.writeColorBuffer);
			cUpdateA.setSelected(s.writeAlphaBuffer);

			cSrc0Rgb.setSelectedIndex(getSourceStrIndexForEnum(s.rgbSource[0]));
			cSrc1Rgb.setSelectedIndex(getSourceStrIndexForEnum(s.rgbSource[1]));
			cSrc2Rgb.setSelectedIndex(getSourceStrIndexForEnum(s.rgbSource[2]));
			cSrc0A.setSelectedIndex(getSourceStrIndexForEnum(s.alphaSource[0]));
			cSrc1A.setSelectedIndex(getSourceStrIndexForEnum(s.alphaSource[1]));
			cSrc2A.setSelectedIndex(getSourceStrIndexForEnum(s.alphaSource[2]));

			cOp0Rgb.setSelectedIndex(getColorOpStrIndexForEnum(s.rgbOperand[0]));
			cOp1Rgb.setSelectedIndex(getColorOpStrIndexForEnum(s.rgbOperand[1]));
			cOp2Rgb.setSelectedIndex(getColorOpStrIndexForEnum(s.rgbOperand[2]));
			cOp0A.setSelectedIndex(s.alphaOperand[0].ordinal());
			cOp1A.setSelectedIndex(s.alphaOperand[1].ordinal());
			cOp2A.setSelectedIndex(s.alphaOperand[2].ordinal());

			constantAsgn.setSelectedIndex(s.constantColor.ordinal());

			tevAlAll.setAllowEvents(true);
		}
	}

	public void setTEVEquations(int stage) {
		cEqtPreviewRgb.setText("clamp" + combinerModeEquations[Math.max(0, cModeRgb.getSelectedIndex())]);
		cEqtPreviewA.setText("clamp" + combinerModeEquations[Math.max(0, cModeA.getSelectedIndex())]);
	}

	public void showMetaData() {
		metaDataEditor.loadMetaData(mat.metaData);

		MetaDataValue gfl_EdgeEnable = mat.metaData.getValue(GFLMetaData.GFL_EDGE_ENABLE);
		MetaDataValue gfl_EdgeID = mat.metaData.getValue(GFLMetaData.GFL_EDGE_ID);
		if (gfl_EdgeEnable != null) {
			isEdgeEnabled.setSelected(gfl_EdgeEnable.intValue() != 0);
		}
		if (gfl_EdgeID != null) {
			edgeGroup.setValue(gfl_EdgeID.intValue());
		}
	}

	private void setBlendColor() {
		boColorPreview.setBackground(mat != null ? getAlphadColor(mat.blendOperation.blendColor) : Color.WHITE);
	}

	private void setInputBufferColor() {
		inBufColPreview.setBackground(mat != null ? getAlphadColor(mat.tevStages.inputBufferColor) : Color.WHITE);
	}

	private void setTEVConstantColor(int stage) {
		constantColorPicker.attachColor(mat != null ? mat.getMaterialColor(mat.tevStages.stages[stage].constantColor) : RGBA.WHITE);
	}

	private void setLightingColors() {
		ambientColorPreview.setBackground(mat != null ? getAlphadColor(mat.ambientColor) : Color.WHITE);
		diffuseColorPreview.setBackground(mat != null ? getAlphadColor(mat.diffuseColor) : Color.WHITE);
		spc0ColorPreview.setBackground(mat != null ? getAlphadColor(mat.specular0Color) : Color.BLACK);
		spc1ColorPreview.setBackground(mat != null ? getAlphadColor(mat.specular0Color) : Color.BLACK);
	}

	private void setBumpModeEnabled() {
		bumpMode.setEnabled(btnBumpMap.isSelected());
	}

	private static Color getAlphadColor(RGBA c) {
		return new Color(c.r, c.g, c.b, 255);
	}

	@Override
	public void onSceneUpdate() {

	}

	/**
	 * This method is called from within the constructor to initialize the form. WARNING: Do NOT modify this code. The content of this method is always regenerated by the Form Editor.
	 */
	@SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        buttonGroup1 = new javax.swing.ButtonGroup();
        generalPanel = new javax.swing.JPanel();
        materialNameLabel = new javax.swing.JLabel();
        materialName = new javax.swing.JTextField();
        materialEditorTabbedPane = new javax.swing.JTabbedPane();
        texturingPanel = new javax.swing.JPanel();
        textureNameLabel = new javax.swing.JLabel();
        textureSelectionSeparator = new javax.swing.JSeparator();
        textureTransformPanel = new javax.swing.JPanel();
        textureTX = new javax.swing.JFormattedTextField();
        textureTransformXLabel = new javax.swing.JLabel();
        textureTransformYLabel = new javax.swing.JLabel();
        textureTY = new javax.swing.JFormattedTextField();
        textureRot = new javax.swing.JFormattedTextField();
        textureSY = new javax.swing.JFormattedTextField();
        textureSX = new javax.swing.JFormattedTextField();
        textureTranslationLabel = new javax.swing.JLabel();
        textureRotationLabel = new javax.swing.JLabel();
        textureScaleLabel = new javax.swing.JLabel();
        textureRotYDummy = new javax.swing.JFormattedTextField();
        textureMappingPanel = new javax.swing.JPanel();
        textureUVLabel = new javax.swing.JLabel();
        textureWrapULabel = new javax.swing.JLabel();
        textureWrapU = new javax.swing.JComboBox<>();
        textureWrapVLabel = new javax.swing.JLabel();
        textureWrapV = new javax.swing.JComboBox<>();
        textureUV = new xstandard.gui.components.combobox.ComboBoxAndSpinner();
        textureFilteringPanel = new javax.swing.JPanel();
        textureMagFilterLabel = new javax.swing.JLabel();
        textureMagFilter = new javax.swing.JComboBox<>();
        textureMinFilterLabel = new javax.swing.JLabel();
        textureMinFilter = new javax.swing.JComboBox<>();
        texturePreview = new ctrmap.util.gui.TexturePreview();
        bumpMapPanel = new javax.swing.JPanel();
        btnBumpMap = new javax.swing.JCheckBox();
        bumpModeLabel = new javax.swing.JLabel();
        bumpMode = new javax.swing.JComboBox<>();
        textureMapperLabel = new javax.swing.JLabel();
        textureMapper = new javax.swing.JComboBox<>();
        btnAddTextureMapper = new javax.swing.JButton();
        btnRemoveTextureMapper = new javax.swing.JButton();
        textureName = new ctrmap.creativestudio.editors.CSTextureSelector();
        renderStatePanel = new javax.swing.JPanel();
        alphaTestPanel = new javax.swing.JPanel();
        alphaTestFuncLabel = new javax.swing.JLabel();
        alphaTestFunc = new javax.swing.JComboBox<>();
        alphaTestEnabled = new javax.swing.JCheckBox();
        alphaTestReferenceLabel = new javax.swing.JLabel();
        alphaTestReference = new javax.swing.JFormattedTextField();
        depthTestPanel = new javax.swing.JPanel();
        depthFunc = new javax.swing.JComboBox<>();
        depthFuncLabel = new javax.swing.JLabel();
        dtRedWrite = new javax.swing.JCheckBox();
        dtBlueWrite = new javax.swing.JCheckBox();
        dtGreenWrite = new javax.swing.JCheckBox();
        dtAlphaWrite = new javax.swing.JCheckBox();
        dtDepthWrite = new javax.swing.JCheckBox();
        dtEnabled = new javax.swing.JCheckBox();
        blendOpPanel = new javax.swing.JPanel();
        blendOpRGBLabel = new javax.swing.JLabel();
        blendOpAlphaLabel = new javax.swing.JLabel();
        blendOpEquationLabel = new javax.swing.JLabel();
        blendOpSrcFuncLabel = new javax.swing.JLabel();
        blendOpDstFuncLabel = new javax.swing.JLabel();
        boDstFuncRgb = new javax.swing.JComboBox<>();
        boDstFuncAlpha = new javax.swing.JComboBox<>();
        boSrcFuncRgb = new javax.swing.JComboBox<>();
        boSrcFuncAlpha = new javax.swing.JComboBox<>();
        boEqtRgb = new javax.swing.JComboBox<>();
        boEqtAlpha = new javax.swing.JComboBox<>();
        blendOpEnabled = new javax.swing.JCheckBox();
        boConstantColorSet = new javax.swing.JButton();
        boColorPreview = new javax.swing.JLabel();
        blendOpColorLabel = new javax.swing.JLabel();
        stencilOpPanel = new javax.swing.JPanel();
        stencilFuncLabel = new javax.swing.JLabel();
        stencilFunc = new javax.swing.JComboBox<>();
        stencilRefLabel = new javax.swing.JLabel();
        stencilRef = new javax.swing.JFormattedTextField();
        stencilMaskLabel = new javax.swing.JLabel();
        stencilMask = new javax.swing.JFormattedTextField();
        stencilBufMaskLabel = new javax.swing.JLabel();
        stencilBufMask = new javax.swing.JFormattedTextField();
        stencilEnabled = new javax.swing.JCheckBox();
        soFailLabel = new javax.swing.JLabel();
        soPassLabel = new javax.swing.JLabel();
        soZPassLabel = new javax.swing.JLabel();
        soSep = new javax.swing.JSeparator();
        soZPass = new javax.swing.JComboBox<>();
        soPass = new javax.swing.JComboBox<>();
        soFail = new javax.swing.JComboBox<>();
        faceCullingPanel = new javax.swing.JPanel();
        faceCulling = new javax.swing.JComboBox<>();
        shadingPanel = new javax.swing.JPanel();
        texCmbPanel = new javax.swing.JPanel();
        inputBufferColorLabel = new javax.swing.JLabel();
        inBufColSetButton = new javax.swing.JButton();
        inBufColPreview = new javax.swing.JLabel();
        inBufColSeparator = new javax.swing.JSeparator();
        alphaCombinerPanel = new javax.swing.JPanel();
        cModeLabelA = new javax.swing.JLabel();
        cOp0ALabel = new javax.swing.JLabel();
        cOp1ALabel = new javax.swing.JLabel();
        cOp2ALabel = new javax.swing.JLabel();
        cOp2A = new javax.swing.JComboBox<>();
        cOp1A = new javax.swing.JComboBox<>();
        cOp0A = new javax.swing.JComboBox<>();
        cOf0Label = new javax.swing.JLabel();
        cOf1Label = new javax.swing.JLabel();
        cOf2Label = new javax.swing.JLabel();
        cModeA = new javax.swing.JComboBox<>();
        cSrc0A = new javax.swing.JComboBox<>();
        cSrc1A = new javax.swing.JComboBox<>();
        cSrc2A = new javax.swing.JComboBox<>();
        cScaleALabel = new javax.swing.JLabel();
        cScaleA = new javax.swing.JComboBox<>();
        cUpdateA = new javax.swing.JCheckBox();
        cEqtPreviewA = new javax.swing.JLabel();
        shadingStageLabel = new javax.swing.JLabel();
        shadingStage = new javax.swing.JComboBox<>();
        colorCombinerPanel = new javax.swing.JPanel();
        cModeRgbLabel = new javax.swing.JLabel();
        cOp0RgbLabel = new javax.swing.JLabel();
        cOp1RgbLabel = new javax.swing.JLabel();
        cOp2RgbLabel = new javax.swing.JLabel();
        cOp2Rgb = new javax.swing.JComboBox<>();
        cOp1Rgb = new javax.swing.JComboBox<>();
        cOp0Rgb = new javax.swing.JComboBox<>();
        cOf0RgbLabel = new javax.swing.JLabel();
        cOf1RgbLabel = new javax.swing.JLabel();
        cOf2RgbLabel = new javax.swing.JLabel();
        cModeRgb = new javax.swing.JComboBox<>();
        cSrc0Rgb = new javax.swing.JComboBox<>();
        cSrc1Rgb = new javax.swing.JComboBox<>();
        cSrc2Rgb = new javax.swing.JComboBox<>();
        cScaleRgbLabel = new javax.swing.JLabel();
        cScaleRgb = new javax.swing.JComboBox<>();
        cUpdateRgb = new javax.swing.JCheckBox();
        cEqtPreviewRgb = new javax.swing.JLabel();
        cConstColorPanel = new javax.swing.JPanel();
        ccolAsgnLabel = new javax.swing.JLabel();
        constantColorPicker = new xstandard.gui.components.SimpleColorSelector();
        constantAsgn = new javax.swing.JComboBox<>();
        vshPanel = new javax.swing.JPanel();
        shaderArcLabel = new javax.swing.JLabel();
        shaderArcName = new javax.swing.JTextField();
        lightingPanel = new javax.swing.JPanel();
        lightingGeneralPanel = new javax.swing.JPanel();
        lsiLabel = new javax.swing.JLabel();
        lightSetIndex = new javax.swing.JSpinner();
        lightLayerLabel = new javax.swing.JLabel();
        lightLayer = new javax.swing.JSpinner();
        colorPanel = new javax.swing.JPanel();
        ambientColorLabel = new javax.swing.JLabel();
        ambientColorPreview = new javax.swing.JLabel();
        btnSetAmbientColor = new javax.swing.JButton();
        diffuseColorLabel = new javax.swing.JLabel();
        diffuseColorPreview = new javax.swing.JLabel();
        btnSetDiffuseColor = new javax.swing.JButton();
        spc0ColorLabel = new javax.swing.JLabel();
        spc0ColorPreview = new javax.swing.JLabel();
        btnSetSpc0Color = new javax.swing.JButton();
        spc1ColorLabel = new javax.swing.JLabel();
        spc1ColorPreview = new javax.swing.JLabel();
        btnSetSpc1Color = new javax.swing.JButton();
        lutPanel = new javax.swing.JPanel();
        lutLabel = new javax.swing.JLabel();
        lut = new javax.swing.JComboBox<>();
        lutTextureLabel = new javax.swing.JLabel();
        lutInputLabel = new javax.swing.JLabel();
        lutInput = new javax.swing.JComboBox<>();
        lutTextureName = new ctrmap.creativestudio.editors.CSTextureSelector();
        btnIsLUTEnabled = new javax.swing.JCheckBox();
        metaDataPanel = new javax.swing.JPanel();
        metaDataEditor = new ctrmap.creativestudio.editors.MetaDataEditor();
        shortcutsPanel = new javax.swing.JPanel();
        btnCmdAlphaEnable = new javax.swing.JButton();
        btnCmdOutlineSet = new javax.swing.JButton();
        isEdgeEnabled = new javax.swing.JCheckBox();
        edgeGroup = new javax.swing.JFormattedTextField();
        edgeGroupLabel = new javax.swing.JLabel();
        outlineSep = new javax.swing.JSeparator();
        btnCmdFragLightEnable = new javax.swing.JButton();
        jLabel2 = new javax.swing.JLabel();
        btnSetDefaultSha = new javax.swing.JButton();
        btnSetBtlFldSha = new javax.swing.JButton();
        btnAddBake = new javax.swing.JButton();
        bakeTexLabel = new javax.swing.JLabel();
        bakeUVLabel = new javax.swing.JLabel();
        bakeUV = new javax.swing.JComboBox<>();
        bakeFormulaLabel = new javax.swing.JLabel();
        bakeFormula = new javax.swing.JComboBox<>();
        bakeTexName = new ctrmap.creativestudio.editors.CSTextureSelector();
        btnDisableAlphaBlend = new javax.swing.JButton();
        saveCtrl = new ctrmap.creativestudio.editors.SaveControlPanel();

        generalPanel.setBorder(javax.swing.BorderFactory.createTitledBorder("General"));

        materialNameLabel.setText("Name");

        javax.swing.GroupLayout generalPanelLayout = new javax.swing.GroupLayout(generalPanel);
        generalPanel.setLayout(generalPanelLayout);
        generalPanelLayout.setHorizontalGroup(
            generalPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(generalPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(generalPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(generalPanelLayout.createSequentialGroup()
                        .addComponent(materialNameLabel)
                        .addGap(0, 0, Short.MAX_VALUE))
                    .addComponent(materialName))
                .addContainerGap())
        );
        generalPanelLayout.setVerticalGroup(
            generalPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(generalPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(materialNameLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(materialName, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );

        textureNameLabel.setText("Texture name:");

        textureTransformPanel.setBorder(javax.swing.BorderFactory.createTitledBorder("Transform"));

        textureTX.setFormatterFactory(new javax.swing.text.DefaultFormatterFactory(new javax.swing.text.NumberFormatter(new java.text.DecimalFormat("#0.00"))));

        textureTransformXLabel.setForeground(new java.awt.Color(255, 0, 0));
        textureTransformXLabel.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        textureTransformXLabel.setText("X:");

        textureTransformYLabel.setForeground(new java.awt.Color(51, 153, 0));
        textureTransformYLabel.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        textureTransformYLabel.setText("Y:");

        textureTY.setFormatterFactory(new javax.swing.text.DefaultFormatterFactory(new javax.swing.text.NumberFormatter(new java.text.DecimalFormat("#0.00"))));

        textureRot.setFormatterFactory(new javax.swing.text.DefaultFormatterFactory(new javax.swing.text.NumberFormatter(new java.text.DecimalFormat("#0.00"))));

        textureSY.setFormatterFactory(new javax.swing.text.DefaultFormatterFactory(new javax.swing.text.NumberFormatter(new java.text.DecimalFormat("#0.00"))));

        textureSX.setFormatterFactory(new javax.swing.text.DefaultFormatterFactory(new javax.swing.text.NumberFormatter(new java.text.DecimalFormat("#0.00"))));

        textureTranslationLabel.setText("Translation");

        textureRotationLabel.setText("Rotation");

        textureScaleLabel.setText("Scale");

        textureRotYDummy.setEditable(false);
        textureRotYDummy.setFormatterFactory(new javax.swing.text.DefaultFormatterFactory(new javax.swing.text.NumberFormatter(new java.text.DecimalFormat("#0.00"))));
        textureRotYDummy.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        textureRotYDummy.setText("~");
        textureRotYDummy.setEnabled(false);

        javax.swing.GroupLayout textureTransformPanelLayout = new javax.swing.GroupLayout(textureTransformPanel);
        textureTransformPanel.setLayout(textureTransformPanelLayout);
        textureTransformPanelLayout.setHorizontalGroup(
            textureTransformPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(textureTransformPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(textureTransformPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(textureTranslationLabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(textureRotationLabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(textureScaleLabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(textureTransformPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(textureSX)
                    .addComponent(textureTransformXLabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(textureTX, javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(textureRot, javax.swing.GroupLayout.Alignment.TRAILING))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(textureTransformPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(textureSY)
                    .addComponent(textureTY, javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(textureTransformYLabel, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(textureRotYDummy))
                .addContainerGap())
        );
        textureTransformPanelLayout.setVerticalGroup(
            textureTransformPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(textureTransformPanelLayout.createSequentialGroup()
                .addGroup(textureTransformPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(textureTransformXLabel)
                    .addComponent(textureTransformYLabel))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(textureTransformPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(textureTX, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(textureTY, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(textureTranslationLabel))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(textureTransformPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(textureRot, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(textureRotationLabel)
                    .addComponent(textureRotYDummy, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(textureTransformPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(textureSX, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(textureSY, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(textureScaleLabel))
                .addContainerGap())
        );

        textureMappingPanel.setBorder(javax.swing.BorderFactory.createTitledBorder("Mapping"));

        textureUVLabel.setText("Map mode");

        textureWrapULabel.setText("Wrap U");

        textureWrapU.setModel(new javax.swing.DefaultComboBoxModel<>(textureMappingModes));

        textureWrapVLabel.setText("Wrap V");
        textureWrapVLabel.setToolTipText("");

        textureWrapV.setModel(new javax.swing.DefaultComboBoxModel<>(textureMappingModes));

        javax.swing.GroupLayout textureMappingPanelLayout = new javax.swing.GroupLayout(textureMappingPanel);
        textureMappingPanel.setLayout(textureMappingPanelLayout);
        textureMappingPanelLayout.setHorizontalGroup(
            textureMappingPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(textureMappingPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(textureMappingPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(textureUVLabel)
                    .addComponent(textureWrapULabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(textureWrapVLabel, javax.swing.GroupLayout.DEFAULT_SIZE, 63, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(textureMappingPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(textureWrapU, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(textureWrapV, 0, 200, Short.MAX_VALUE)
                    .addComponent(textureUV, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap(192, Short.MAX_VALUE))
        );
        textureMappingPanelLayout.setVerticalGroup(
            textureMappingPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(textureMappingPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(textureMappingPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(textureUV, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(textureUVLabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(textureMappingPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(textureWrapULabel)
                    .addComponent(textureWrapU, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(textureMappingPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(textureWrapVLabel)
                    .addComponent(textureWrapV, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap())
        );

        textureFilteringPanel.setBorder(javax.swing.BorderFactory.createTitledBorder("Filtering"));

        textureMagFilterLabel.setText("Magnification");

        textureMagFilter.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Nearest Neighbor", "Linear" }));

        textureMinFilterLabel.setText("Minification");

        textureMinFilter.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Nearest Neighbor", "Nearest-Mipmap-Nearest", "Nearest-Mipmap-Linear", "Linear", "Linear-Mipmap-Nearest", "Linear-Mipmap-Linear" }));

        javax.swing.GroupLayout textureFilteringPanelLayout = new javax.swing.GroupLayout(textureFilteringPanel);
        textureFilteringPanel.setLayout(textureFilteringPanelLayout);
        textureFilteringPanelLayout.setHorizontalGroup(
            textureFilteringPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(textureFilteringPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(textureFilteringPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(textureMinFilterLabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(textureMagFilterLabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(textureFilteringPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(textureMinFilter, 0, 200, Short.MAX_VALUE)
                    .addComponent(textureMagFilter, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap(192, Short.MAX_VALUE))
        );
        textureFilteringPanelLayout.setVerticalGroup(
            textureFilteringPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(textureFilteringPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(textureFilteringPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(textureMagFilter, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(textureMagFilterLabel))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(textureFilteringPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(textureMinFilterLabel)
                    .addComponent(textureMinFilter, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        texturePreview.setBorder(javax.swing.BorderFactory.createEtchedBorder());

        bumpMapPanel.setBorder(javax.swing.BorderFactory.createTitledBorder("Bump mapping"));

        btnBumpMap.setText("Use as bump map");
        btnBumpMap.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnBumpMapActionPerformed(evt);
            }
        });

        bumpModeLabel.setText("Mode:");

        bumpMode.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Normal", "Tangent" }));
        bumpMode.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                bumpModeActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout bumpMapPanelLayout = new javax.swing.GroupLayout(bumpMapPanel);
        bumpMapPanel.setLayout(bumpMapPanelLayout);
        bumpMapPanelLayout.setHorizontalGroup(
            bumpMapPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(bumpMapPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(btnBumpMap)
                .addGap(95, 95, 95)
                .addComponent(bumpModeLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(bumpMode, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        bumpMapPanelLayout.setVerticalGroup(
            bumpMapPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(bumpMapPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                .addComponent(btnBumpMap)
                .addComponent(bumpModeLabel)
                .addComponent(bumpMode, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
        );

        textureMapperLabel.setText("Texture mapper");

        textureMapper.setModel(textureMappersModel);
        textureMapper.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                textureMapperActionPerformed(evt);
            }
        });

        btnAddTextureMapper.setText("+");
        btnAddTextureMapper.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnAddTextureMapperActionPerformed(evt);
            }
        });

        btnRemoveTextureMapper.setText("-");
        btnRemoveTextureMapper.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnRemoveTextureMapperActionPerformed(evt);
            }
        });

        textureName.setMaximumRowCount(25);
        textureName.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                textureNameActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout texturingPanelLayout = new javax.swing.GroupLayout(texturingPanel);
        texturingPanel.setLayout(texturingPanelLayout);
        texturingPanelLayout.setHorizontalGroup(
            texturingPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(texturingPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(texturingPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(textureTransformPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(texturingPanelLayout.createSequentialGroup()
                        .addComponent(textureNameLabel)
                        .addGap(0, 0, Short.MAX_VALUE))
                    .addComponent(textureSelectionSeparator)
                    .addComponent(textureMappingPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(textureFilteringPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(bumpMapPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, texturingPanelLayout.createSequentialGroup()
                        .addGap(0, 0, Short.MAX_VALUE)
                        .addComponent(texturePreview, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(0, 0, Short.MAX_VALUE))
                    .addGroup(texturingPanelLayout.createSequentialGroup()
                        .addComponent(textureMapperLabel)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(textureMapper, javax.swing.GroupLayout.PREFERRED_SIZE, 200, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(btnAddTextureMapper, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(btnRemoveTextureMapper, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(0, 0, Short.MAX_VALUE))
                    .addComponent(textureName, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        texturingPanelLayout.setVerticalGroup(
            texturingPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(texturingPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(texturingPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(textureMapperLabel)
                    .addComponent(textureMapper, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnAddTextureMapper)
                    .addComponent(btnRemoveTextureMapper))
                .addGap(8, 8, 8)
                .addComponent(textureSelectionSeparator, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(textureNameLabel)
                .addGap(7, 7, 7)
                .addComponent(textureName, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(bumpMapPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(textureTransformPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(textureMappingPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(textureFilteringPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(texturePreview, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(72, Short.MAX_VALUE))
        );

        materialEditorTabbedPane.addTab("Texturing", texturingPanel);

        alphaTestPanel.setBorder(javax.swing.BorderFactory.createTitledBorder("Alpha test"));

        alphaTestFuncLabel.setText("Function");

        alphaTestFunc.setModel(new javax.swing.DefaultComboBoxModel<>(testFunctions));

        alphaTestEnabled.setText("Enabled");
        alphaTestEnabled.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                alphaTestEnabledItemStateChanged(evt);
            }
        });
        alphaTestEnabled.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                alphaTestEnabledActionPerformed(evt);
            }
        });

        alphaTestReferenceLabel.setText("Reference");

        alphaTestReference.setFormatterFactory(new javax.swing.text.DefaultFormatterFactory(new javax.swing.text.NumberFormatter(new java.text.DecimalFormat("#0"))));

        javax.swing.GroupLayout alphaTestPanelLayout = new javax.swing.GroupLayout(alphaTestPanel);
        alphaTestPanel.setLayout(alphaTestPanelLayout);
        alphaTestPanelLayout.setHorizontalGroup(
            alphaTestPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(alphaTestPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(alphaTestPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(alphaTestFuncLabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(alphaTestReferenceLabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(alphaTestPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(alphaTestPanelLayout.createSequentialGroup()
                        .addComponent(alphaTestReference, javax.swing.GroupLayout.PREFERRED_SIZE, 80, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(alphaTestEnabled))
                    .addComponent(alphaTestFunc, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        alphaTestPanelLayout.setVerticalGroup(
            alphaTestPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(alphaTestPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(alphaTestPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(alphaTestFuncLabel)
                    .addComponent(alphaTestFunc, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(alphaTestPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(alphaTestReferenceLabel)
                    .addComponent(alphaTestReference, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(alphaTestEnabled))
                .addContainerGap(11, Short.MAX_VALUE))
        );

        depthTestPanel.setBorder(javax.swing.BorderFactory.createTitledBorder("Depth test"));

        depthFunc.setModel(new javax.swing.DefaultComboBoxModel<>(testFunctions));

        depthFuncLabel.setText("Function");

        dtRedWrite.setText("Red buffer write");

        dtBlueWrite.setText("Blue buffer write");

        dtGreenWrite.setText("Green buffer write");

        dtAlphaWrite.setText("Alpha buffer write");

        dtDepthWrite.setText("Depth buffer write");

        dtEnabled.setText("Enabled");
        dtEnabled.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                dtEnabledItemStateChanged(evt);
            }
        });
        dtEnabled.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                dtEnabledActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout depthTestPanelLayout = new javax.swing.GroupLayout(depthTestPanel);
        depthTestPanel.setLayout(depthTestPanelLayout);
        depthTestPanelLayout.setHorizontalGroup(
            depthTestPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(depthTestPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(depthTestPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(depthTestPanelLayout.createSequentialGroup()
                        .addComponent(depthFuncLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 50, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(depthFunc, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addGroup(depthTestPanelLayout.createSequentialGroup()
                        .addGroup(depthTestPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(dtAlphaWrite)
                            .addComponent(dtGreenWrite)
                            .addComponent(dtBlueWrite)
                            .addComponent(dtRedWrite))
                        .addGap(0, 0, Short.MAX_VALUE))
                    .addGroup(depthTestPanelLayout.createSequentialGroup()
                        .addComponent(dtDepthWrite)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(dtEnabled)))
                .addContainerGap())
        );
        depthTestPanelLayout.setVerticalGroup(
            depthTestPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(depthTestPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(depthTestPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(depthFuncLabel)
                    .addComponent(depthFunc, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(dtRedWrite)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(dtBlueWrite)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(dtGreenWrite)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(dtAlphaWrite)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(depthTestPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(dtDepthWrite)
                    .addComponent(dtEnabled))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        blendOpPanel.setBorder(javax.swing.BorderFactory.createTitledBorder("Blend operation"));

        blendOpRGBLabel.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        blendOpRGBLabel.setText("<html>\n<font color=red>R</font> \n<font color=green>G</font>\n<font color=blue>B</font>\n</html>");

        blendOpAlphaLabel.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        blendOpAlphaLabel.setText("Alpha");

        blendOpEquationLabel.setText("Equation");

        blendOpSrcFuncLabel.setText("Source function");

        blendOpDstFuncLabel.setText("Destination function");

        boDstFuncRgb.setMaximumRowCount(16);
        boDstFuncRgb.setModel(new javax.swing.DefaultComboBoxModel<>(blendFunctions));

        boDstFuncAlpha.setMaximumRowCount(16);
        boDstFuncAlpha.setModel(new javax.swing.DefaultComboBoxModel<>(blendFunctions));

        boSrcFuncRgb.setMaximumRowCount(16);
        boSrcFuncRgb.setModel(new javax.swing.DefaultComboBoxModel<>(blendFunctions));

        boSrcFuncAlpha.setMaximumRowCount(16);
        boSrcFuncAlpha.setModel(new javax.swing.DefaultComboBoxModel<>(blendFunctions));

        boEqtRgb.setModel(new javax.swing.DefaultComboBoxModel<>(blendEquations));

        boEqtAlpha.setModel(new javax.swing.DefaultComboBoxModel<>(blendEquations));

        blendOpEnabled.setText("Enabled");
        blendOpEnabled.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                blendOpEnabledItemStateChanged(evt);
            }
        });
        blendOpEnabled.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                blendOpEnabledActionPerformed(evt);
            }
        });

        boConstantColorSet.setText("Set");
        boConstantColorSet.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                boConstantColorSetActionPerformed(evt);
            }
        });

        boColorPreview.setBackground(new java.awt.Color(255, 255, 255));
        boColorPreview.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        boColorPreview.setOpaque(true);

        blendOpColorLabel.setText("Blend color");

        javax.swing.GroupLayout blendOpPanelLayout = new javax.swing.GroupLayout(blendOpPanel);
        blendOpPanel.setLayout(blendOpPanelLayout);
        blendOpPanelLayout.setHorizontalGroup(
            blendOpPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(blendOpPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(blendOpPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(blendOpPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                        .addComponent(blendOpEquationLabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(blendOpDstFuncLabel, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(blendOpSrcFuncLabel, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addComponent(blendOpColorLabel))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(blendOpPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(blendOpPanelLayout.createSequentialGroup()
                        .addComponent(boColorPreview, javax.swing.GroupLayout.PREFERRED_SIZE, 50, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(boConstantColorSet)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 175, Short.MAX_VALUE)
                        .addComponent(blendOpEnabled))
                    .addGroup(blendOpPanelLayout.createSequentialGroup()
                        .addGroup(blendOpPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(blendOpRGBLabel, javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(boEqtRgb, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(boSrcFuncRgb, javax.swing.GroupLayout.Alignment.TRAILING, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(boDstFuncRgb, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(blendOpPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(boDstFuncAlpha, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(boSrcFuncAlpha, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(boEqtAlpha, javax.swing.GroupLayout.Alignment.TRAILING, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(blendOpAlphaLabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))))
                .addContainerGap())
        );
        blendOpPanelLayout.setVerticalGroup(
            blendOpPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(blendOpPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(blendOpPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(blendOpRGBLabel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(blendOpAlphaLabel))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(blendOpPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(blendOpEquationLabel)
                    .addComponent(boEqtRgb, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(boEqtAlpha, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(blendOpPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(blendOpSrcFuncLabel)
                    .addComponent(boSrcFuncRgb, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(boSrcFuncAlpha, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(blendOpPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(blendOpDstFuncLabel)
                    .addComponent(boDstFuncRgb, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(boDstFuncAlpha, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGroup(blendOpPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(blendOpPanelLayout.createSequentialGroup()
                        .addGap(0, 0, Short.MAX_VALUE)
                        .addComponent(blendOpEnabled)
                        .addContainerGap())
                    .addGroup(blendOpPanelLayout.createSequentialGroup()
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(blendOpPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(blendOpColorLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 23, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(boColorPreview, javax.swing.GroupLayout.PREFERRED_SIZE, 23, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(boConstantColorSet))
                        .addContainerGap(22, Short.MAX_VALUE))))
        );

        stencilOpPanel.setBorder(javax.swing.BorderFactory.createTitledBorder("Stencil operation"));

        stencilFuncLabel.setText("Test function");

        stencilFunc.setModel(new javax.swing.DefaultComboBoxModel<>(testFunctions));

        stencilRefLabel.setText("Test reference");

        stencilRef.setFormatterFactory(new javax.swing.text.DefaultFormatterFactory(new javax.swing.text.NumberFormatter(new java.text.DecimalFormat("#0"))));

        stencilMaskLabel.setText("Test mask");

        stencilMask.setFormatterFactory(new javax.swing.text.DefaultFormatterFactory(new javax.swing.text.NumberFormatter(new java.text.DecimalFormat("#0"))));

        stencilBufMaskLabel.setText("Buffer mask");

        stencilBufMask.setFormatterFactory(new javax.swing.text.DefaultFormatterFactory(new javax.swing.text.NumberFormatter(new java.text.DecimalFormat("#0"))));

        stencilEnabled.setText("Enabled");

        soFailLabel.setText("Fail operation");

        soPassLabel.setText("Pass operation");

        soZPassLabel.setText("Z-Pass operation");

        soSep.setOrientation(javax.swing.SwingConstants.VERTICAL);

        soZPass.setModel(new javax.swing.DefaultComboBoxModel<>(stencilOps));

        soPass.setModel(new javax.swing.DefaultComboBoxModel<>(stencilOps));

        soFail.setModel(new javax.swing.DefaultComboBoxModel<>(stencilOps));

        javax.swing.GroupLayout stencilOpPanelLayout = new javax.swing.GroupLayout(stencilOpPanel);
        stencilOpPanel.setLayout(stencilOpPanelLayout);
        stencilOpPanelLayout.setHorizontalGroup(
            stencilOpPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(stencilOpPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(stencilOpPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(stencilOpPanelLayout.createSequentialGroup()
                        .addComponent(stencilFuncLabel)
                        .addGap(18, 18, 18)
                        .addComponent(stencilFunc, javax.swing.GroupLayout.PREFERRED_SIZE, 130, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(stencilOpPanelLayout.createSequentialGroup()
                        .addGroup(stencilOpPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(stencilRefLabel)
                            .addComponent(stencilMaskLabel)
                            .addComponent(stencilBufMaskLabel))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(stencilOpPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(stencilBufMask, javax.swing.GroupLayout.DEFAULT_SIZE, 60, Short.MAX_VALUE)
                            .addComponent(stencilRef)
                            .addComponent(stencilMask))))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(soSep, javax.swing.GroupLayout.PREFERRED_SIZE, 2, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(stencilOpPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(stencilOpPanelLayout.createSequentialGroup()
                        .addComponent(soZPassLabel)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(soZPass, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addGroup(stencilOpPanelLayout.createSequentialGroup()
                        .addGap(0, 0, Short.MAX_VALUE)
                        .addComponent(stencilEnabled))
                    .addGroup(stencilOpPanelLayout.createSequentialGroup()
                        .addGroup(stencilOpPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(soPassLabel)
                            .addComponent(soFailLabel))
                        .addGap(14, 14, 14)
                        .addGroup(stencilOpPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(soFail, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(soPass, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))))
                .addGap(10, 10, 10))
        );
        stencilOpPanelLayout.setVerticalGroup(
            stencilOpPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(stencilOpPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(stencilOpPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(stencilOpPanelLayout.createSequentialGroup()
                        .addGroup(stencilOpPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(stencilFuncLabel)
                            .addComponent(stencilFunc, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(soFailLabel)
                            .addComponent(soFail, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(stencilOpPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(stencilRefLabel)
                            .addComponent(stencilRef, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(soPassLabel)
                            .addComponent(soPass, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(stencilOpPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(stencilMaskLabel)
                            .addComponent(stencilMask, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(soZPassLabel)
                            .addComponent(soZPass, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGroup(stencilOpPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(stencilOpPanelLayout.createSequentialGroup()
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(stencilOpPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                    .addComponent(stencilBufMaskLabel)
                                    .addComponent(stencilBufMask, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addGap(0, 0, Short.MAX_VALUE))
                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, stencilOpPanelLayout.createSequentialGroup()
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 7, Short.MAX_VALUE)
                                .addComponent(stencilEnabled)
                                .addContainerGap())))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, stencilOpPanelLayout.createSequentialGroup()
                        .addComponent(soSep)
                        .addContainerGap())))
        );

        faceCullingPanel.setBorder(javax.swing.BorderFactory.createTitledBorder("Face culling"));

        faceCulling.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Disabled", "Front face", "Back face" }));

        javax.swing.GroupLayout faceCullingPanelLayout = new javax.swing.GroupLayout(faceCullingPanel);
        faceCullingPanel.setLayout(faceCullingPanelLayout);
        faceCullingPanelLayout.setHorizontalGroup(
            faceCullingPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(faceCullingPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(faceCulling, javax.swing.GroupLayout.PREFERRED_SIZE, 161, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        faceCullingPanelLayout.setVerticalGroup(
            faceCullingPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(faceCullingPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(faceCulling, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout renderStatePanelLayout = new javax.swing.GroupLayout(renderStatePanel);
        renderStatePanel.setLayout(renderStatePanelLayout);
        renderStatePanelLayout.setHorizontalGroup(
            renderStatePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(renderStatePanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(renderStatePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(alphaTestPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(depthTestPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(blendOpPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(stencilOpPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(faceCullingPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        renderStatePanelLayout.setVerticalGroup(
            renderStatePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(renderStatePanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(alphaTestPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(depthTestPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(blendOpPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(stencilOpPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(faceCullingPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(37, Short.MAX_VALUE))
        );

        materialEditorTabbedPane.addTab("Render state", renderStatePanel);

        texCmbPanel.setBorder(javax.swing.BorderFactory.createTitledBorder("Texture Combiners"));

        inputBufferColorLabel.setText("Input buffer color");

        inBufColSetButton.setText("Set");
        inBufColSetButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                inBufColSetButtonActionPerformed(evt);
            }
        });

        inBufColPreview.setBackground(new java.awt.Color(255, 255, 255));
        inBufColPreview.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        inBufColPreview.setOpaque(true);

        alphaCombinerPanel.setBorder(javax.swing.BorderFactory.createTitledBorder("Alpha combiner"));

        cModeLabelA.setText("Mode");

        cOp0ALabel.setText("Source A:");

        cOp1ALabel.setText("Source B:");

        cOp2ALabel.setText("Source C:");

        cOp2A.setModel(new javax.swing.DefaultComboBoxModel<>(combinerAlphaOps));

        cOp1A.setModel(new javax.swing.DefaultComboBoxModel<>(combinerAlphaOps));

        cOp0A.setModel(new javax.swing.DefaultComboBoxModel<>(combinerAlphaOps));

        cOf0Label.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        cOf0Label.setText("of");

        cOf1Label.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        cOf1Label.setText("of");

        cOf2Label.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        cOf2Label.setText("of");

        cModeA.setMaximumRowCount(16);
        cModeA.setModel(new javax.swing.DefaultComboBoxModel<>(combinerModes));
        cModeA.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cModeAActionPerformed(evt);
            }
        });

        cSrc0A.setMaximumRowCount(16);
        cSrc0A.setModel(new javax.swing.DefaultComboBoxModel<>(combinerSources));

        cSrc1A.setMaximumRowCount(16);
        cSrc1A.setModel(new javax.swing.DefaultComboBoxModel<>(combinerSources));

        cSrc2A.setMaximumRowCount(16);
        cSrc2A.setModel(new javax.swing.DefaultComboBoxModel<>(combinerSources));

        cScaleALabel.setText("Scale");

        cScaleA.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "One", "Two", "Four" }));

        cUpdateA.setText("Update alpha buffer");

        cEqtPreviewA.setText("<Equation>");

        javax.swing.GroupLayout alphaCombinerPanelLayout = new javax.swing.GroupLayout(alphaCombinerPanel);
        alphaCombinerPanel.setLayout(alphaCombinerPanelLayout);
        alphaCombinerPanelLayout.setHorizontalGroup(
            alphaCombinerPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(alphaCombinerPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(alphaCombinerPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(alphaCombinerPanelLayout.createSequentialGroup()
                        .addComponent(cUpdateA)
                        .addGap(0, 0, Short.MAX_VALUE))
                    .addGroup(alphaCombinerPanelLayout.createSequentialGroup()
                        .addGroup(alphaCombinerPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(alphaCombinerPanelLayout.createSequentialGroup()
                                .addComponent(cScaleALabel, javax.swing.GroupLayout.PREFERRED_SIZE, 47, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(cScaleA, javax.swing.GroupLayout.PREFERRED_SIZE, 130, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(alphaCombinerPanelLayout.createSequentialGroup()
                                .addGroup(alphaCombinerPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addGroup(alphaCombinerPanelLayout.createSequentialGroup()
                                        .addGroup(alphaCombinerPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                                            .addComponent(cOp1ALabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                            .addComponent(cOp2ALabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addGroup(alphaCombinerPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                            .addGroup(alphaCombinerPanelLayout.createSequentialGroup()
                                                .addComponent(cOp1A, javax.swing.GroupLayout.PREFERRED_SIZE, 130, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                .addComponent(cOf1Label, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE))
                                            .addGroup(alphaCombinerPanelLayout.createSequentialGroup()
                                                .addComponent(cOp2A, javax.swing.GroupLayout.PREFERRED_SIZE, 130, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                .addComponent(cOf2Label, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE))))
                                    .addGroup(alphaCombinerPanelLayout.createSequentialGroup()
                                        .addGroup(alphaCombinerPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                                            .addComponent(cModeLabelA, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                            .addComponent(cOp0ALabel, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addGroup(alphaCombinerPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                            .addComponent(cOp0A, 0, 130, Short.MAX_VALUE)
                                            .addComponent(cModeA, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(cOf0Label, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE)))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(alphaCombinerPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addGroup(alphaCombinerPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                        .addComponent(cSrc1A, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                        .addComponent(cSrc2A, 0, 130, Short.MAX_VALUE)
                                        .addComponent(cSrc0A, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                                    .addComponent(cEqtPreviewA, javax.swing.GroupLayout.PREFERRED_SIZE, 180, javax.swing.GroupLayout.PREFERRED_SIZE))))
                        .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))))
        );
        alphaCombinerPanelLayout.setVerticalGroup(
            alphaCombinerPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(alphaCombinerPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(alphaCombinerPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(cModeLabelA)
                    .addComponent(cModeA, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(cEqtPreviewA))
                .addGap(18, 18, 18)
                .addGroup(alphaCombinerPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(alphaCombinerPanelLayout.createSequentialGroup()
                        .addComponent(cSrc0A, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(cSrc1A, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(cSrc2A, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(alphaCombinerPanelLayout.createSequentialGroup()
                        .addGroup(alphaCombinerPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(cOp0ALabel, javax.swing.GroupLayout.PREFERRED_SIZE, 14, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(cOp0A, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(cOf0Label))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(alphaCombinerPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(cOp1ALabel)
                            .addComponent(cOp1A, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(cOf1Label))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(alphaCombinerPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(cOp2ALabel)
                            .addComponent(cOp2A, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(cOf2Label))))
                .addGap(18, 18, 18)
                .addGroup(alphaCombinerPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(cScaleALabel)
                    .addComponent(cScaleA, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(cUpdateA)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        shadingStageLabel.setText("Shading stage");

        shadingStage.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Stage 1", "Stage 2", "Stage 3", "Stage 4", "Stage 5", "Stage 6" }));
        shadingStage.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                shadingStageActionPerformed(evt);
            }
        });

        colorCombinerPanel.setBorder(javax.swing.BorderFactory.createTitledBorder("Color combiner"));

        cModeRgbLabel.setText("Mode");

        cOp0RgbLabel.setText("Source A:");

        cOp1RgbLabel.setText("Source B:");

        cOp2RgbLabel.setText("Source C:");

        cOp2Rgb.setMaximumRowCount(16);
        cOp2Rgb.setModel(new javax.swing.DefaultComboBoxModel<>(combinerColorOps));

        cOp1Rgb.setMaximumRowCount(16);
        cOp1Rgb.setModel(new javax.swing.DefaultComboBoxModel<>(combinerColorOps));

        cOp0Rgb.setMaximumRowCount(16);
        cOp0Rgb.setModel(new javax.swing.DefaultComboBoxModel<>(combinerColorOps));

        cOf0RgbLabel.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        cOf0RgbLabel.setText("of");

        cOf1RgbLabel.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        cOf1RgbLabel.setText("of");

        cOf2RgbLabel.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        cOf2RgbLabel.setText("of");

        cModeRgb.setMaximumRowCount(16);
        cModeRgb.setModel(new javax.swing.DefaultComboBoxModel<>(combinerModes));
        cModeRgb.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cModeRgbActionPerformed(evt);
            }
        });

        cSrc0Rgb.setMaximumRowCount(16);
        cSrc0Rgb.setModel(new javax.swing.DefaultComboBoxModel<>(combinerSources));

        cSrc1Rgb.setMaximumRowCount(16);
        cSrc1Rgb.setModel(new javax.swing.DefaultComboBoxModel<>(combinerSources));

        cSrc2Rgb.setMaximumRowCount(16);
        cSrc2Rgb.setModel(new javax.swing.DefaultComboBoxModel<>(combinerSources));

        cScaleRgbLabel.setText("Scale");

        cScaleRgb.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "One", "Two", "Four" }));

        cUpdateRgb.setText("Update color buffer");

        cEqtPreviewRgb.setText("<Equation>");

        javax.swing.GroupLayout colorCombinerPanelLayout = new javax.swing.GroupLayout(colorCombinerPanel);
        colorCombinerPanel.setLayout(colorCombinerPanelLayout);
        colorCombinerPanelLayout.setHorizontalGroup(
            colorCombinerPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(colorCombinerPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(colorCombinerPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(colorCombinerPanelLayout.createSequentialGroup()
                        .addComponent(cUpdateRgb)
                        .addGap(0, 0, Short.MAX_VALUE))
                    .addGroup(colorCombinerPanelLayout.createSequentialGroup()
                        .addGroup(colorCombinerPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(colorCombinerPanelLayout.createSequentialGroup()
                                .addComponent(cScaleRgbLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 47, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(cScaleRgb, javax.swing.GroupLayout.PREFERRED_SIZE, 130, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(colorCombinerPanelLayout.createSequentialGroup()
                                .addGroup(colorCombinerPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addGroup(colorCombinerPanelLayout.createSequentialGroup()
                                        .addGroup(colorCombinerPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                                            .addComponent(cOp1RgbLabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                            .addComponent(cOp2RgbLabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addGroup(colorCombinerPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                            .addGroup(colorCombinerPanelLayout.createSequentialGroup()
                                                .addComponent(cOp1Rgb, javax.swing.GroupLayout.PREFERRED_SIZE, 130, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                .addComponent(cOf1RgbLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE))
                                            .addGroup(colorCombinerPanelLayout.createSequentialGroup()
                                                .addComponent(cOp2Rgb, javax.swing.GroupLayout.PREFERRED_SIZE, 130, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                .addComponent(cOf2RgbLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE))))
                                    .addGroup(colorCombinerPanelLayout.createSequentialGroup()
                                        .addGroup(colorCombinerPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                                            .addComponent(cModeRgbLabel, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                            .addComponent(cOp0RgbLabel, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addGroup(colorCombinerPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                            .addComponent(cOp0Rgb, 0, 130, Short.MAX_VALUE)
                                            .addComponent(cModeRgb, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(cOf0RgbLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE)))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(colorCombinerPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addGroup(colorCombinerPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                        .addComponent(cSrc1Rgb, 0, 130, Short.MAX_VALUE)
                                        .addComponent(cSrc2Rgb, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                        .addComponent(cSrc0Rgb, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                                    .addComponent(cEqtPreviewRgb, javax.swing.GroupLayout.PREFERRED_SIZE, 180, javax.swing.GroupLayout.PREFERRED_SIZE))))
                        .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))))
        );
        colorCombinerPanelLayout.setVerticalGroup(
            colorCombinerPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(colorCombinerPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(colorCombinerPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(cModeRgbLabel)
                    .addComponent(cModeRgb, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(cEqtPreviewRgb))
                .addGap(18, 18, 18)
                .addGroup(colorCombinerPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(colorCombinerPanelLayout.createSequentialGroup()
                        .addComponent(cSrc0Rgb, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(cSrc1Rgb, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(cSrc2Rgb, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(colorCombinerPanelLayout.createSequentialGroup()
                        .addGroup(colorCombinerPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(cOp0RgbLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 14, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(cOp0Rgb, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(cOf0RgbLabel))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(colorCombinerPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(cOp1RgbLabel)
                            .addComponent(cOp1Rgb, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(cOf1RgbLabel))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(colorCombinerPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(cOp2RgbLabel)
                            .addComponent(cOp2Rgb, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(cOf2RgbLabel))))
                .addGap(18, 18, 18)
                .addGroup(colorCombinerPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(cScaleRgbLabel)
                    .addComponent(cScaleRgb, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(cUpdateRgb)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        cConstColorPanel.setBorder(javax.swing.BorderFactory.createTitledBorder("Constant color"));

        ccolAsgnLabel.setText("Assignment:");

        constantAsgn.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Constant 1", "Constant 2", "Constant 3", "Constant 4", "Constant 5", "Constant 6", "Emission", "Diffuse", "Ambient", "Specular 0", "Specular 1" }));
        constantAsgn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                constantAsgnActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout cConstColorPanelLayout = new javax.swing.GroupLayout(cConstColorPanel);
        cConstColorPanel.setLayout(cConstColorPanelLayout);
        cConstColorPanelLayout.setHorizontalGroup(
            cConstColorPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(cConstColorPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(ccolAsgnLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(constantAsgn, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(constantColorPicker, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        cConstColorPanelLayout.setVerticalGroup(
            cConstColorPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(cConstColorPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(cConstColorPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(cConstColorPanelLayout.createSequentialGroup()
                        .addComponent(constantColorPicker, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(0, 0, Short.MAX_VALUE))
                    .addComponent(constantAsgn, javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(ccolAsgnLabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );

        javax.swing.GroupLayout texCmbPanelLayout = new javax.swing.GroupLayout(texCmbPanel);
        texCmbPanel.setLayout(texCmbPanelLayout);
        texCmbPanelLayout.setHorizontalGroup(
            texCmbPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(texCmbPanelLayout.createSequentialGroup()
                .addGap(10, 10, 10)
                .addComponent(shadingStageLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(shadingStage, javax.swing.GroupLayout.PREFERRED_SIZE, 120, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(267, javax.swing.GroupLayout.PREFERRED_SIZE))
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, texCmbPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                .addComponent(cConstColorPanel, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(inBufColSeparator, javax.swing.GroupLayout.Alignment.LEADING)
                .addComponent(colorCombinerPanel, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(alphaCombinerPanel, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(javax.swing.GroupLayout.Alignment.LEADING, texCmbPanelLayout.createSequentialGroup()
                    .addGap(9, 9, 9)
                    .addComponent(inputBufferColorLabel)
                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                    .addComponent(inBufColPreview, javax.swing.GroupLayout.PREFERRED_SIZE, 50, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                    .addComponent(inBufColSetButton)
                    .addGap(266, 266, 266)))
        );
        texCmbPanelLayout.setVerticalGroup(
            texCmbPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(texCmbPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(texCmbPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                    .addComponent(inBufColPreview, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(texCmbPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(inputBufferColorLabel)
                        .addComponent(inBufColSetButton)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(inBufColSeparator, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(texCmbPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(shadingStageLabel)
                    .addComponent(shadingStage, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(colorCombinerPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(alphaCombinerPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(cConstColorPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );

        vshPanel.setBorder(javax.swing.BorderFactory.createTitledBorder("Vertex Shader"));

        shaderArcLabel.setText("Shader Archive");

        javax.swing.GroupLayout vshPanelLayout = new javax.swing.GroupLayout(vshPanel);
        vshPanel.setLayout(vshPanelLayout);
        vshPanelLayout.setHorizontalGroup(
            vshPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(vshPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(vshPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(shaderArcName, javax.swing.GroupLayout.PREFERRED_SIZE, 190, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(shaderArcLabel))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        vshPanelLayout.setVerticalGroup(
            vshPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(vshPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(shaderArcLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(shaderArcName, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );

        javax.swing.GroupLayout shadingPanelLayout = new javax.swing.GroupLayout(shadingPanel);
        shadingPanel.setLayout(shadingPanelLayout);
        shadingPanelLayout.setHorizontalGroup(
            shadingPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(shadingPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(shadingPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(vshPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(texCmbPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        shadingPanelLayout.setVerticalGroup(
            shadingPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, shadingPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(vshPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(texCmbPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, Short.MAX_VALUE))
        );

        materialEditorTabbedPane.addTab("Shading", shadingPanel);

        lightingGeneralPanel.setBorder(javax.swing.BorderFactory.createTitledBorder("General"));

        lsiLabel.setText("Light set index");

        lightSetIndex.setModel(new javax.swing.SpinnerNumberModel(0, 0, 255, 1));

        lightLayerLabel.setText("Lighting layer");

        lightLayer.setModel(new javax.swing.SpinnerNumberModel(0, 0, 7, 1));

        javax.swing.GroupLayout lightingGeneralPanelLayout = new javax.swing.GroupLayout(lightingGeneralPanel);
        lightingGeneralPanel.setLayout(lightingGeneralPanelLayout);
        lightingGeneralPanelLayout.setHorizontalGroup(
            lightingGeneralPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(lightingGeneralPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(lightingGeneralPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(lightingGeneralPanelLayout.createSequentialGroup()
                        .addComponent(lsiLabel)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(lightSetIndex, javax.swing.GroupLayout.PREFERRED_SIZE, 72, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(lightingGeneralPanelLayout.createSequentialGroup()
                        .addComponent(lightLayerLabel)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(lightLayer, javax.swing.GroupLayout.PREFERRED_SIZE, 72, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        lightingGeneralPanelLayout.setVerticalGroup(
            lightingGeneralPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(lightingGeneralPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(lightingGeneralPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lsiLabel)
                    .addComponent(lightSetIndex, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(lightingGeneralPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lightLayerLabel)
                    .addComponent(lightLayer, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        colorPanel.setBorder(javax.swing.BorderFactory.createTitledBorder("Colors"));

        ambientColorLabel.setText("Ambient");

        ambientColorPreview.setBackground(new java.awt.Color(255, 255, 255));
        ambientColorPreview.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        ambientColorPreview.setOpaque(true);

        btnSetAmbientColor.setText("Set");
        btnSetAmbientColor.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnSetAmbientColorActionPerformed(evt);
            }
        });

        diffuseColorLabel.setText("Diffuse");

        diffuseColorPreview.setBackground(new java.awt.Color(255, 255, 255));
        diffuseColorPreview.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        diffuseColorPreview.setOpaque(true);

        btnSetDiffuseColor.setText("Set");
        btnSetDiffuseColor.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnSetDiffuseColorActionPerformed(evt);
            }
        });

        spc0ColorLabel.setText("Specular 0");

        spc0ColorPreview.setBackground(new java.awt.Color(255, 255, 255));
        spc0ColorPreview.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        spc0ColorPreview.setOpaque(true);

        btnSetSpc0Color.setText("Set");
        btnSetSpc0Color.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnSetSpc0ColorActionPerformed(evt);
            }
        });

        spc1ColorLabel.setText("Specular 1");

        spc1ColorPreview.setBackground(new java.awt.Color(255, 255, 255));
        spc1ColorPreview.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        spc1ColorPreview.setOpaque(true);

        btnSetSpc1Color.setText("Set");
        btnSetSpc1Color.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnSetSpc1ColorActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout colorPanelLayout = new javax.swing.GroupLayout(colorPanel);
        colorPanel.setLayout(colorPanelLayout);
        colorPanelLayout.setHorizontalGroup(
            colorPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(colorPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(colorPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(colorPanelLayout.createSequentialGroup()
                        .addComponent(ambientColorLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 65, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(ambientColorPreview, javax.swing.GroupLayout.PREFERRED_SIZE, 50, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(btnSetAmbientColor))
                    .addGroup(colorPanelLayout.createSequentialGroup()
                        .addComponent(diffuseColorLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 65, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(diffuseColorPreview, javax.swing.GroupLayout.PREFERRED_SIZE, 50, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(btnSetDiffuseColor))
                    .addGroup(colorPanelLayout.createSequentialGroup()
                        .addComponent(spc0ColorLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 65, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(spc0ColorPreview, javax.swing.GroupLayout.PREFERRED_SIZE, 50, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(btnSetSpc0Color))
                    .addGroup(colorPanelLayout.createSequentialGroup()
                        .addComponent(spc1ColorLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 65, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(spc1ColorPreview, javax.swing.GroupLayout.PREFERRED_SIZE, 50, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(btnSetSpc1Color)))
                .addContainerGap(285, Short.MAX_VALUE))
        );
        colorPanelLayout.setVerticalGroup(
            colorPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(colorPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(colorPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                    .addComponent(ambientColorPreview, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(colorPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(ambientColorLabel)
                        .addComponent(btnSetAmbientColor)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(colorPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                    .addComponent(diffuseColorPreview, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(colorPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(diffuseColorLabel)
                        .addComponent(btnSetDiffuseColor)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(colorPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                    .addComponent(spc0ColorPreview, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(colorPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(spc0ColorLabel)
                        .addComponent(btnSetSpc0Color)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(colorPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                    .addComponent(spc1ColorPreview, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(colorPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(spc1ColorLabel)
                        .addComponent(btnSetSpc1Color)))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        lutPanel.setBorder(javax.swing.BorderFactory.createTitledBorder("Lookup Tables"));

        lutLabel.setText("LUT Target");

        lut.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Reflectance R", "Reflectance G", "Reflectance B", "Distance 0", "Distance 1", "Primary Fresnel", "Secondary Fresnel" }));
        lut.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                lutActionPerformed(evt);
            }
        });

        lutTextureLabel.setText("LUT Texture");

        lutInputLabel.setText("LUT Input");

        lutInput.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Normal ⋅ Half", "View ⋅ Half", "Normal ⋅ View", "Light ⋅ Normal", "-Light ⋅ LightDir", "cosϕ" }));
        lutInput.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                lutInputActionPerformed(evt);
            }
        });

        lutTextureName.setMaximumRowCount(20);
        lutTextureName.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                lutTextureNameActionPerformed(evt);
            }
        });

        btnIsLUTEnabled.setText("Enabled");
        btnIsLUTEnabled.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnIsLUTEnabledActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout lutPanelLayout = new javax.swing.GroupLayout(lutPanel);
        lutPanel.setLayout(lutPanelLayout);
        lutPanelLayout.setHorizontalGroup(
            lutPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(lutPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(lutPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addGroup(lutPanelLayout.createSequentialGroup()
                        .addComponent(lutLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 59, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(lutPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(btnIsLUTEnabled)
                            .addComponent(lut, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                    .addGroup(lutPanelLayout.createSequentialGroup()
                        .addGroup(lutPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(lutTextureLabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(lutInputLabel))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(lutPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(lutTextureName, javax.swing.GroupLayout.DEFAULT_SIZE, 240, Short.MAX_VALUE)
                            .addComponent(lutInput, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))))
                .addGap(0, 150, Short.MAX_VALUE))
        );
        lutPanelLayout.setVerticalGroup(
            lutPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(lutPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(lutPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lutLabel)
                    .addComponent(lut, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(btnIsLUTEnabled)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(lutPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lutTextureLabel)
                    .addComponent(lutTextureName, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(lutPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lutInputLabel)
                    .addComponent(lutInput, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout lightingPanelLayout = new javax.swing.GroupLayout(lightingPanel);
        lightingPanel.setLayout(lightingPanelLayout);
        lightingPanelLayout.setHorizontalGroup(
            lightingPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(lightingPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(lightingPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(lightingGeneralPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(colorPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(lutPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        lightingPanelLayout.setVerticalGroup(
            lightingPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(lightingPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(lightingGeneralPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(colorPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(lutPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(325, Short.MAX_VALUE))
        );

        materialEditorTabbedPane.addTab("Lighting", lightingPanel);

        javax.swing.GroupLayout metaDataPanelLayout = new javax.swing.GroupLayout(metaDataPanel);
        metaDataPanel.setLayout(metaDataPanelLayout);
        metaDataPanelLayout.setHorizontalGroup(
            metaDataPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(metaDataPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(metaDataEditor, javax.swing.GroupLayout.DEFAULT_SIZE, 481, Short.MAX_VALUE)
                .addContainerGap())
        );
        metaDataPanelLayout.setVerticalGroup(
            metaDataPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(metaDataPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(metaDataEditor, javax.swing.GroupLayout.DEFAULT_SIZE, 717, Short.MAX_VALUE)
                .addContainerGap())
        );

        materialEditorTabbedPane.addTab("Metadata", metaDataPanel);

        btnCmdAlphaEnable.setText("Enable Alpha blending");
        btnCmdAlphaEnable.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnCmdAlphaEnableActionPerformed(evt);
            }
        });

        btnCmdOutlineSet.setText("Set outline metadata");
        btnCmdOutlineSet.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnCmdOutlineSetActionPerformed(evt);
            }
        });

        isEdgeEnabled.setText("Enable outlines");

        edgeGroup.setFormatterFactory(new javax.swing.text.DefaultFormatterFactory(new javax.swing.text.NumberFormatter(new java.text.DecimalFormat("#0"))));

        edgeGroupLabel.setText("Edge group");

        outlineSep.setOrientation(javax.swing.SwingConstants.VERTICAL);

        btnCmdFragLightEnable.setText("Enable fragment lighting");
        btnCmdFragLightEnable.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnCmdFragLightEnableActionPerformed(evt);
            }
        });

        jLabel2.setText("Pokémon shaders");

        btnSetDefaultSha.setText("Set to Default shader");
        btnSetDefaultSha.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnSetDefaultShaActionPerformed(evt);
            }
        });

        btnSetBtlFldSha.setText("Set to BattleField shader");
        btnSetBtlFldSha.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnSetBtlFldShaActionPerformed(evt);
            }
        });

        btnAddBake.setText("Add bake map");
        btnAddBake.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnAddBakeActionPerformed(evt);
            }
        });

        bakeTexLabel.setText("Texture name");

        bakeUVLabel.setText("UV Set");

        bakeUV.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "UV0", "UV1", "UV2" }));

        bakeFormulaLabel.setText("Formula");

        bakeFormula.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Shadow", "Lightmap", "Both (R/G channels)" }));

        bakeTexName.setMaximumRowCount(20);
        bakeTexName.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                bakeTexNameActionPerformed(evt);
            }
        });

        btnDisableAlphaBlend.setText("Disable Alpha blending");
        btnDisableAlphaBlend.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnDisableAlphaBlendActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout shortcutsPanelLayout = new javax.swing.GroupLayout(shortcutsPanel);
        shortcutsPanel.setLayout(shortcutsPanelLayout);
        shortcutsPanelLayout.setHorizontalGroup(
            shortcutsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(shortcutsPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(shortcutsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel2)
                    .addGroup(shortcutsPanelLayout.createSequentialGroup()
                        .addComponent(btnSetDefaultSha)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(btnSetBtlFldSha))
                    .addGroup(shortcutsPanelLayout.createSequentialGroup()
                        .addGroup(shortcutsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                            .addComponent(btnAddBake, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(btnCmdAlphaEnable, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(btnCmdFragLightEnable, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(btnCmdOutlineSet, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(shortcutsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(shortcutsPanelLayout.createSequentialGroup()
                                .addGroup(shortcutsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(bakeTexLabel)
                                    .addComponent(bakeUVLabel)
                                    .addComponent(bakeFormulaLabel))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(shortcutsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(bakeUV, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(bakeFormula, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(bakeTexName, javax.swing.GroupLayout.DEFAULT_SIZE, 189, Short.MAX_VALUE)))
                            .addGroup(shortcutsPanelLayout.createSequentialGroup()
                                .addComponent(isEdgeEnabled)
                                .addGap(6, 6, 6)
                                .addComponent(outlineSep, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(edgeGroupLabel)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(edgeGroup, javax.swing.GroupLayout.PREFERRED_SIZE, 60, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 32, Short.MAX_VALUE))
                            .addGroup(shortcutsPanelLayout.createSequentialGroup()
                                .addComponent(btnDisableAlphaBlend, javax.swing.GroupLayout.PREFERRED_SIZE, 154, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(0, 0, Short.MAX_VALUE)))))
                .addGap(76, 76, 76))
        );
        shortcutsPanelLayout.setVerticalGroup(
            shortcutsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(shortcutsPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(shortcutsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btnCmdAlphaEnable)
                    .addComponent(btnDisableAlphaBlend))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(btnCmdFragLightEnable)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(shortcutsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(btnCmdOutlineSet)
                    .addGroup(shortcutsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                        .addComponent(outlineSep, javax.swing.GroupLayout.PREFERRED_SIZE, 23, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGroup(javax.swing.GroupLayout.Alignment.LEADING, shortcutsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(isEdgeEnabled)
                            .addComponent(edgeGroupLabel)
                            .addComponent(edgeGroup, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(shortcutsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btnAddBake)
                    .addComponent(bakeTexLabel)
                    .addComponent(bakeTexName, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(shortcutsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE, false)
                    .addComponent(bakeUVLabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(bakeUV, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(shortcutsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(bakeFormulaLabel)
                    .addComponent(bakeFormula, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(2, 2, 2)
                .addComponent(jLabel2)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(shortcutsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btnSetDefaultSha)
                    .addComponent(btnSetBtlFldSha))
                .addContainerGap(521, Short.MAX_VALUE))
        );

        materialEditorTabbedPane.addTab("Shortcuts", shortcutsPanel);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(generalPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(materialEditorTabbedPane, javax.swing.GroupLayout.DEFAULT_SIZE, 506, Short.MAX_VALUE)
                    .addComponent(saveCtrl, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(generalPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(materialEditorTabbedPane)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(saveCtrl, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents

    private void boConstantColorSetActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_boConstantColorSetActionPerformed
		if (mat != null) {
			Color c = JColorChooser.showDialog(this, "Pick a Color", mat.blendOperation.blendColor.toColor());
			if (c != null) {
				mat.blendOperation.blendColor = new RGBA(c);
				setBlendColor();
			}
		}
    }//GEN-LAST:event_boConstantColorSetActionPerformed

    private void alphaTestEnabledItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_alphaTestEnabledItemStateChanged
		atEnableSeq();
    }//GEN-LAST:event_alphaTestEnabledItemStateChanged

	private void atEnableSeq() {
		boolean is = alphaTestEnabled.isSelected();
		//ComponentUtils.setComponentsEnabled(is, alphaTestFunc, alphaTestReference);
	}

    private void dtEnabledItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_dtEnabledItemStateChanged
		dtEnableSeq();
    }//GEN-LAST:event_dtEnabledItemStateChanged

	private void dtEnableSeq() {
		boolean is = dtEnabled.isSelected();
		//ComponentUtils.setComponentsEnabled(is, depthFunc, dtRedWrite, dtGreenWrite, dtBlueWrite, dtAlphaWrite, dtDepthWrite);
	}

    private void blendOpEnabledItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_blendOpEnabledItemStateChanged
		boEnableSeq();
    }//GEN-LAST:event_blendOpEnabledItemStateChanged

	private void boEnableSeq() {
		boolean is = blendOpEnabled.isSelected();
		//ComponentUtils.setComponentsEnabled(is, boColorPreview, boConstantColorSet, boDstFuncAlpha, boDstFuncRgb, boEqtAlpha, boEqtRgb, boSrcFuncAlpha, boSrcFuncRgb);
	}

    private void shadingStageActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_shadingStageActionPerformed
		if (mat != null) {
			showTEV(shadingStage.getSelectedIndex());
		}
    }//GEN-LAST:event_shadingStageActionPerformed

	private void texEnableSeq() {
		boolean is = textureMapper.getSelectedIndex() >= 0;
		//ComponentUtils.setComponentsEnabled(is, textureName, textureMagFilter, textureMinFilter, textureRot, textureSX, textureSY, textureTX, textureTY, textureUV, textureWrapU, textureWrapV);
	}

	private void texBtnEnableSeq() {
		btnAddTextureMapper.setEnabled(textureMapper.getItemCount() < 3);
		btnRemoveTextureMapper.setEnabled(textureMapper.getItemCount() > 0);
	}

    private void inBufColSetButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_inBufColSetButtonActionPerformed
		if (mat != null) {
			Color c = JColorChooser.showDialog(this, "Pick a Color", mat.tevStages.inputBufferColor.toColor());
			if (c != null) {
				mat.tevStages.inputBufferColor = new RGBA(c);
				setInputBufferColor();
			}
		}
    }//GEN-LAST:event_inBufColSetButtonActionPerformed

    private void alphaTestEnabledActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_alphaTestEnabledActionPerformed
		atEnableSeq();
    }//GEN-LAST:event_alphaTestEnabledActionPerformed

    private void dtEnabledActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_dtEnabledActionPerformed
		dtEnableSeq();
    }//GEN-LAST:event_dtEnabledActionPerformed

    private void blendOpEnabledActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_blendOpEnabledActionPerformed
		boEnableSeq();
    }//GEN-LAST:event_blendOpEnabledActionPerformed

    private void btnCmdFragLightEnableActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnCmdFragLightEnableActionPerformed
		MaterialProcessor.enableFragmentLighting(mat);
		currentStageIdx = -1;
		showTEV(0, true);
    }//GEN-LAST:event_btnCmdFragLightEnableActionPerformed

    private void btnCmdAlphaEnableActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnCmdAlphaEnableActionPerformed
		if (mat != null) {
			MaterialProcessor.setAlphaBlend(mat);
			showRenderState();
		}
    }//GEN-LAST:event_btnCmdAlphaEnableActionPerformed

    private void btnCmdOutlineSetActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnCmdOutlineSetActionPerformed
		if (mat != null) {
			MaterialProcessor.setEdgeMetaData(mat, isEdgeEnabled.isSelected(), (Integer) edgeGroup.getValue());
			metaDataEditor.loadMetaData(mat.metaData);
			showRenderState();
		}
    }//GEN-LAST:event_btnCmdOutlineSetActionPerformed

    private void btnBumpMapActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnBumpMapActionPerformed
		if (mat != null) {
			if (btnBumpMap.isSelected()) {
				mat.bumpTextureIndex = textureMapper.getSelectedIndex();
			} else {
				mat.bumpMode = MaterialParams.BumpMode.NONE;
			}
			setBumpModeEnabled();
		}
    }//GEN-LAST:event_btnBumpMapActionPerformed

    private void bumpModeActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_bumpModeActionPerformed
		if (mat != null && bumpMode.getSelectedIndex() != -1 && btnBumpMap.isSelected()) {
			mat.bumpMode = bumpMode.getSelectedIndex() == 1 ? MaterialParams.BumpMode.TANGENT : MaterialParams.BumpMode.NORMAL;
		}
    }//GEN-LAST:event_bumpModeActionPerformed

    private void btnSetDefaultShaActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnSetDefaultShaActionPerformed
		if (mat != null) {
			shaderArcName.setText("0@DefaultShader");
			showTEV(currentStageIdx);
		}
    }//GEN-LAST:event_btnSetDefaultShaActionPerformed

    private void btnSetBtlFldShaActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnSetBtlFldShaActionPerformed
		if (mat != null) {
			shaderArcName.setText("0@BattleField");
			saveShading();
		}
    }//GEN-LAST:event_btnSetBtlFldShaActionPerformed

    private void btnSetAmbientColorActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnSetAmbientColorActionPerformed
		if (mat != null) {
			Color c = JColorChooser.showDialog(this, "Pick a Color", mat.ambientColor.toColor());
			if (c != null) {
				mat.ambientColor = new RGBA(c);
				setLightingColors();
			}
		}
    }//GEN-LAST:event_btnSetAmbientColorActionPerformed

    private void btnSetDiffuseColorActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnSetDiffuseColorActionPerformed
		if (mat != null) {
			Color c = JColorChooser.showDialog(this, "Pick a Color", mat.diffuseColor.toColor());
			if (c != null) {
				mat.diffuseColor = new RGBA(c);
				setLightingColors();
			}
		}
    }//GEN-LAST:event_btnSetDiffuseColorActionPerformed

    private void btnAddTextureMapperActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnAddTextureMapperActionPerformed
		if (mat != null) {
			TextureMapper tm = new TextureMapper();
			mat.textures.add(tm);
			textureMapper.addItem(tm);
			textureMapper.setSelectedIndex(textureMapper.getItemCount() - 1);
			texBtnEnableSeq();
		}
    }//GEN-LAST:event_btnAddTextureMapperActionPerformed

    private void btnRemoveTextureMapperActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnRemoveTextureMapperActionPerformed
		if (mat != null) {
			int idx = textureMapper.getSelectedIndex();
			mat.textures.remove(idx);
			textureMapper.setSelectedIndex(Math.min(idx, mat.textures.size() - 1));
			textureMapper.removeItemAt(idx);
			texBtnEnableSeq();
		}
    }//GEN-LAST:event_btnRemoveTextureMapperActionPerformed

    private void textureMapperActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_textureMapperActionPerformed
		showTexture();
    }//GEN-LAST:event_textureMapperActionPerformed

    private void btnAddBakeActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnAddBakeActionPerformed
		if (mat != null) {
			String texName = bakeTexName.getTextureName();
			if (texName != null) {
				MaterialProcessor.addBakeMap(mat, texName, bakeUV.getSelectedIndex(), MaterialProcessor.BakeMode.values()[bakeFormula.getSelectedIndex()]);
				buildTexMapperBox();
				showTEV(0);
			}
		}
    }//GEN-LAST:event_btnAddBakeActionPerformed

    private void btnSetSpc0ColorActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnSetSpc0ColorActionPerformed
		if (mat != null) {
			Color c = JColorChooser.showDialog(this, "Pick a Color", mat.specular0Color.toColor());
			if (c != null) {
				mat.specular0Color = new RGBA(c);
				setLightingColors();
			}
		}
    }//GEN-LAST:event_btnSetSpc0ColorActionPerformed

    private void btnSetSpc1ColorActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnSetSpc1ColorActionPerformed
		if (mat != null) {
			Color c = JColorChooser.showDialog(this, "Pick a Color", mat.specular1Color.toColor());
			if (c != null) {
				mat.specular1Color = new RGBA(c);
				setLightingColors();
			}
		}
    }//GEN-LAST:event_btnSetSpc1ColorActionPerformed

    private void textureNameActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_textureNameActionPerformed
		if (scene != null) {
			String texName = textureName.getTextureName();
			texturePreview.showTexture(scene.getResTexture(texName));
			if (mat != null) {
				if (currentTexIdx >= 0 && currentTexIdx < mat.textures.size()) {
					TextureMapper tm = mat.textures.get(currentTexIdx);
					tm.textureName = texName;
				}
			}
		}
    }//GEN-LAST:event_textureNameActionPerformed

    private void lutActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_lutActionPerformed
		if (mat != null) {
			showLUT();
		}
    }//GEN-LAST:event_lutActionPerformed

	private LUT getCurrentLUT() {
		if (mat != null) {
			int lutTarget = this.lut.getSelectedIndex();
			if (lutTarget != -1) {
				return mat.getLUTForTarget(MaterialParams.LUTTarget.values()[lutTarget]);
			}
		}
		return null;
	}

	private void showLUT() {
		LUT lut = mat.getLUTForTarget(MaterialParams.LUTTarget.values()[this.lut.getSelectedIndex()]);
		btnIsLUTEnabled.setSelected(lut != null);
		ComponentUtils.setComponentsEnabled(lut != null, lutTextureName, lutInput);
		if (lut != null) {
			lutTextureName.setTextureName(lut.textureName);
			lutInput.setSelectedIndex(lut.source.ordinal());
		} else {
			lutTextureName.setTextureName(null);
			lutInput.setSelectedItem(null);
		}
	}

    private void btnIsLUTEnabledActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnIsLUTEnabledActionPerformed
		boolean v = btnIsLUTEnabled.isSelected();
		ComponentUtils.setComponentsEnabled(v, lutTextureName, lutInput);
		if (mat != null) {
			int lutTarget = this.lut.getSelectedIndex();
			if (lutTarget != -1) {
				MaterialParams.LUTTarget lutTargetType = MaterialParams.LUTTarget.values()[lutTarget];
				if (!v) {
					mat.LUTs.remove(mat.getLUTForTarget(lutTargetType));
				} else {
					LUT lut = mat.getLUTForTarget(lutTargetType);
					if (lut == null) {
						lut = new LUT();
						lut.target = lutTargetType;
						lut.source = MaterialParams.LUTSource.LIGHT_NORMAL;
						mat.LUTs.add(lut);
					}
				}
				showLUT();
			}
		}
    }//GEN-LAST:event_btnIsLUTEnabledActionPerformed

    private void lutTextureNameActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_lutTextureNameActionPerformed
		LUT lut = getCurrentLUT();
		if (lut != null) {
			lut.textureName = lutTextureName.getTextureName();
		}
    }//GEN-LAST:event_lutTextureNameActionPerformed

    private void lutInputActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_lutInputActionPerformed
		LUT lut = getCurrentLUT();
		if (lut != null) {
			int idx = lutInput.getSelectedIndex();
			if (idx != -1) {
				lut.source = MaterialParams.LUTSource.values()[idx];
			}
		}
    }//GEN-LAST:event_lutInputActionPerformed

    private void constantAsgnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_constantAsgnActionPerformed
		if (mat != null) {
			int stage = shadingStage.getSelectedIndex();
			if (stage != -1) {
				mat.tevStages.stages[stage].constantColor = MaterialColorType.forCColIndex(constantAsgn.getSelectedIndex());
				setTEVConstantColor(stage);
			}
		}
    }//GEN-LAST:event_constantAsgnActionPerformed

    private void cModeRgbActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cModeRgbActionPerformed
		setTEVEquations(shadingStage.getSelectedIndex());
    }//GEN-LAST:event_cModeRgbActionPerformed

    private void cModeAActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cModeAActionPerformed
		setTEVEquations(shadingStage.getSelectedIndex());
    }//GEN-LAST:event_cModeAActionPerformed

    private void bakeTexNameActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_bakeTexNameActionPerformed
		// TODO add your handling code here:
    }//GEN-LAST:event_bakeTexNameActionPerformed

    private void btnDisableAlphaBlendActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnDisableAlphaBlendActionPerformed
		if (mat != null) {
			MaterialProcessor.unsetAlphaBlend(mat);
			showRenderState();
		}
    }//GEN-LAST:event_btnDisableAlphaBlendActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel alphaCombinerPanel;
    private javax.swing.JCheckBox alphaTestEnabled;
    private javax.swing.JComboBox<String> alphaTestFunc;
    private javax.swing.JLabel alphaTestFuncLabel;
    private javax.swing.JPanel alphaTestPanel;
    private javax.swing.JFormattedTextField alphaTestReference;
    private javax.swing.JLabel alphaTestReferenceLabel;
    private javax.swing.JLabel ambientColorLabel;
    private javax.swing.JLabel ambientColorPreview;
    private javax.swing.JComboBox<String> bakeFormula;
    private javax.swing.JLabel bakeFormulaLabel;
    private javax.swing.JLabel bakeTexLabel;
    private ctrmap.creativestudio.editors.CSTextureSelector bakeTexName;
    private javax.swing.JComboBox<String> bakeUV;
    private javax.swing.JLabel bakeUVLabel;
    private javax.swing.JLabel blendOpAlphaLabel;
    private javax.swing.JLabel blendOpColorLabel;
    private javax.swing.JLabel blendOpDstFuncLabel;
    private javax.swing.JCheckBox blendOpEnabled;
    private javax.swing.JLabel blendOpEquationLabel;
    private javax.swing.JPanel blendOpPanel;
    private javax.swing.JLabel blendOpRGBLabel;
    private javax.swing.JLabel blendOpSrcFuncLabel;
    private javax.swing.JLabel boColorPreview;
    private javax.swing.JButton boConstantColorSet;
    private javax.swing.JComboBox<String> boDstFuncAlpha;
    private javax.swing.JComboBox<String> boDstFuncRgb;
    private javax.swing.JComboBox<String> boEqtAlpha;
    private javax.swing.JComboBox<String> boEqtRgb;
    private javax.swing.JComboBox<String> boSrcFuncAlpha;
    private javax.swing.JComboBox<String> boSrcFuncRgb;
    private javax.swing.JButton btnAddBake;
    private javax.swing.JButton btnAddTextureMapper;
    private javax.swing.JCheckBox btnBumpMap;
    private javax.swing.JButton btnCmdAlphaEnable;
    private javax.swing.JButton btnCmdFragLightEnable;
    private javax.swing.JButton btnCmdOutlineSet;
    private javax.swing.JButton btnDisableAlphaBlend;
    private javax.swing.JCheckBox btnIsLUTEnabled;
    private javax.swing.JButton btnRemoveTextureMapper;
    private javax.swing.JButton btnSetAmbientColor;
    private javax.swing.JButton btnSetBtlFldSha;
    private javax.swing.JButton btnSetDefaultSha;
    private javax.swing.JButton btnSetDiffuseColor;
    private javax.swing.JButton btnSetSpc0Color;
    private javax.swing.JButton btnSetSpc1Color;
    private javax.swing.JPanel bumpMapPanel;
    private javax.swing.JComboBox<String> bumpMode;
    private javax.swing.JLabel bumpModeLabel;
    private javax.swing.ButtonGroup buttonGroup1;
    private javax.swing.JPanel cConstColorPanel;
    private javax.swing.JLabel cEqtPreviewA;
    private javax.swing.JLabel cEqtPreviewRgb;
    private javax.swing.JComboBox<String> cModeA;
    private javax.swing.JLabel cModeLabelA;
    private javax.swing.JComboBox<String> cModeRgb;
    private javax.swing.JLabel cModeRgbLabel;
    private javax.swing.JLabel cOf0Label;
    private javax.swing.JLabel cOf0RgbLabel;
    private javax.swing.JLabel cOf1Label;
    private javax.swing.JLabel cOf1RgbLabel;
    private javax.swing.JLabel cOf2Label;
    private javax.swing.JLabel cOf2RgbLabel;
    private javax.swing.JComboBox<String> cOp0A;
    private javax.swing.JLabel cOp0ALabel;
    private javax.swing.JComboBox<String> cOp0Rgb;
    private javax.swing.JLabel cOp0RgbLabel;
    private javax.swing.JComboBox<String> cOp1A;
    private javax.swing.JLabel cOp1ALabel;
    private javax.swing.JComboBox<String> cOp1Rgb;
    private javax.swing.JLabel cOp1RgbLabel;
    private javax.swing.JComboBox<String> cOp2A;
    private javax.swing.JLabel cOp2ALabel;
    private javax.swing.JComboBox<String> cOp2Rgb;
    private javax.swing.JLabel cOp2RgbLabel;
    private javax.swing.JComboBox<String> cScaleA;
    private javax.swing.JLabel cScaleALabel;
    private javax.swing.JComboBox<String> cScaleRgb;
    private javax.swing.JLabel cScaleRgbLabel;
    private javax.swing.JComboBox<String> cSrc0A;
    private javax.swing.JComboBox<String> cSrc0Rgb;
    private javax.swing.JComboBox<String> cSrc1A;
    private javax.swing.JComboBox<String> cSrc1Rgb;
    private javax.swing.JComboBox<String> cSrc2A;
    private javax.swing.JComboBox<String> cSrc2Rgb;
    private javax.swing.JCheckBox cUpdateA;
    private javax.swing.JCheckBox cUpdateRgb;
    private javax.swing.JLabel ccolAsgnLabel;
    private javax.swing.JPanel colorCombinerPanel;
    private javax.swing.JPanel colorPanel;
    private javax.swing.JComboBox<String> constantAsgn;
    private xstandard.gui.components.SimpleColorSelector constantColorPicker;
    private javax.swing.JComboBox<String> depthFunc;
    private javax.swing.JLabel depthFuncLabel;
    private javax.swing.JPanel depthTestPanel;
    private javax.swing.JLabel diffuseColorLabel;
    private javax.swing.JLabel diffuseColorPreview;
    private javax.swing.JCheckBox dtAlphaWrite;
    private javax.swing.JCheckBox dtBlueWrite;
    private javax.swing.JCheckBox dtDepthWrite;
    private javax.swing.JCheckBox dtEnabled;
    private javax.swing.JCheckBox dtGreenWrite;
    private javax.swing.JCheckBox dtRedWrite;
    private javax.swing.JFormattedTextField edgeGroup;
    private javax.swing.JLabel edgeGroupLabel;
    private javax.swing.JComboBox<String> faceCulling;
    private javax.swing.JPanel faceCullingPanel;
    private javax.swing.JPanel generalPanel;
    private javax.swing.JLabel inBufColPreview;
    private javax.swing.JSeparator inBufColSeparator;
    private javax.swing.JButton inBufColSetButton;
    private javax.swing.JLabel inputBufferColorLabel;
    private javax.swing.JCheckBox isEdgeEnabled;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JSpinner lightLayer;
    private javax.swing.JLabel lightLayerLabel;
    private javax.swing.JSpinner lightSetIndex;
    private javax.swing.JPanel lightingGeneralPanel;
    private javax.swing.JPanel lightingPanel;
    private javax.swing.JLabel lsiLabel;
    private javax.swing.JComboBox<String> lut;
    private javax.swing.JComboBox<String> lutInput;
    private javax.swing.JLabel lutInputLabel;
    private javax.swing.JLabel lutLabel;
    private javax.swing.JPanel lutPanel;
    private javax.swing.JLabel lutTextureLabel;
    private ctrmap.creativestudio.editors.CSTextureSelector lutTextureName;
    private javax.swing.JTabbedPane materialEditorTabbedPane;
    private javax.swing.JTextField materialName;
    private javax.swing.JLabel materialNameLabel;
    private ctrmap.creativestudio.editors.MetaDataEditor metaDataEditor;
    private javax.swing.JPanel metaDataPanel;
    private javax.swing.JSeparator outlineSep;
    private javax.swing.JPanel renderStatePanel;
    private ctrmap.creativestudio.editors.SaveControlPanel saveCtrl;
    private javax.swing.JLabel shaderArcLabel;
    private javax.swing.JTextField shaderArcName;
    private javax.swing.JPanel shadingPanel;
    private javax.swing.JComboBox<String> shadingStage;
    private javax.swing.JLabel shadingStageLabel;
    private javax.swing.JPanel shortcutsPanel;
    private javax.swing.JComboBox<String> soFail;
    private javax.swing.JLabel soFailLabel;
    private javax.swing.JComboBox<String> soPass;
    private javax.swing.JLabel soPassLabel;
    private javax.swing.JSeparator soSep;
    private javax.swing.JComboBox<String> soZPass;
    private javax.swing.JLabel soZPassLabel;
    private javax.swing.JLabel spc0ColorLabel;
    private javax.swing.JLabel spc0ColorPreview;
    private javax.swing.JLabel spc1ColorLabel;
    private javax.swing.JLabel spc1ColorPreview;
    private javax.swing.JFormattedTextField stencilBufMask;
    private javax.swing.JLabel stencilBufMaskLabel;
    private javax.swing.JCheckBox stencilEnabled;
    private javax.swing.JComboBox<String> stencilFunc;
    private javax.swing.JLabel stencilFuncLabel;
    private javax.swing.JFormattedTextField stencilMask;
    private javax.swing.JLabel stencilMaskLabel;
    private javax.swing.JPanel stencilOpPanel;
    private javax.swing.JFormattedTextField stencilRef;
    private javax.swing.JLabel stencilRefLabel;
    private javax.swing.JPanel texCmbPanel;
    private javax.swing.JPanel textureFilteringPanel;
    private javax.swing.JComboBox<String> textureMagFilter;
    private javax.swing.JLabel textureMagFilterLabel;
    private javax.swing.JComboBox<TextureMapper> textureMapper;
    private javax.swing.JLabel textureMapperLabel;
    private javax.swing.JPanel textureMappingPanel;
    private javax.swing.JComboBox<String> textureMinFilter;
    private javax.swing.JLabel textureMinFilterLabel;
    private ctrmap.creativestudio.editors.CSTextureSelector textureName;
    private javax.swing.JLabel textureNameLabel;
    private ctrmap.util.gui.TexturePreview texturePreview;
    private javax.swing.JFormattedTextField textureRot;
    private javax.swing.JFormattedTextField textureRotYDummy;
    private javax.swing.JLabel textureRotationLabel;
    private javax.swing.JFormattedTextField textureSX;
    private javax.swing.JFormattedTextField textureSY;
    private javax.swing.JLabel textureScaleLabel;
    private javax.swing.JSeparator textureSelectionSeparator;
    private javax.swing.JFormattedTextField textureTX;
    private javax.swing.JFormattedTextField textureTY;
    private javax.swing.JPanel textureTransformPanel;
    private javax.swing.JLabel textureTransformXLabel;
    private javax.swing.JLabel textureTransformYLabel;
    private javax.swing.JLabel textureTranslationLabel;
    private xstandard.gui.components.combobox.ComboBoxAndSpinner textureUV;
    private javax.swing.JLabel textureUVLabel;
    private javax.swing.JComboBox<String> textureWrapU;
    private javax.swing.JLabel textureWrapULabel;
    private javax.swing.JComboBox<String> textureWrapV;
    private javax.swing.JLabel textureWrapVLabel;
    private javax.swing.JPanel texturingPanel;
    private javax.swing.JPanel vshPanel;
    // End of variables declaration//GEN-END:variables
}
