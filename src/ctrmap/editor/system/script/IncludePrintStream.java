
package ctrmap.editor.system.script;

import xstandard.io.util.IndentedPrintStream;
import xstandard.text.StringEx;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.OutputStream;

public class IncludePrintStream extends IndentedPrintStream {

	public IncludePrintStream(OutputStream out) {
		super(out);
	}
	
	public IncludePrintStream(File f) throws FileNotFoundException {
		super(f);
	}
	
	public void printClassBrief(String headerSubject) {
		IncludeGeneratorCommons.printHeader(this, headerSubject);
	}
	
	public void setPackage(String packageName) {
		print("package ");
		print(packageName);
		println(";\n");
	}

	public void beginClass(String className) {
		print("public class ");
		print(className);
		println(" {");
		incrementIndentLevel();
	}
	
	public void endClass(){
		decrementIndentLevel();
		println("}");
	}
	
	public void printConstantInt(String name, int value) {
		IncludeGeneratorCommons.appendInt(this, name, value);
	}
	
	public void printDoxygenComment(String comment) {
		println("/**");
		String[] lines = StringEx.splitOnecharFast(comment, '\n');
		for (String line : lines) {
			print("* ");
			println(line);
		}
		println("*/");
	}
}
