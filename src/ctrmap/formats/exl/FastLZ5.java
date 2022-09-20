/*
  FastLZ - Byte-aligned LZ77 compression library
  Copyright (C) 2005-2020 Ariya Hidayat <ariya.hidayat@gmail.com>

  Permission is hereby granted, free of charge, to any person obtaining a copy
  of this software and associated documentation files (the "Software"), to deal
  in the Software without restriction, including without limitation the rights
  to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
  copies of the Software, and to permit persons to whom the Software is
  furnished to do so, subject to the following conditions:

  The above copyright notice and this permission notice shall be included in
  all copies or substantial portions of the Software.

  THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
  IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
  FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
  AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
  LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
  OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
  THE SOFTWARE.
 */
package ctrmap.formats.exl;

import xstandard.io.util.IOUtils;
import java.util.Arrays;

public class FastLZ5 {

	private static final int MAX_COPY = 32;
	private static final int MAX_LEN = 264;
	/* 256 + 8 */
	private static final int MAX_L1_DISTANCE = 8192;
	private static final int MAX_L2_DISTANCE = 8191;
	private static final int MAX_FARDISTANCE = (65535 + MAX_L2_DISTANCE - 1);

	private static final int HASH_LOG = 14;
	private static final int HASH_SIZE = (1 << HASH_LOG);
	private static final int HASH_MASK = (HASH_SIZE - 1);

	private static int flz_hash(int v) {
		long h = (v * 2654435769L) >>> (32 - HASH_LOG);
		return (int) (h & HASH_MASK);
	}

	private static int flz_cmp(BytePtr p, BytePtr q, BytePtr r) {
		int start = p.offset;
		p = p.clone();
		q = q.clone();
		while (q.offset < r.offset) {
			if (p.getAndIncrement() != q.getAndIncrement()) {
				break;
			}
		}
		return p.offset - start;
	}

	private static int flz_readu32(BytePtr ptr) {
		return IOUtils.byteArrayToIntegerLE(ptr.array, ptr.offset);
	}

	private static void flz_copy8bytes(BytePtr dest, BytePtr src, int count) {
		System.arraycopy(src.array, src.offset, dest.array, dest.offset, count);
	}

	private static void flz_copy32bytes(BytePtr dest, BytePtr src) {
		System.arraycopy(src.array, src.offset, dest.array, dest.offset, 32);
	}

	private static BytePtr flz_literals(int runs, BytePtr src, BytePtr dest) {
		src = src.clone();
		dest = dest.clone();
		while (runs >= MAX_COPY) {
			dest.setAndIncrement(MAX_COPY - 1);
			flz_copy32bytes(dest, src);
			src.offset += MAX_COPY;
			dest.offset += MAX_COPY;
			runs -= MAX_COPY;
		}
		if (runs > 0) {
			dest.setAndIncrement(runs - 1);
			flz_copy8bytes(dest, src, runs);
			dest.offset += runs;
		}
		return dest;
	}

	private static void fastlz_memmove(BytePtr dest, BytePtr src, int count) {
		dest = dest.clone();
		src = src.clone();
		do {
			dest.setAndIncrement(src.getAndIncrement());
		} while (--count != 0);
	}

	private static void fastlz_memcpy(BytePtr dest, BytePtr src, int count) {
		fastlz_memmove(dest, src, count);
	}


	/* special case of memcpy: at most 32 bytes */
	private static void flz_smallcopy(BytePtr dest, BytePtr src, int count) {
		System.arraycopy(src.array, src.offset, dest.array, dest.offset, count);
	}

	private static BytePtr flz_finalize(int runs, BytePtr src, BytePtr dest) {
		src = src.clone();
		dest = dest.clone();
		while (runs >= MAX_COPY) {
			dest.setAndIncrement(MAX_COPY - 1);
			flz_smallcopy(dest, src, MAX_COPY);
			src.offset += MAX_COPY;
			dest.offset += MAX_COPY;
			runs -= MAX_COPY;
		}
		if (runs > 0) {
			dest.setAndIncrement(runs - 1);
			flz_smallcopy(dest, src, runs);
			dest.offset += runs;
		}
		return dest;
	}

	private static BytePtr flz1_match(int len, int distance, BytePtr op) {
		op = op.clone();
		--distance;
		if (len > MAX_LEN - 2) {
			while (len > MAX_LEN - 2) {
				op.setAndIncrement((7 << 5) + (distance >>> 8));
				op.setAndIncrement(MAX_LEN - 2 - 7 - 2);
				op.setAndIncrement(distance & 255);
				len -= MAX_LEN - 2;
			}
		}
		if (len < 7) {
			op.setAndIncrement((len << 5) + (distance >>> 8));
			op.setAndIncrement(distance & 255);
		} else {
			op.setAndIncrement((7 << 5) + (distance >>> 8));
			op.setAndIncrement(len - 7);
			op.setAndIncrement(distance & 255);
		}
		return op;
	}

	private static int fastlz1_compress(byte[] input, int length, byte[] output) {
		BytePtr ip = new BytePtr(input, 0);
		BytePtr ip_start = ip.clone();
		BytePtr ip_bound = ip.clone();
		ip_bound.offset += length - 4;
		/* because readU32 */
		BytePtr ip_limit = ip.clone();
		ip_limit.offset += length - 12 - 1;
		BytePtr op = new BytePtr(output, 0);

		int[] htab = new int[HASH_SIZE];
		int seq, hash;

		/* initializes hash table */
		for (hash = 0; hash < HASH_SIZE; ++hash) {
			htab[hash] = 0;
		}

		/* we start with literal copy */
		BytePtr anchor = ip.clone();
		ip.offset += 2;

		/* main loop */
		while (ip.offset < ip_limit.offset) {
			BytePtr ref;
			int distance, cmp;

			/* find potential match */
			do {
				seq = flz_readu32(ip) & 0xffffff;
				hash = flz_hash(seq);
				ref = ip_start.clone();
				ref.offset += htab[hash];
				htab[hash] = ip.offset - ip_start.offset;
				distance = ip.offset - ref.offset;
				cmp = distance < MAX_L1_DISTANCE ? flz_readu32(ref) & 0xffffff : 0x1000000;
				if (ip.offset >= ip_limit.offset) {
					break;
				}
				ip.offset++;
			} while (seq != cmp);

			if (ip.offset >= ip_limit.offset) {
				break;
			}
			--ip.offset;

			if (ip.offset > anchor.offset) {
				op = flz_literals(ip.offset - anchor.offset, anchor, op);
			}

			int len;
			{
				BytePtr ref3 = ref.clone();
				ref3.offset += 3;
				BytePtr ip3 = ip.clone();
				ip3.offset += 3;

				len = flz_cmp(ref3, ip3, ip_bound);
			}
			op = flz1_match(len, distance, op);

			/* update the hash at match boundary */
			ip.offset += len;
			seq = flz_readu32(ip);
			hash = flz_hash(seq & 0xffffff);
			htab[hash] = ip.offset - ip_start.offset;
			ip.offset++;
			seq >>>= 8;
			hash = flz_hash(seq);
			htab[hash] = ip.offset - ip_start.offset;
			ip.offset++;

			anchor = ip.clone();
		}

		int copy = length - anchor.offset;
		op = flz_finalize(copy, anchor, op);

		return op.offset;
	}

	private static int fastlz1_decompress(byte[] input, int length, byte[] output, int maxout) {
		BytePtr ip = new BytePtr(input, 0);
		BytePtr ip_limit = ip.clone();
		ip_limit.offset += length;
		BytePtr ip_bound = ip_limit.clone();
		ip_bound.offset -= 2;
		BytePtr op = new BytePtr(output, 0);
		BytePtr op_limit = new BytePtr(output, maxout);
		int ctrl = ip.getAndIncrement() & 31;

		while (true) {
			if (ctrl >= 32) {
				int len = (ctrl >>> 5) - 1;
				int ofs = (ctrl & 31) << 8;
				BytePtr ref = op.clone();
				op.offset -= (ofs + 1);
				if (len == 7 - 1) {
					//FASTLZ_BOUND_CHECK(ip <= ip_bound);
					len += ip.getAndIncrement();
				}
				ref.offset -= ip.getAndIncrement();
				len += 3;
				//FASTLZ_BOUND_CHECK(op + len <= op_limit);
				//FASTLZ_BOUND_CHECK(ref >= (uint8_t*)output);
				fastlz_memmove(op, ref, len);
				op.offset += len;
			} else {
				ctrl++;
				//FASTLZ_BOUND_CHECK(op + ctrl <= op_limit);
				//FASTLZ_BOUND_CHECK(ip + ctrl <= ip_limit);
				fastlz_memcpy(op, ip, ctrl);
				ip.offset += ctrl;
				op.offset += ctrl;
			}

			if (ip.offset > ip_bound.offset) {
				break;
			}
			ctrl = ip.getAndIncrement();
		}

		return op.offset;
	}

	private static BytePtr flz2_match(int len, int distance, BytePtr op) {
		--distance;
		if (distance < MAX_L2_DISTANCE) {
			if (len < 7) {
				op.setAndIncrement((len << 5) + (distance >>> 8));
				op.setAndIncrement(distance & 255);
			} else {
				op.setAndIncrement((7 << 5) + (distance >>> 8));
				for (len -= 7; len >= 255; len -= 255) {
					op.setAndIncrement(255);
				}
				op.setAndIncrement(len);
				op.setAndIncrement(distance & 255);
			}
		} else {
			/* far away, but not yet in the another galaxy... */
			if (len < 7) {
				distance -= MAX_L2_DISTANCE;
				op.setAndIncrement((len << 5) + 31);
				op.setAndIncrement(255);
				op.setAndIncrement(distance >>> 8);
				op.setAndIncrement(distance & 255);
			} else {
				distance -= MAX_L2_DISTANCE;
				op.setAndIncrement((7 << 5) + 31);
				for (len -= 7; len >= 255; len -= 255) {
					op.setAndIncrement(255);
				}
				op.setAndIncrement(len);
				op.setAndIncrement(255);
				op.setAndIncrement(distance >>> 8);
				op.setAndIncrement(distance & 255);
			}
		}
		return op;
	}

	private static int fastlz2_compress(byte[] input, int length, byte[] output) {
		BytePtr ip = new BytePtr(input, 0);
		BytePtr ip_start = ip.clone();
		BytePtr ip_bound = ip.clone();
		ip_bound.offset += length - 4;
		/* because readU32 */
		BytePtr ip_limit = ip.clone();
		ip_limit.offset += length - 12 - 1;
		BytePtr op = new BytePtr(output, 0);

		int[] htab = new int[HASH_SIZE];
		int seq, hash;

		/* initializes hash table */
		for (hash = 0; hash < HASH_SIZE; ++hash) {
			htab[hash] = 0;
		}

		/* we start with literal copy */
		BytePtr anchor = ip.clone();
		ip.offset += 2;

		/* main loop */
		while (ip.offset < ip_limit.offset) {
			BytePtr ref;
			int distance, cmp;

			/* find potential match */
			do {
				seq = flz_readu32(ip) & 0xffffff;
				hash = flz_hash(seq);
				ref = ip_start.clone();
				ref.offset += htab[hash];
				htab[hash] = ip.offset - ip_start.offset;
				distance = ip.offset - ref.offset;
				cmp = (distance < MAX_FARDISTANCE) ? flz_readu32(ref) & 0xffffff : 0x1000000;
				if (ip.offset >= ip_limit.offset) {
					break;
				}
				++ip.offset;
			} while (seq != cmp);

			if (ip.offset >= ip_limit.offset) {
				break;
			}

			--ip.offset;

			/* far, needs at least 5-byte match */
			if (distance >= MAX_L2_DISTANCE) {
				if (ref.get(3) != ip.get(3) || ref.get(4) != ip.get(4)) {
					++ip.offset;
					continue;
				}
			}

			if (ip.offset > anchor.offset) {
				op = flz_literals(ip.offset - anchor.offset, anchor, op);
			}

			int len;
			{
				BytePtr ref3 = ref.clone();
				ref3.offset += 3;
				BytePtr ip3 = ip.clone();
				ip3.offset += 3;

				len = flz_cmp(ref3, ip3, ip_bound);
			}
			op = flz2_match(len, distance, op);

			/* update the hash at match boundary */
			ip.offset += len;
			seq = flz_readu32(ip);
			hash = flz_hash(seq & 0xffffff);
			htab[hash] = ip.offset - ip_start.offset;
			ip.offset++;
			seq >>>= 8;
			hash = flz_hash(seq);
			htab[hash] = ip.offset - ip_start.offset;
			ip.offset++;

			anchor = ip.clone();
		}
		int copy = length - anchor.offset;
		op = flz_finalize(copy, anchor, op);


		/* marker for fastlz2 */
		output[0] |= (1 << 5);

		return op.offset;
	}

	private static int fastlz2_decompress(byte[] input, int length, byte[] output, int maxout) {
		BytePtr ip = new BytePtr(input, 0);
		BytePtr ip_limit = ip.clone();
		ip_limit.offset += length;
		BytePtr ip_bound = ip_limit.clone();
		ip_bound.offset -= 2;
		BytePtr op = new BytePtr(output, 0);
		BytePtr op_limit = new BytePtr(output, maxout);
		int ctrl = ip.getAndIncrement() & 31;

		while (true) {
			if (ctrl >= 32) {
				int len = (ctrl >>> 5) - 1;
				int ofs = (ctrl & 31) << 8;
				BytePtr ref = op.clone();
				ref.offset -= (ofs + 1);

				int code;
				if (len == 7 - 1) {
					do {
						// FASTLZ_BOUND_CHECK(ip <= ip_bound);
						code = ip.getAndIncrement();
						len += code;
					} while (code == 255);
				}
				code = ip.getAndIncrement();
				ref.offset -= code;
				len += 3;

				/* match from 16-bit distance */
				if (code == 255) {
					if (ofs == (31 << 8)) {
						//FASTLZ_BOUND_CHECK(ip < ip_bound);
						ofs = ip.getAndIncrement() << 8;
						ofs += ip.getAndIncrement();
						ref = op.clone();
						ref.offset -= (ofs + MAX_L2_DISTANCE + 1);
					}
				}

				//FASTLZ_BOUND_CHECK(op + len <= op_limit);
				//FASTLZ_BOUND_CHECK(ref >= (uint8_t*)output);
				fastlz_memmove(op, ref, len);
				op.offset += len;
			} else {
				ctrl++;
				//FASTLZ_BOUND_CHECK(op + ctrl <= op_limit);
				//FASTLZ_BOUND_CHECK(ip + ctrl <= ip_limit);
				fastlz_memcpy(op, ip, ctrl);
				ip.offset += ctrl;
				op.offset += ctrl;
			}

			if (ip.offset >= ip_limit.offset) {
				break;
			}
			ctrl = ip.getAndIncrement();
		}

		return op.offset;
	}

	public static int calcLength(int length) {
		return (int) Math.max(66, (length * 1.06));
	}

	public static byte[] compress(byte[] input) {
		byte[] output = new byte[calcLength(input.length)];
		int len = fastlz_compress(input, output);
		return Arrays.copyOf(output, len);
	}

	private static int fastlz_compress(byte[] input, byte[] output) {
		/* for short block, choose fastlz1 */
		if (input.length < 65536) {
			return fastlz1_compress(input, input.length, output);
		}

		/*else... */
		return fastlz2_compress(input, input.length, output);
	}

	private static int fastlz_decompress(byte[] input, byte[] output) {
		/* magic identifier for compression level */
		int level = ((input[0] & 0xFF) >> 5) + 1;

		if (level == 1) {
			return fastlz1_decompress(input, input.length, output, output.length);
		}
		if (level == 2) {
			return fastlz2_decompress(input, input.length, output, output.length);
		}

		/* unknown level, trigger error */
		return 0;
	}

	public static byte[] decompress(byte[] buf, int uncompSize) {
		byte[] out = new byte[uncompSize];
		fastlz_decompress(buf, out);
		return out;
	}

	private static int fastlz_compress_level(int level, byte[] input, byte[] output) {
		if (level == 1) {
			return fastlz1_compress(input, input.length, output);
		}
		if (level == 2) {
			return fastlz2_compress(input, input.length, output);
		}

		return 0;
	}

	private static class BytePtr {

		public byte[] array;
		public int offset;

		public BytePtr(byte[] arr, int off) {
			this.array = arr;
			this.offset = off;
		}

		public BytePtr(BytePtr src) {
			array = src.array;
			offset = src.offset;
		}

		public int get(int index) {
			return array[offset + index];
		}

		public int getAndIncrement() {
			int val = array[offset] & 0xFF;
			offset++;
			return val;
		}

		public void setAndIncrement(int value) {
			array[offset] = (byte) value;
			offset++;
		}

		@Override
		public BytePtr clone() {
			return new BytePtr(this);
		}
	}
}
