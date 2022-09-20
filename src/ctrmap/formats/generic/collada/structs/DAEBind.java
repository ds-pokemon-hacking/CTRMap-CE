package ctrmap.formats.generic.collada.structs;

import ctrmap.formats.generic.collada.XmlFormat;
import java.util.ArrayList;
import java.util.List;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class DAEBind implements DAESerializable {
	
	public BindType type;
	
	public String bindName;
	public String targetBindInputSemantic;
	public int targetBindInputSetNo = -1;
	
	public List<DAEInstance> subInstances = new ArrayList<>();
	
	public DAEBind(BindType type) {
		this.type = type;
	}
	
	public DAEBind(Element elem){
		String fn = elem.getTagName();
		for (BindType t : BindType.values()){
			if (t.hasFN(fn)){
				type = t;
				break;
			}
		}
		
		bindName = elem.getAttribute("semantic");
		targetBindInputSemantic = elem.getAttribute("input_semantic");
		targetBindInputSetNo = XmlFormat.getIntAttribute(elem, "input_set");
		
		Element techniqueCommon = XmlFormat.getParamElement(elem, "technique_common");
		if (techniqueCommon != null){
			List<Element> elems = XmlFormat.getElementList(XmlFormat.nodeListToListOfNodes(techniqueCommon.getChildNodes()));
			for (Element inst : elems){
				if (inst.getTagName().contains("instance")){
					subInstances.add(new DAEInstance(inst));
				}
			}
		}
	}

	public Element createElement(Document doc) {
		Element e = doc.createElement(type.fn);
		XmlFormat.setAttributeNonNull(e, "semantic", bindName);
		XmlFormat.setAttributeNonNull(e, "input_semantic", targetBindInputSemantic);
		XmlFormat.setAttributeNonMinus1(e, "input_set", targetBindInputSetNo);
		
		Element tech = doc.createElement("technique_common");
		
		for (DAEInstance sub : subInstances) {
			tech.appendChild(sub.createElement(doc));
		}
		
		e.appendChild(tech);
		
		return e;
	}
	
	public static enum BindType{
		MATERIAL("bind_material"),
		VERTEX_INPUT("bind_vertex_input");
		
		private String fn;
		
		private BindType(String friendlyName){
			fn = friendlyName;
		}
		
		public boolean hasFN(String fn){
			return fn.equals(this.fn);
		}
	}
}
