package ctrmap.renderer.util.texture.compressors;

import xstandard.io.util.IOUtils;
import static ctrmap.renderer.util.texture.TextureCodec.*;
import xstandard.io.util.BitConverter;

/**
 * Ericsson EAC texture decoding, ported from Detex.
 * 
 * https://github.com/hglm/detex
 * 
 * Copyright (c) 2015 Harm Hanemaaijer <fgenfb@yahoo.com>
 * Permission to use, copy, modify, and/or distribute this software for any
 * purpose with or without fee is hereby granted, provided that the above
 * copyright notice and this permission notice appear in all copies.
 * THE SOFTWARE IS PROVIDED "AS IS" AND THE AUTHOR DISCLAIMS ALL WARRANTIES
 * WITH REGARD TO THIS SOFTWARE INCLUDING ALL IMPLIED WARRANTIES OF
 * MERCHANTABILITY AND FITNESS. IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR
 * ANY SPECIAL, DIRECT, INDIRECT, OR CONSEQUENTIAL DAMAGES OR ANY DAMAGES
 * WHATSOEVER RESULTING FROM LOSS OF USE, DATA OR PROFITS, WHETHER IN AN
 * ACTION OF CONTRACT, NEGLIGENCE OR OTHER TORTIOUS ACTION, ARISING OUT OF
 * OR IN CONNECTION WITH THE USE OR PERFORMANCE OF THIS SOFTWARE. 
 */
public class EAC {

	private static final int[][] EAC_MODIFIER_TABLE = new int[][]{
		{-3, -6, -9, -15, 2, 5, 8, 14},
		{-3, -7, -10, -13, 2, 6, 9, 12},
		{-2, -5, -8, -13, 1, 4, 7, 12},
		{-2, -4, -6, -13, 1, 3, 5, 12},
		{-3, -6, -8, -12, 2, 5, 7, 11},
		{-3, -7, -9, -11, 2, 6, 8, 10},
		{-4, -7, -8, -11, 3, 6, 7, 10},
		{-3, -5, -8, -11, 2, 4, 7, 10},
		{-2, -6, -8, -10, 1, 5, 7, 9},
		{-2, -5, -8, -10, 1, 4, 7, 9},
		{-2, -4, -8, -10, 1, 3, 7, 9},
		{-2, -5, -7, -10, 1, 4, 6, 9},
		{-3, -4, -7, -10, 2, 3, 6, 9},
		{-1, -2, -3, -10, 0, 1, 2, 9},
		{-4, -6, -8, -9, 3, 5, 7, 8},
		{-3, -5, -7, -9, 2, 4, 6, 8}
	};
	
	private static void decodeAlphaPixelEAC(int i, long pixels, int[] modifier_table, int base_codeword, int multiplier, byte[] pixel_buffer, int pbuf_offs) {
		int modifier = modifier_table[(int) ((pixels >> (45 - i * 3)) & 7)];
		pixel_buffer[pbuf_offs + ((i & 3) * 4 + ((i & 12) >> 2)) * 4 + 3]
			= (byte) saturate(base_codeword + modifier * multiplier);
	}

	/**
	 * Decompress a 128-bit 4x4 pixel texture block compressed using the ETC2_EAC format.
	 *
	 * @param bitstring Byte array containing the texture block.
	 * @param bitstring_offs Offset of the texture block in the array.
	 * @param mode_mask Mask of enabled decoding modes.
	 * @param flags Detex decompression cap flags.
	 * @param pixel_buffer Output RGBA buffer.
	 * @param pbuf_offs Offset of the resulting block in the RGBA buffer.
	 * @return True if the entire block was successfully decoded.
	 */
	public static boolean detexDecompressBlockETC2_EAC(byte[] bitstring, int bitstring_offs, int mode_mask, int flags, byte[] pixel_buffer, int pbuf_offs) {
		boolean r = ETC2.detexDecompressBlockETC2(bitstring, bitstring_offs + 8, mode_mask, flags, pixel_buffer, pbuf_offs, true);
		if (!r) {
			return false;
		}
		// Decode the alpha part.
		int base_codeword = (bitstring[bitstring_offs + 0] & 0xFF);
		int[] modifier_table = EAC_MODIFIER_TABLE[(bitstring[bitstring_offs + 1] & 0x0F)];
		int multiplier = (bitstring[bitstring_offs + 1] & 0xF0) >> 4;
		if (multiplier == 0 && (flags & ETC2.DETEX_DECOMPRESS_FLAG_ENCODE) != 0) // Not allowed in encoding. Decoder should handle it.
		{
			return false;
		}
		long pixels = ((long) (bitstring[bitstring_offs + 2] & 0xFF) << 40) | ((long) (bitstring[bitstring_offs + 3] & 0xFF) << 32)
			| ((long) (bitstring[bitstring_offs + 4] & 0xFF) << 24)
			| ((long) (bitstring[bitstring_offs + 5] & 0xFF) << 16) | ((long) (bitstring[bitstring_offs + 6] & 0xFF) << 8) | (long) (bitstring[bitstring_offs + 7] & 0xFF);
		decodeAlphaPixelEAC(0, pixels, modifier_table, base_codeword, multiplier, pixel_buffer, pbuf_offs);
		decodeAlphaPixelEAC(1, pixels, modifier_table, base_codeword, multiplier, pixel_buffer, pbuf_offs);
		decodeAlphaPixelEAC(2, pixels, modifier_table, base_codeword, multiplier, pixel_buffer, pbuf_offs);
		decodeAlphaPixelEAC(3, pixels, modifier_table, base_codeword, multiplier, pixel_buffer, pbuf_offs);
		decodeAlphaPixelEAC(4, pixels, modifier_table, base_codeword, multiplier, pixel_buffer, pbuf_offs);
		decodeAlphaPixelEAC(5, pixels, modifier_table, base_codeword, multiplier, pixel_buffer, pbuf_offs);
		decodeAlphaPixelEAC(6, pixels, modifier_table, base_codeword, multiplier, pixel_buffer, pbuf_offs);
		decodeAlphaPixelEAC(7, pixels, modifier_table, base_codeword, multiplier, pixel_buffer, pbuf_offs);
		decodeAlphaPixelEAC(8, pixels, modifier_table, base_codeword, multiplier, pixel_buffer, pbuf_offs);
		decodeAlphaPixelEAC(9, pixels, modifier_table, base_codeword, multiplier, pixel_buffer, pbuf_offs);
		decodeAlphaPixelEAC(10, pixels, modifier_table, base_codeword, multiplier, pixel_buffer, pbuf_offs);
		decodeAlphaPixelEAC(11, pixels, modifier_table, base_codeword, multiplier, pixel_buffer, pbuf_offs);
		decodeAlphaPixelEAC(12, pixels, modifier_table, base_codeword, multiplier, pixel_buffer, pbuf_offs);
		decodeAlphaPixelEAC(13, pixels, modifier_table, base_codeword, multiplier, pixel_buffer, pbuf_offs);
		decodeAlphaPixelEAC(14, pixels, modifier_table, base_codeword, multiplier, pixel_buffer, pbuf_offs);
		decodeAlphaPixelEAC(15, pixels, modifier_table, base_codeword, multiplier, pixel_buffer, pbuf_offs);
		return true;
	}

	private static int clamp0To2047(int x) {
		if (x < 0) {
			return 0;
		}
		if (x > 2047) {
			return 2047;
		}
		return x;
	}

// For each pixel, decode an 11-bit integer and store as follows:
// If shift and offset are zero, store each value in consecutive 16 bit values in pixel_buffer.
// If shift is one, store each value in consecutive 32-bit words in pixel_buffer; if offset
// is zero, store it in the first 16 bits, if offset is one store it in the last 16 bits of each
// 32-bit word.
	private static void decodeBlockEAC11Bit(long qword, int shift, int offset, byte[] pixel_buffer, int pbuf_offs) {
		int base_codeword_times_8_plus_4 = (int) (((qword & 0xFF00000000000000L) >> (56 - 3)) | 0x4);
		int modifier_index = (int) ((qword & 0x000F000000000000L) >> 48);
		int[] modifier_table = EAC_MODIFIER_TABLE[modifier_index];
		int multiplier_times_8 = (int) ((qword & 0x00F0000000000000L) >> (52 - 3));
		if (multiplier_times_8 == 0) {
			multiplier_times_8 = 1;
		}

		for (int i = 0; i < 16; i++) {
			int pixel_index = (int) ((qword & (0x0000E00000000000L >> (i * 3))) >> (45 - i * 3));
			int modifier = modifier_table[pixel_index];
			int value = clamp0To2047(base_codeword_times_8_plus_4
				+ modifier * multiplier_times_8);

			int off = pbuf_offs + 2 * ((((i & 3) * 4 + ((i & 12) >> 2)) << shift) + offset);
			int bits = (value << 5) | (value >> 6);

			pixel_buffer[off] = (byte) (bits & 0xFF);
			pixel_buffer[off + 1] = (byte) ((bits >> 8) & 0xFF);
		}
	}

	/* Decompress a 64-bit 4x4 pixel texture block compressed using the */
 /* EAC_R11 format. */
	public static boolean detexDecompressBlockEAC_R11(byte[] bitstring, int bitstring_offs, int mode_mask,
		int flags, byte[] pixel_buffer, int pbuf_offs) {
		long qword = BitConverter.toInt64LE(bitstring, bitstring_offs);
		decodeBlockEAC11Bit(qword, 0, 0, pixel_buffer, pbuf_offs);
		return true;
	}

	/* Decompress a 128-bit 4x4 pixel texture block compressed using the */
 /* EAC_RG11 format. */
	boolean detexDecompressBlockEAC_RG11(byte[] bitstring, int bitstring_offs, int mode_mask,
		int flags, byte[] pixel_buffer, int pbuf_offs) {
		long red_qword = BitConverter.toInt64BE(bitstring, bitstring_offs);
		decodeBlockEAC11Bit(red_qword, 1, 0, pixel_buffer, pbuf_offs);
		long green_qword = BitConverter.toInt64BE(bitstring, bitstring_offs + 8);
		decodeBlockEAC11Bit(green_qword, 1, 1, pixel_buffer, pbuf_offs);
		return true;
	}
	
	public static int replicateSigned11BitsTo16Bits(int value) {
		if (value >= 0) {
			return (value << 5) | (value >> 5);
		}
		value = -value;
		value = (value << 5) | (value >> 5);
		return -value;
	}

// For each pixel, decode an 11-bit signed integer and store as follows:
// If shift and offset are zero, store each value in consecutive 16 bit values in pixel_buffer.
// If shift is one, store each value in consecutive 32-bit words in pixel_buffer; if offset
// is zero, store it in the first 16 bits, if offset is one store it in the last 16 bits of each
// 32-bit word.
	private static boolean decodeBlockEACSigned11Bit(long qword, int shift, int offset, byte[] pixel_buffer, int pbuf_offs) {
		byte base_codeword = (byte) ((qword & 0xFF00000000000000L) >> 56);	// Signed 8 bits.
		if (base_codeword == - 128) // Not allowed in encoding. Decoder should handle it but we don't do that yet.
		{
			return false;
		}
		int base_codeword_times_8 = base_codeword << 3;				// Arithmetic shift.
		int modifier_index = (int) ((qword & 0x000F000000000000L) >> 48);
		int[] modifier_table = EAC_MODIFIER_TABLE[modifier_index];
		int multiplier_times_8 = (int) ((qword & 0x00F0000000000000L) >> (52 - 3));
		if (multiplier_times_8 == 0) {
			multiplier_times_8 = 1;
		}

		for (int i = 0; i < 16; i++) {
			int pixel_index = (int) ((qword & (0x0000E00000000000L >> (i * 3))) >> (45 - i * 3));
			int modifier = modifier_table[pixel_index];
			int value = clamp1023_signed(base_codeword_times_8 + modifier * multiplier_times_8);
			int bits = replicateSigned11BitsTo16Bits(value);

			int off = pbuf_offs + 2 * ((((i & 3) * 4 + ((i & 12) >> 2)) << shift) + offset);

			pixel_buffer[off + 0] = (byte) (bits & 0xFF);
			pixel_buffer[off + 1] = (byte) ((bits >> 8) & 0xFF);
		}
		return true;
	}

	/* Decompress a 64-bit 4x4 pixel texture block compressed using the */
 /* EAC_SIGNED_R11 format. */
	public static boolean detexDecompressBlockEAC_SIGNED_R11(byte[] bitstring, int bitstring_offs,
		int mode_mask, int flags, byte[] pixel_buffer, int pbuf_offs) {
		long qword = BitConverter.toInt64BE(bitstring, bitstring_offs);
		return decodeBlockEACSigned11Bit(qword, 0, 0, pixel_buffer, pbuf_offs);
	}

	/* Decompress a 128-bit 4x4 pixel texture block compressed using the */
 /* EAC_SIGNED_RG11 format. */
	public static boolean detexDecompressBlockEAC_SIGNED_RG11(byte[] bitstring, int bitstring_offs,
		int mode_mask, int flags, byte[] pixel_buffer, int pbuf_offs) {
		long red_qword = BitConverter.toInt64BE(bitstring, bitstring_offs);

		boolean r = decodeBlockEACSigned11Bit(red_qword, 1, 0, pixel_buffer, pbuf_offs);
		if (!r) {
			return false;
		}
		long green_qword = BitConverter.toInt64BE(bitstring, bitstring_offs + 8);
		return decodeBlockEACSigned11Bit(green_qword, 1, 1, pixel_buffer, pbuf_offs);
	}
}
