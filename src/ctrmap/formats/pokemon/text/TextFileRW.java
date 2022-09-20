package ctrmap.formats.pokemon.text;

import ctrmap.formats.pokemon.text.crypto.DummyTextCrypto;
import ctrmap.formats.pokemon.text.crypto.MessageTextCrypto;
import xstandard.fs.FSFile;
import xstandard.io.base.impl.ext.data.DataIOStream;
import xstandard.io.structs.TemporaryOffset;
import xstandard.io.structs.TemporaryValue;
import xstandard.io.structs.TemporaryValueShort;
import xstandard.util.ListenableList;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class TextFileRW {

	public static void readLinesForFile(TextFile target, FSFile fsf, MessageHandler handler) {
		try {
			ListenableList<MsgStr> lines = new ListenableList<>();

			GFMessageStream io = new GFMessageStream(fsf.getIO(), handler);
			int sectCount = io.readShort();

			if (sectCount > 1) {
				throw new UnsupportedOperationException("More than 1 sections are not supported");
			}
			if (sectCount == 1) {
				int lineCount = io.readUnsignedShort();
				io.skipBytes(4);
				target.enableEncryption = io.readInt() == 0 || !handler.isMsgDataSupportsNonEncrypted();
				io.setCrypto(target.enableEncryption ? MessageTextCrypto.getInstance() : DummyTextCrypto.getInstance());
				int sectionStart = io.readInt();

				for (int l = 0; l < lineCount; l++) {
					io.seek(sectionStart + 4 + l * 8);
					int offs = io.readInt();
					int charCount = io.readUnsignedShort();
					int extra = io.readUnsignedShort();
					io.seek(offs + sectionStart);
					lines.add(io.readString(charCount));
				}
			}
			io.close();

			target.lines = lines;
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
