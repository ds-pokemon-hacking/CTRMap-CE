
package ctrmap.creativestudio.ngcs.rtldr;

import java.awt.Frame;
import rtldr.RExtensionBase;

public interface INGCSPlugin extends RExtensionBase<NGCSJulietIface> {
	public default void registerFormats(NGCSJulietIface j) {
		
	}
	
	public default void registerUI(NGCSJulietIface j, Frame uiParent, NGCSContentAccessor contentAccessor) {
		
	}
}
