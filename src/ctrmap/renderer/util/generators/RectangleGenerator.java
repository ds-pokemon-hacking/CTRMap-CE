package ctrmap.renderer.util.generators;

import xstandard.math.vec.RGBA;
import ctrmap.renderer.scenegraph.G3DResource;
import ctrmap.renderer.scene.model.ModelInstance;
import java.awt.Point;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class RectangleGenerator {
	
	private static Map<LineWidthColorSet, G3DResource> modelCache = new HashMap<>();
	
	public static ModelInstance generateGridRectangle(int width, int height, int lineWidth, float gridUnitSize, RGBA color){
		LineWidthColorSet key = new LineWidthColorSet(lineWidth, color);
		if (!modelCache.containsKey(key)){
			modelCache.put(key, GridGenerator.generateGrid(gridUnitSize, 0, 1, lineWidth, color));			
		}
		
		ModelInstance i = modelCache.get(key).createInstance();
		
		i.s.x = width;
		i.s.z = height;
		i.p.x = gridUnitSize * width;
		i.p.z = gridUnitSize * height;
		return i;
	}
	
	public static void setGridRectScale(Point selectorStart, Point selectorEnd, float gridUnitSize, ModelInstance mi){
		float x1 = Math.min(selectorStart.x, selectorEnd.x);
		float y1 = Math.min(selectorStart.y, selectorEnd.y);
		float x2 = Math.max(selectorStart.x, selectorEnd.x);
		float y2 = Math.max(selectorStart.y, selectorEnd.y);
		float width = x2 - x1 + 1;
		float height = y2 - y1 + 1;
		mi.s.x = width;
		mi.s.z = height;
		mi.p.x = x1 * gridUnitSize;
		mi.p.z = y1 * gridUnitSize;
	}
	
	private static class LineWidthColorSet{
		private int lineWidth;
		private RGBA color;
		
		public LineWidthColorSet(int lineWidth, RGBA color){
			this.lineWidth = lineWidth;
			this.color = color;
		}
		
		@Override
		public boolean equals(Object o){
			if (o != null && o instanceof LineWidthColorSet){
				LineWidthColorSet l = (LineWidthColorSet)o;
				return lineWidth == l.lineWidth && color.equals(l.color);
			}
			return false;
		}

		@Override
		public int hashCode() {
			int hash = 3;
			hash = 59 * hash + this.lineWidth;
			hash = 59 * hash + Objects.hashCode(this.color);
			return hash;
		}
	}
}
