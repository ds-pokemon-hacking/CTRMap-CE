package ctrmap.formats.pokemon.containers;

import ctrmap.formats.pokemon.containers.util.ContainerFile;
import ctrmap.formats.pokemon.containers.util.ContentType;
import xstandard.fs.FSFile;
import xstandard.fs.FSUtil;
import xstandard.fs.TempFileAccessor;
import xstandard.fs.accessors.DiskFile;
import xstandard.fs.accessors.MemoryFile;
import xstandard.io.base.iface.DataInputEx;
import xstandard.io.base.iface.DataOutputEx;
import xstandard.io.base.impl.ext.data.DataIOStream;
import xstandard.io.structs.PointerTable;
import xstandard.io.structs.TemporaryOffset;
import xstandard.io.util.StringIO;
import xstandard.math.MathEx;
import java.io.DataInput;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

public abstract class GFContainer {

	public static final int GF_CONTAINER_PADDING = 0x80;

	protected FSFile source;

	private boolean enableMemoryHandle = false;
	private DataIOStream memoryHandle;

	public GFContainer(FSFile fsf) {
		source = fsf;
	}

	public GFContainer(byte[] b) {
		this(new MemoryFile("dummy", b));
	}

	public GFContainer(int initFileCount) {
		this(new MemoryFile("dummy", new byte[0]), initFileCount);
	}

	public GFContainer(FSFile fsf, int initFileCount) {
		source = fsf;
		initializeContainer(initFileCount);
	}

	public void makeMemoryHandle() {
		if (!enableMemoryHandle) {
			enableMemoryHandle = true;
			memoryHandle = new DataIOStream(source.getBytes());
		}
	}

	public void flushMemoryHandle() {
		source.setBytes(memoryHandle.toByteArray());
	}

	public void deleteMemoryHandle() {
		enableMemoryHandle = false;
		memoryHandle = null;
	}

	public ContainerFile getFSFile(int fileNum) {
		return new ContainerFile(this, fileNum);
	}

	public abstract String getSignature();

	public abstract ContentType getDefaultContentType(int index);

	public abstract boolean getIsPadded();

	public static boolean isValidContainerMagic(int character) {
		return character >= 'A' && character <= 'Z' || Character.isDigit(character); //digits are valid, but lower case letters are never used iirc
	}

	public static boolean isContainer(byte[] data) {
		return isContainer(new DataIOStream(data));
	}

	public static boolean isContainer(DataInputEx io) {
		boolean r = false;
		try {
			int len = io.getLength();

			if (len >= 4) {
				if (isValidContainerMagic(io.readUnsignedByte()) && isValidContainerMagic(io.readUnsignedByte())) {
					int fc = io.readUnsignedShort();
					if (len > io.getPosition() + fc * 4) {
						boolean offsetsValid = true;
						for (int i = 0; i < fc; i++) {
							int offs = io.readInt();
							if (offs < 0 || offs > len) {
								offsetsValid = false;
								break;
							}
						}
						r = offsetsValid;
					}
				}
			}

			io.close();
		} catch (IOException ex) {
			Logger.getLogger(GFContainer.class.getName()).log(Level.SEVERE, null, ex);
		}
		return r;
	}

	protected void initializeContainer(int fcount) {
		//This method is used for better performance compared to a sequential storeFile call as that one would reopen and close lot of streams
		try {
			boolean pad = getIsPadded();
			
			DataOutputEx dos = getOutputStream();

			dos.writeStringUnterminated(getSignature());
			dos.writeShort(fcount);
			int baseOffs = 4 + (fcount + 1) * 4;
			if (pad) {
				baseOffs = MathEx.padInteger(baseOffs, GF_CONTAINER_PADDING);
			}
			for (int i = 0; i < fcount + 1; i++) {
				dos.writeInt(baseOffs);
			}

			dos.close();
		} catch (IOException ex) {
			Logger.getLogger(GFContainer.class.getName()).log(Level.SEVERE, null, ex);
		}
	}

	public synchronized int getFileCount() {
		if (source.length() < 4) {
			return 0;
		}
		try {
			//fast mode
			DataInputEx dis = getInputStream();

			dis.skipBytes(2);
			int length = dis.readUnsignedShort();

			dis.close();
			return length;
		} catch (IOException ex) {
			Logger.getLogger(GFContainer.class.getName()).log(Level.SEVERE, null, ex);
		}
		return 0;
	}

	public int getFileSize(int num) {
		try {
			//fast mode
			DataInputEx dis = getInputStream();

			dis.skipBytes(4 + num * 4);
			int offs1 = dis.readInt();
			int offs2 = dis.readInt();

			dis.close();
			return offs2 - offs1;
		} catch (IOException ex) {
			Logger.getLogger(GFContainer.class.getName()).log(Level.SEVERE, null, ex);
		}
		return 0;
	}

	public void setFileCount(int count) {
		int fc = getFileCount();
		if (count > fc) {
			storeFile(count - 1, new byte[0]);
		} else if (count < fc) {
			try {
				boolean pad = getIsPadded();
				//fast mode
				DataInputEx dis = getInputStream();
				
				GFContainerHeader header = new GFContainerHeader(dis);
				
				int endOffset = header.fileOffsets[count];
				int startOffset = header.fileOffsets[0];
				
				byte[] copyData = new byte[endOffset - startOffset];
				dis.seekNext(startOffset);
				dis.read(copyData);
				
				dis.close();
				
				DataOutputEx out = getOutputStream();
				
				out.writeStringUnterminated(getSignature());
				out.writeShort(count);
				
				int newContentStartOffset = 4 + (count + 1) * Integer.BYTES;
				if (pad) {
					newContentStartOffset = MathEx.padInteger(newContentStartOffset, GF_CONTAINER_PADDING);
				}
				
				for (int i = 0; i < count; i++) {
					out.writeInt(header.fileOffsets[i] - startOffset + newContentStartOffset);
				}
				out.writeInt(newContentStartOffset + copyData.length);
				out.writePadding(newContentStartOffset - out.getPosition(), 0);
				
				out.write(copyData);
				
				out.close();
			} catch (IOException ex) {
				Logger.getLogger(GFContainer.class.getName()).log(Level.SEVERE, null, ex);
			}
		}
	}

	private DataInputEx getInputStream() {
		if (enableMemoryHandle) {
			try {
				memoryHandle.seek(0);
				return memoryHandle;
			} catch (IOException ex) {
				Logger.getLogger(GFContainer.class.getName()).log(Level.SEVERE, null, ex);
			}
		}
		if (!source.exists()) {
			return null;
		}
		return source.getDataInputStream();
	}
	
	private DataOutputEx getOutputStream() {
		if (enableMemoryHandle) {
			try {
				memoryHandle.seek(0);
				return memoryHandle;
			} catch (IOException ex) {
				Logger.getLogger(GFContainer.class.getName()).log(Level.SEVERE, null, ex);
			}
		}
		if (!source.exists()) {
			return null;
		}
		return source.getDataOutputStream();
	}

	protected GFContainerHeader getContHeader() {
		try {
			DataInputEx dis = getInputStream();
			GFContainerHeader header = new GFContainerHeader(dis);
			if (!enableMemoryHandle) {
				dis.close();
			}
			return header;
		} catch (IOException ex) {
			Logger.getLogger(GFContainer.class.getName()).log(Level.SEVERE, null, ex);
		}
		return null;
	}

	public File getIOFile(int num) {
		byte[] b = getFile(num);
		File out = TempFileAccessor.createTempFile("/agfc_extract_" + UUID.randomUUID().toString());
		FSUtil.writeBytesToFile(out, b);
		return out;
	}

	public synchronized byte[] getFile(int num) {
		//System.out.println("Container " + source + " begin get file " + num);
		byte[] data = null;
		try {
			DataInputEx dis = getInputStream();
			GFContainerHeader header = new GFContainerHeader(dis);

			if (num < header.fileOffsets.length - 1) {
				//System.out.println("i wanna be at " + header.fileOffsets[num] + ", so i'll skip " + (header.fileOffsets[num] - dis.getPosition()));
				dis.skipBytes(header.fileOffsets[num] - dis.getPosition());
				//System.out.println("i am at " + dis.getPosition() + " of " + source);
				int len = header.fileOffsets[num + 1] - header.fileOffsets[num];
				if (header.fileOffsets[num] < 0) {
					throw new ArrayIndexOutOfBoundsException("Offset is negative (file " + num + ")");
				}
				if (len < 0) {
					throw new ArrayIndexOutOfBoundsException("Length is negative (file " + num + ")");
				}
				data = new byte[len];
				if (len > 0) {
					dis.readFully(data);
				}
			} else {
				throw new IOException("File index " + num + " of container" + source + " exceeds container capacity " + (header.fileOffsets.length - 1) + ".");
			}

			if (!enableMemoryHandle) {
				dis.close();
			}
		} catch (IOException ex) {
			Logger.getLogger(GFContainer.class.getName()).log(Level.SEVERE, null, ex);
		}
		//System.out.println("Container " + source + " end get file " + num);
		return data;
	}

	public void storeFile(int num, File f) {
		storeFile(num, FSUtil.readFileToBytes(f));
	}

	public void storeFile(int num, FSFile f) {
		storeFile(num, f.getBytes());
	}

	public synchronized void storeFile(int fileIndex, byte[] data) {
		try {
			boolean pad = getIsPadded();
			
			DataInputEx dis = getInputStream();
			DataIOStream out = new DataIOStream();

			GFContainerHeader header = new GFContainerHeader(dis);
			int offcount = Math.max(fileIndex + 1 + 1, header.fileOffsets.length);
			int haveFcount = header.fileOffsets.length - 1;
			int fcount = offcount - 1;

			out.writeStringUnterminated(getSignature());
			out.writeShort(fcount);
			List<TemporaryOffset> dataOffsets = PointerTable.allocatePointerTable(offcount, out, 0, false);
			if (pad) {
				out.pad(GF_CONTAINER_PADDING);
			}

			int contentStart = out.getPosition();

			byte[] dataBefore = new byte[0];

			int firstChangedOffset = Math.min(fileIndex, header.fileOffsets.length - 1);
			if (header.fileOffsets.length > 0) {
				int dataBeforeEnd = header.fileOffsets[firstChangedOffset];
				int dataBeforeStart = header.fileOffsets[0];
				if (dataBeforeEnd > dataBeforeStart) {
					dis.skipBytes(dataBeforeStart - dis.getPosition());
					dataBefore = new byte[dataBeforeEnd - dataBeforeStart];
					//System.out.println("db len " + dataBefore.length + " start " + dataBeforeStart + " end " + dataBeforeEnd);
					dis.readFully(dataBefore);
				}

				int newInitOffset = contentStart;
				int reloc = newInitOffset - dataBeforeStart;
				for (int i = 0; i < firstChangedOffset; i++) {
					dataOffsets.get(i).set(header.fileOffsets[i] + reloc);
				}
			}

			if (dataBefore.length > 0) {
				out.write(dataBefore);
			}
			for (int supplementaryDummyOffset = firstChangedOffset; supplementaryDummyOffset < fileIndex; supplementaryDummyOffset++) {
				dataOffsets.get(supplementaryDummyOffset).setHere();
			}
			dataOffsets.get(fileIndex).setHere();

			out.write(data);
			if (pad) {
				out.pad(GF_CONTAINER_PADDING);
			}

			byte[] dataAfter = new byte[0];

			if (fileIndex < haveFcount) {
				int dataAfterStart = header.fileOffsets[fileIndex + 1];
				int dataAfterEnd = header.fileOffsets[header.fileOffsets.length - 1];
				//System.out.println(dataAfterStart + "/" + dataAfterEnd);
				if (dataAfterEnd > dataAfterStart) {
					//System.out.println("bstart " + dis.getPosition());
					dis.skipBytes(dataAfterStart - dis.getPosition());
					dataAfter = new byte[dataAfterEnd - dataAfterStart];
					//System.out.println("start " + dis.getPosition());
					dis.readFully(dataAfter);
				}
				int newAfterInitOffset = out.getPosition();
				int reloc = newAfterInitOffset - dataAfterStart;
				for (int i = fileIndex + 1; i < offcount; i++) {
					dataOffsets.get(i).set(header.fileOffsets[i] + reloc);
				}
			}

			if (dataAfter.length > 0) {
				out.write(dataAfter);
			}

			dataOffsets.get(fcount).setHere();
			out.close();

			dis.close();

			if (enableMemoryHandle) {
				memoryHandle = out;
			} else {
				source.setBytes(out.toByteArray());
			}
		} catch (IOException ex) {
			Logger.getLogger(GFContainer.class.getName()).log(Level.SEVERE, null, ex);
		}
	}

	public FSFile getOriginFile() {
		return source;
	}

	public void relocateToNewFile(File target) {
		relocateToNewFile(new DiskFile(target));
	}

	public void relocateToNewFile(FSFile target) {
		if (source != target) {
			if (enableMemoryHandle) {
				target.setBytes(memoryHandle.toByteArray());
			} else {
				FSUtil.copy(source, target);
			}
			source = target;
		}
	}

	public static class GFContainerHeader {

		public String magic;
		public int[] fileOffsets;

		public GFContainerHeader(DataInput in) throws IOException {
			if (in == null) {
				fileOffsets = new int[0];
			} else {
				magic = StringIO.readPaddedString(in, 2);
				fileOffsets = new int[in.readUnsignedShort() + 1];
				for (int i = 0; i < fileOffsets.length; i++) {
					fileOffsets[i] = in.readInt();
				}
			}
		}
	}
}
