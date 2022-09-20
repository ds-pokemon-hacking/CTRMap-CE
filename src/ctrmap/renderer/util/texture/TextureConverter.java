package ctrmap.renderer.util.texture;

import ctrmap.renderer.scene.texturing.Texture;
import ctrmap.renderer.scene.texturing.formats.TextureFormatHandler;
import xstandard.fs.FSFile;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;

public class TextureConverter {

	/*
	 * https://stackoverflow.com/questions/4216123/how-to-scale-a-bufferedimage
	 */
	public static BufferedImage getScaledImage(BufferedImage img, int w, int h) {
		BufferedImage resized = new BufferedImage(w, h, img.getType());
		Graphics2D g = resized.createGraphics();
		g.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
				RenderingHints.VALUE_INTERPOLATION_BICUBIC);
		g.drawImage(img, 0, 0, w, h, 0, 0, img.getWidth(), img.getHeight(), null);
		g.dispose();
		return resized;
	}

	public static Texture readTextureFromFile(File f) {
		try {
			BufferedImage tex = ImageIO.read(f);
			if (tex != null) {
				return texFromBufferedImage(f.getName(), tex, false);
			}
		} catch (Exception ex) {
			Logger.getLogger(TextureConverter.class.getName()).log(Level.SEVERE, null, ex);
		}
		return null;
	}
	
	public static Texture readTextureFromFile(FSFile f) {
		try {
			InputStream in = f.getNativeInputStream();
			BufferedImage tex = ImageIO.read(in);
			if (tex != null) {
				in.close();
				return texFromBufferedImage(f.getName(), tex, false);
			}
		} catch (Exception ex) {
			Logger.getLogger(TextureConverter.class.getName()).log(Level.SEVERE, null, ex);
		}
		return null;
	}
	
	public static void writeTextureToFile(File f, String formatName, Texture tex){
		BufferedImage img = tex.getBufferedImage();
		try {
			ImageIO.write(img, formatName, f);
		} catch (IOException ex) {
			Logger.getLogger(TextureConverter.class.getName()).log(Level.SEVERE, null, ex);
		}
	}
	
	public static void writeTextureToFile(FSFile f, String formatName, Texture tex){
		BufferedImage img = tex.getBufferedImage();
		try {
			OutputStream out = f.getNativeOutputStream();
			ImageIO.write(img, formatName, out);
			out.close();
		} catch (IOException ex) {
			Logger.getLogger(TextureConverter.class.getName()).log(Level.SEVERE, null, ex);
		}
	}

	public static Texture texFromBufferedImage(String name, BufferedImage img, boolean flip) {
		TextureProto proto = getRasterBytesEnsureRGBA(img, flip);
		Texture t = proto.toTexture();
		t.name = name;
		return t;
	}

	private static TextureProto getRasterBytesEnsureRGBA(BufferedImage r, boolean doFlip) {
		return new TextureProto(r, doFlip);
	}

	private static class TextureProto {

		public int w;
		public int h;
		public byte[] dataRGBA;

		public boolean hasAlpha;

		public TextureProto(BufferedImage r, boolean doFlip) {
			w = r.getWidth();
			h = r.getHeight();
			dataRGBA = new byte[w * h * 4];
			for (int x = 0; x < w; x++) {
				for (int y = 0; y < h; y++) {
					int pixel = r.getRGB(x, y);
					dataRGBA[y * w * 4 + x * 4 + 0] = (byte) ((pixel >> 16) & 0xFF);
					dataRGBA[y * w * 4 + x * 4 + 1] = (byte) ((pixel >> 8) & 0xFF);
					dataRGBA[y * w * 4 + x * 4 + 2] = (byte) ((pixel >> 0) & 0xFF);
					int alpha = ((pixel >> 24) & 0xFF);
					dataRGBA[y * w * 4 + x * 4 + 3] = (byte)alpha;
					hasAlpha |= alpha < 255;
				}
			}
			if (doFlip) {
				byte[] flip = new byte[w * h * 4]; //flip for OpenGL and convert to RGBA
				for (int y = 0; y < h; y++) {
					for (int x = 0; x < w; x++) {
						int offset = y * w * 4 + x * 4;
						int inOffset = (h - y - 1) * w * 4 + x * 4;
						System.arraycopy(dataRGBA, inOffset, flip, offset, 4);
					}
				}
				dataRGBA = flip;
			}
		}

		public Texture toTexture() {
			if (hasAlpha) {
				return new Texture(w, h, TextureFormatHandler.RGBA8, dataRGBA);
			} else {
				byte[] rgb = new byte[dataRGBA.length / 4 * 3];

				for (int rgbaOffs = 0, rgbOffs = 0; rgbaOffs < dataRGBA.length; rgbaOffs += 4, rgbOffs += 3) {
					System.arraycopy(dataRGBA, rgbaOffs, rgb, rgbOffs, 3);
				}
				return new Texture(w, h, TextureFormatHandler.RGB8, rgb);
			}
		}
	}
}
