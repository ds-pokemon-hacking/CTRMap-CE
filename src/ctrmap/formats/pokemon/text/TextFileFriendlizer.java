
package ctrmap.formats.pokemon.text;

/**
 *
 */
public class TextFileFriendlizer {
	
	private static FriendlyReplacement[] PATTERNS = new FriendlyReplacement[]{
		new FriendlyReplacement("\n", "\\n"),
		new FriendlyReplacement("[CLEAR]", "\\c"),
		new FriendlyReplacement("[SCROLL]", "\\r")
	};
	
	public static String getFriendlized(String line){
		for (FriendlyReplacement p : PATTERNS){
			if (line.contains(p.original)){
				line = line.replace(p.original, p.friendly);
			}
		}
		return line;
	}
	
	public static String getDefriendlized(String line){
		for (FriendlyReplacement p : PATTERNS){
			if (line.contains(p.friendly)){
				line = line.replace(p.friendly, p.original);
			}
		}
		return line;
	}
	
	private static class FriendlyReplacement {
		public final String friendly;
		public final String original;
		
		public FriendlyReplacement(String original, String friendly){
			this.friendly = friendly;
			this.original = original;
		}
	}
}
