package ctrmap.formats.pokemon.text;

import ctrmap.formats.pokemon.text.crypto.DummyTextCrypto;
import ctrmap.formats.pokemon.text.crypto.MessageTextCrypto;
import xstandard.fs.FSFile;
import xstandard.io.base.impl.ext.data.DataIOStream;
import xstandard.io.structs.TemporaryOffset;
import xstandard.io.structs.TemporaryValueShort;
import xstandard.util.ListenableList;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

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
				
				for (int l = 0; l < lineCount; l++) {
					io.seek(sectionDataStart + l * 8);
					int offs = io.readInt();
					int charCount = io.readUnsignedShort();
					int extra = io.readUnsignedShort();
					io.seek(offs + sectionStart);
					target.lines.add(io.readString(charCount));
				}
			}
		} catch (IOException ex) {
			Logger.getLogger(TextFileRW.class.getName()).log(Level.SEVERE, null, ex);
		}
	}

	public static byte[] getBytesForFile(TextFile file, MessageHandler handler) {
		try {
			List<MsgStr> lines = file.lines;
			DataIOStream ba = new DataIOStream();
			GFMessageStream io = new GFMessageStream(ba, handler);
			boolean isEncrypted = file.enableEncryption || handler.isMsgDataSupportsNonEncrypted();
			io.setCrypto(isEncrypted ? MessageTextCrypto.getInstance() : DummyTextCrypto.getInstance());

			io.writeShort(1);
			io.writeShort((short) lines.size());
			TemporaryOffset dataBottom = new TemporaryOffset(io, -0x10);
			io.writeInt(0);
			TemporaryOffset sectionTopOffs = new TemporaryOffset(io);
			sectionTopOffs.setHere();

			int sectionTop = io.getPosition();

			TemporaryOffset sectionSize = new TemporaryOffset(io, -io.getPosition());

			List<TemporaryOffset> lineOffsets = new ArrayList<>();
			List<TemporaryValueShort> lineLengths = new ArrayList<>();
			for (MsgStr l : lines) {
				lineOffsets.add(new TemporaryOffset(io, -sectionTop));
				lineLengths.add(new TemporaryValueShort(io));
				io.writeShort(0); //some unused values
			}

			for (int i = 0; i < lines.size(); i++) {
				lineOffsets.get(i).setHere();
				int lineBegin = io.getPosition();

				io.writeString(lines.get(i));
				if (handler.isMsgDataPadded()) {
					io.pad(4);
				}

				int lineEnd = io.getPosition();
				int lineLengthInChars = (lineEnd - lineBegin) / 2;
				lineLengths.get(i).set(lineLengthInChars);
			}

			dataBottom.setHere();
			sectionSize.setHere();

			io.close();
			return ba.toByteArray();
		} catch (IOException ex) {
			Logger.getLogger(TextFileRW.class.getName()).log(Level.SEVERE, null, ex);
		}
		return null;
	}
}
