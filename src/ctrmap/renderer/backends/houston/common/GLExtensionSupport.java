
package ctrmap.renderer.backends.houston.common;

import com.jogamp.opengl.GL;
import com.jogamp.opengl.GL2;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class GLExtensionSupport {
	
	public static final String NV_TEXTURE_SHADER3 = "GL_NV_texture_shader3";
	
	private final List<String> extensions = new ArrayList<>();
	
	public void init(GL gl){
		extensions.addAll(Arrays.asList(callExtensionQuery(gl).split(" ")));
	}
	
	public boolean supportsExtension(String extensionName){
		return extensions.contains(extensionName);
	}
	
	private String callExtensionQuery(GL gl){
		if (gl instanceof GL2){
			return gl.glGetString(GL2.GL_EXTENSIONS);
		}
		else {
			throw new UnsupportedOperationException("GLExtensionSupport only accepts OpenGL 2 objects.");
		}
	}
}
