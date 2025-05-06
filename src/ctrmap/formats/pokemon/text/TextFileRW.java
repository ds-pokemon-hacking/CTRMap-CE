package ctrmap.formats.pokemon.text;

import ctrmap.formats.pokemon.text.crypto.DummyTextCrypto;
import ctrmap.formats.pokemon.text.crypto.MessageTextCrypto;
import xstandard.fs.FSFile;
import xstandard.io.base.impl.ext.data.DataIOStream;
import xstandard.io.structs.TemporaryOffset;
import xstandard.io.structs.TemporaryValueShort;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import xstandard.io.structs.PointerTable;
import xstandard.io.structs.TemporaryValue;

public class TextFileRW {

	public static void readLinesForFile(TextFile target, FSFile fsf, MessageHandler handler) {
		try (GFMessageStream io = new GFMessageStream(fsf.getIO(), handler)) {
			int sectCount = io.readShort();
			int lineCount = io.readUnsignedShort();
			int sectionAllocateSize = io.readInt(); //white2-U 0x020487F0 - probably max. size for preload
			int reserved = io.readInt();

			target.enableEncryption = reserved == 0 || !handler.isMsgDataSupportsNonEncrypted();
			io.setCrypto(target.enableEncryption ? MessageTextCrypto.getInstance() : DummyTextCrypto.getInstance());

			int[] sectionOffsets = new int[sectCount];
			for (int i = 0; i < sectCount; ++i) {
				sectionOffsets[i] = io.readInt();
			}

			for (int s = 0; s < sectCount; ++s) {
				int sectionStart = sectionOffsets[s];
				io.seek(sectionStart);
				int sectionSize = io.readInt();
				int sectionDataStart = io.getPosition();

				io.resetCryptoCounter();

				List<MsgStr> sectionLines = new ArrayList<>();

				for (int l = 0; l < lineCount; l++) {
					io.seek(sectionDataStart + l * 8);
					int offs = io.readInt();
					int charCount = io.readUnsignedShort();
					int extra = io.readUnsignedShort();
					io.seek(offs + sectionStart);
					sectionLines.add(io.readString(charCount));
				}

				target.sections.add(new TextFileSection(target, sectionLines));
			}
		} catch (IOException ex) {
			Logger.getLogger(TextFileRW.class.getName()).log(Level.SEVERE, null, ex);
		}
	}

	public static byte[] getBytesForFile(TextFile file, MessageHandler handler) {
		try (DataIOStream ba = new DataIOStream()) {
			GFMessageStream io = new GFMessageStream(ba, handler);
			boolean isEncrypted = file.enableEncryption || handler.isMsgDataSupportsNonEncrypted();
			io.setCrypto(isEncrypted ? MessageTextCrypto.getInstance() : DummyTextCrypto.getInstance());

			int lineCount = file.mainSection != null ? file.mainSection.getLineCount() : 0;
			for (TextFileSection section : file.sections) {
				lineCount = Math.min(section.getLineCount(), lineCount);
			}

			io.writeShort(file.sections.size());
			io.writeShort((short) lineCount);
			TemporaryValue dataAllocSize = new TemporaryValue(io);
			io.writeInt(isEncrypted ? 0 : 1);

			List<TemporaryOffset> sectionOffsets = PointerTable.allocatePointerTable(file.sections.size(), ba, 0, false);
			int maxSectionSize = 0;

			for (int s = 0; s < file.sections.size(); ++s) {
				io.pad(4); //align to sizeof(uint32) for section size field
				sectionOffsets.get(s).setHere();
				
				TextFileSection section = file.sections.get(s);
				
				int sectionTop = io.getPosition();

				TemporaryValue sectionSizeField = new TemporaryValue(io);

				List<TemporaryOffset> lineOffsets = new ArrayList<>();
				List<TemporaryValueShort> lineLengths = new ArrayList<>();
				for (int i = 0; i < lineCount; ++i) {
					lineOffsets.add(new TemporaryOffset(io, -sectionTop));
					lineLengths.add(new TemporaryValueShort(io));
					io.writeShort(0); //alignment
				}
				
				io.resetCryptoCounter();

				for (int i = 0; i < lineCount; i++) {
					lineOffsets.get(i).setHere();
					int lineBegin = io.getPosition();

					io.writeString(section.lines.get(i));
					if (handler.isMsgDataPadded()) {
						io.pad(4);
					}

					int lineEnd = io.getPosition();
					int lineLengthInChars = (lineEnd - lineBegin) / 2;
					lineLengths.get(i).set(lineLengthInChars);
				}
				
				int sectionSize = io.getPosition() - sectionTop;
				sectionSizeField.set(sectionSize);
				maxSectionSize = Math.max(maxSectionSize, sectionSize);
			}

			dataAllocSize.set(maxSectionSize);

			return ba.toByteArray();
		} catch (IOException ex) {
			Logger.getLogger(TextFileRW.class.getName()).log(Level.SEVERE, null, ex);
		}
		return null;
	}
}
