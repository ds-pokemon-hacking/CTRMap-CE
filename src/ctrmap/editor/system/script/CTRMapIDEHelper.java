package ctrmap.editor.system.script;

import ctrmap.editor.system.workspace.CTRMapProject;
import ctrmap.editor.system.workspace.UserData;
import ctrmap.pokescript.LangPlatform;
import ctrmap.pokescript.ide.PSIDE;
import ctrmap.pokescript.ide.PSIDETemplateVar;
import ctrmap.pokescript.ide.system.IDEResourceReference;
import ctrmap.pokescript.ide.system.ResourcePathType;
import ctrmap.pokescript.ide.system.project.IDEProject;
import ctrmap.pokescript.ide.system.project.include.Dependency;
import ctrmap.pokescript.ide.system.project.include.DependencyType;
import ctrmap.pokescript.ide.system.savedata.IDEWorkspace;
import ctrmap.scriptformats.gen5.VCommandDataBase;
import ctrmap.scriptformats.gen5.VDecompiler;
import ctrmap.scriptformats.gen5.VScriptFile;
import xstandard.formats.zip.ZipArchive;
import xstandard.fs.FSFile;
import ctrmap.formats.common.GameInfo;
import ctrmap.pokescript.ide.system.project.IDEProjectManifest;
import xstandard.formats.yaml.Yaml;
import xstandard.gui.DialogUtils;
import xstandard.res.ResourceAccess;
import xstandard.text.FormattingUtils;
import xstandard.text.StringEx;
import xstandard.util.ParsingUtils;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CTRMapIDEHelper {

	private static final IDEResourceReference CTRMAP_IDE_SETUP_REF = new IDEResourceReference(ResourcePathType.INTERNAL, "scripting/cm_ide/IDE.yml");

	public static final IDEResourceReference CTRMAP_DL_SDK_GENV_BW1 = new IDEResourceReference(ResourcePathType.INTERNAL, "scripting/cm_ide/sdk/EV_GEN_V/SDK5-BW-Generated.lib");
	public static final IDEResourceReference CTRMAP_DL_SDK_GENV_BW2 = new IDEResourceReference(ResourcePathType.INTERNAL, "scripting/cm_ide/sdk/EV_GEN_V/SDK5-B2W2-Generated.lib");
	public static final IDEResourceReference CTRMAP_DL_SDK_GENVI = new IDEResourceReference(ResourcePathType.INTERNAL, "scripting/cm_ide/sdk/AMX_CTR/PokeScriptSDK6-master.lib");

	private final FSFile workspaceRoot;

	private final IDEWorkspace workspace;

	private CTRMapProject cmproject;

	private PSIDE ide = null;

	private Map<Integer, VCommandDataBase> commandDBs = new HashMap<>();

	private Map<FSFile, IDEProject> loadedProjects = new HashMap<>();

	public CTRMapIDEHelper(CTRMapProject project) {
		this.cmproject = project;
		workspaceRoot = project.userData.getUserDataDir(UserData.UsrDirectory.SCRIPT_WORKSPACE);
		workspace = new IDEWorkspace(workspaceRoot);

		if (project.gameInfo.isGenV()) {
			loadBWCommandDatabases(project.gameInfo);
		}
	}

	private void loadBWCommandDatabases(GameInfo game) {
		ZipArchive commandDBArc = new ZipArchive(ResourceAccess.getResourceFile((game.isBW2() ? CTRMAP_DL_SDK_GENV_BW2 : CTRMAP_DL_SDK_GENV_BW1).path));

		for (FSFile child : commandDBArc.getChild("yml").listFiles()) {
			String name = child.getName();
			if (name.endsWith(Yaml.EXTENSION_FILTER.getPrimaryExtension())) {
				VCommandDataBase db = new VCommandDataBase(child);

				name = child.getNameWithoutExtension();

				if (name.equals("Base")) {
					commandDBs.put(-1, db);
				} else {
					if (name.startsWith("Overlay")) {
						name = StringEx.deleteAllString(name, "Overlay").trim();
						int ovlId = ParsingUtils.parseBasedIntOrDefault(name, -1);
						if (ovlId != -1) {
							commandDBs.put(ovlId, db);
						}
					}
				}
			}
		}
	}

	public VCommandDataBase getCommandDBByOvlNo(int ovlNo) {
		return commandDBs.get(ovlNo);
	}

	public boolean hasProject(ProjectSetupParam setupParam) {
		return workspace.getProjectDir(setupParam.getName()).isDirectory();
	}

	public IDEProject getOrCreateProject(ProjectSetupParam setupParam) {
		getIDE();
		String name = setupParam.getName();
		FSFile projectDir = workspace.getProjectDir(name);
		if (loadedProjects.containsKey(projectDir)) {
			return loadedProjects.get(projectDir);
		}
		System.out.println("Loading new IDE project " + projectDir + " (" + projectDir.getClass().getSimpleName() + ")");
		if (projectDir.isFile()) {
			DialogUtils.showErrorMessage("Error", "Could not create project \"" + name + "\": Directory occupied by file.");
			return null;
		}
		projectDir.mkdirs();
		FSFile manifest = projectDir.getChild(name + IDEProject.IDE_PROJECT_EXTENSION_FILTER.getPrimaryExtension());
		IDEProject project;
		if (manifest.exists()) {
			project = new IDEProject(manifest);
		} else {
			project = new IDEProject(
				projectDir,
				name,
				"ctrmap." + cmproject.getProjectName() + "." + name,
				cmproject.gameInfo.isGenV() ? LangPlatform.EV_SWAN : LangPlatform.AMX_CTR
			);
			
			IDEProjectManifest mf = project.getManifest();

			String mainClassName = setupParam.getMainClassName();
			if (mainClassName != null) {
				FSFile mainClassFile = project.getClassFile(mainClassName);
				mainClassFile.setBytes(setupParam.getMainClassBytes());
				mf.setMainClass(mainClassName);
			}

			Dependency sdk = new Dependency(DependencyType.LIBRARY);
			String compilerDef = null;

			switch (cmproject.gameInfo.getGame()) {
				case BW:
					sdk.ref = CTRMAP_DL_SDK_GENV_BW1;
					compilerDef = "WB";
					break;
				case BW2:
					sdk.ref = CTRMAP_DL_SDK_GENV_BW2;
					compilerDef = "SWAN";
					break;
				case ORAS:
				case ORAS_DEMO:
					sdk.ref = CTRMAP_DL_SDK_GENVI;
					compilerDef = "SANGO";
					break;
				case XY:
					sdk.ref = CTRMAP_DL_SDK_GENVI;
					compilerDef = "XY";
					break;
			}

			mf.addDependency(sdk);
			mf.addCompilerDefinition(compilerDef);
		}
		loadedProjects.put(projectDir, project);
		return project;
	}

	public void openProjectInIDE(IDEProject project) {
		getIDE().openProject(project);
	}

	public PSIDE getIDE() {
		if (ide == null) {
			ide = new PSIDE(CTRMAP_IDE_SETUP_REF);
			ide.loadWorkspace(workspace);
			for (IDEProject proj : ide.context.openedProjects) {
				System.out.println("Registering already opened IDE project " + proj.getFSRoot() + " (" + proj.getFSRoot().getClass().getSimpleName() + ")");
				loadedProjects.put(proj.getFSRoot(), proj);
			}
		}
		return ide;
	}

	public VCommandDataBase createCombCommandDB(int... overlayIds) {
		List<VCommandDataBase> databases = new ArrayList<>();
		databases.add(getCommandDBByOvlNo(-1));
		for (int ovlId : overlayIds) {
			if (ovlId != -1) {
				databases.add(getCommandDBByOvlNo(ovlId));
			}
		}

		return new VCommandDataBase(databases);
	}

	public static interface ProjectSetupParam {

		public String getName();

		public String getMainClassName();

		public byte[] getMainClassBytes();
	}

	public static class ZoneEventProjectSetupParam5Decomp extends ZoneEventProjectSetupParam5 {

		private final VScriptFile script;
		private final VCommandDataBase cdb;

		public ZoneEventProjectSetupParam5Decomp(
			int zoneId,
			int textFileId,
			VScriptFile script,
			int[] overlayIds,
			GameInfo game,
			CTRMapIDEHelper ideHelper
		) {
			super(zoneId, textFileId, game);
			this.script = script;
			cdb = ideHelper.createCombCommandDB(overlayIds);
		}

		@Override
		public byte[] getMainClassBytes() {
			if (game.isGenV()) {
				VDecompiler decompiler = new VDecompiler(script, cdb);
				decompiler.overrideClassName = getMainClassName();
				decompiler.decompile();
				try {
					StringBuilder sb = new StringBuilder();
					
					sb.append("import messages.script.Msg").append(FormattingUtils.getIntWithLeadingZeros(4, textFileId)).append(".MSGID;\n\n");
					
					sb.append(decompiler.dump());
					
					return sb.toString().getBytes();
				} catch (Exception ex) {
					ex.printStackTrace();
					DialogUtils.showErrorMessage("Decompilation failed", "The file failed to decompile. Starting from scratch.");
				}
				return super.getMainClassBytes();
			} else {
				throw new UnsupportedOperationException();
			}
		}
	}

	public static class ZoneEventProjectSetupParam5 extends ZoneEventProjectSetupParam {

		public ZoneEventProjectSetupParam5(int zoneId, int textFileId, GameInfo game) {
			super(zoneId, textFileId, game);
		}

		@Override
		public byte[] getMainClassBytes() {
			return PSIDE.getTemplateData(
				"Template5.pks",
				new PSIDETemplateVar("CLASSNAME", getMainClassName()),
				new PSIDETemplateVar("TEXTNUM", FormattingUtils.getIntWithLeadingZeros(4, textFileId))
			);
		}
	}

	public static class ZoneEventProjectSetupParam6 extends ZoneEventProjectSetupParam {

		public ZoneEventProjectSetupParam6(int zoneId, int textFileId, GameInfo game) {
			super(zoneId, textFileId, game);
		}

		@Override
		public byte[] getMainClassBytes() {
			return PSIDE.getTemplateData(
				"EventMainClass6.pks",
				new PSIDETemplateVar("CLASSNAME", getMainClassName()),
				new PSIDETemplateVar("ZONENUM", String.valueOf(zoneId)),
				new PSIDETemplateVar("TEXTNUM", FormattingUtils.getIntWithLeadingZeros(4, textFileId))
			);
		}
	}

	public static abstract class ZoneEventProjectSetupParam implements ProjectSetupParam {

		protected final int zoneId;
		protected final int textFileId;
		protected final GameInfo game;

		public ZoneEventProjectSetupParam(int zoneId, int textFileId, GameInfo game) {
			this.zoneId = zoneId;
			this.game = game;
			this.textFileId = textFileId;
		}

		@Override
		public String getName() {
			return "ZD" + FormattingUtils.getIntWithLeadingZeros(4, zoneId) + "-Events";
		}

		@Override
		public String getMainClassName() {
			return "MainEvents";
		}

		@Override
		public abstract byte[] getMainClassBytes();
	}

	public static class ZoneInitProjectSetupParam implements ProjectSetupParam {

		protected final int zoneId;
		protected final GameInfo game;
		
		public ZoneInitProjectSetupParam(int zoneId, GameInfo game) {
			this.zoneId = zoneId;
			this.game = game;
		}

		@Override
		public String getName() {
			return "ZD" + FormattingUtils.getIntWithLeadingZeros(4, zoneId) + "-Init";
		}

		@Override
		public String getMainClassName() {
			return "InitZone";
		}

		@Override
		public byte[] getMainClassBytes() {
			if (game.isGenV()) {
				throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
			} else {
				return PSIDE.getTemplateData(
					"InitTemplate6.pks",
					new PSIDETemplateVar("CLASSNAME", getMainClassName()),
					new PSIDETemplateVar("ZONENUM", String.valueOf(zoneId))
				);
			}
		}
	}

	public static class CommonScrProjectSetupParam implements ProjectSetupParam {

		private String scrName;
		private GameInfo game;

		public CommonScrProjectSetupParam(String scrName, GameInfo game) {
			this.scrName = scrName;
			this.game = game;
		}

		@Override
		public String getName() {
			return scrName;
		}

		@Override
		public String getMainClassName() {
			return scrName;
		}

		@Override
		public byte[] getMainClassBytes() {
			return PSIDE.getTemplateData(
				game.isGenV() ? "Template5.pks" : "CommonScrTemplate6.pks",
				new PSIDETemplateVar("CLASSNAME", getMainClassName())
			);
		}
	}
}
