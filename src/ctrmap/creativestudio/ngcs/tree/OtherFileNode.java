
package ctrmap.creativestudio.ngcs.tree;

import ctrmap.formats.generic.interchange.CMIFFile;
import ctrmap.renderer.scene.metadata.MetaDataValue;
import ctrmap.renderer.scenegraph.G3DResource;
import ctrmap.renderer.scenegraph.NamedResource;
import xstandard.util.ListenableList;
import java.util.List;

public class OtherFileNode extends CSNode {

	public static final int RESID = 0x42010A;
	
	private CMIFFile.OtherFile otherFile;

	public OtherFileNode(CMIFFile.OtherFile mesh, CSJTree tree) {
		super(tree);
		this.otherFile = mesh;
	}
	
	@Override
	public int getIconResourceID() {
		return RESID;
	}

	@Override
	public String getNodeName() {
		return otherFile.name;
	}

	@Override
	public NamedResource getContent() {
		return otherFile;
	}

	@Override
	public ListenableList getParentList() {
		return getCS().getOthers();
	}

	@Override
	public CSNodeContentType getContentType() {
		return CSNodeContentType.OTHER;
	}

	@Override
	public void setContent(NamedResource cnt) {
		otherFile = (CMIFFile.OtherFile) cnt;
	}
	
	@Override
	public void putForExport(G3DResource rsc) {
		rsc.metaData.putValue(otherFile.name, otherFile.data);
	}

	@Override
	public NamedResource getReplacement(G3DResource rsc) {
		List<MetaDataValue> l = rsc.metaData.getValues();
		for (MetaDataValue v : l) {
			if (v.getType() == MetaDataValue.Type.RAW_BYTES) {
				return new CMIFFile.OtherFile(v.getName(), v.byteArrValue());
			}
		}
		
		return null;
	}
}
