package nl.weeaboo.dt.io;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import nl.weeaboo.common.io.FileUtil;
import nl.weeaboo.dt.Game;

public class HashUtil {

	private static final ByteBuffer temp = ByteBuffer.allocate(16 << 10);
		
	public static byte[] calculateResourcesHash(Game game) throws IOException {
		MessageDigest md;
		try {
			md = MessageDigest.getInstance("SHA-1");
		} catch (NoSuchAlgorithmException e) {
			throw new IOException(e);
		}
	
		md.reset();
		Set<String> textFileExts = new HashSet<String>(Arrays.asList(
				"lua", "xml", "ini", "txt"));
		
		Set<String> files = game.getFolderContents("/");
		for (String filename : files) {
			String fext = FileUtil.getExtension(filename).toLowerCase();			
			if (textFileExts.contains(fext)) {
				hashContents(game, filename, md);
			} else {
				hashFast(game, filename, md);
			}
		}
		
		return md.digest();
	}
	
	public static void hashContents(Game game, String filename, MessageDigest md)
		throws IOException
	{
		InputStream in = null;
		try {
			hash(md, game.getInputStream(filename));
		} finally {
			if (in != null) in.close();
		}
	}
	
	public static void hashFast(Game game, String filename, MessageDigest md)
		throws IOException
	{
		byte filenameBytes[] = filename.getBytes("UTF-8");
		long size = game.getFileSize(filename);
				
		temp.put(filenameBytes, 0, Math.min(temp.capacity()-8, filenameBytes.length));
		temp.putLong(size);
		temp.limit(temp.position());
		temp.rewind();
		
		md.update(temp);
		temp.rewind();
		temp.limit(temp.capacity());
	}
	
	public static void hash(MessageDigest md, InputStream in) throws IOException {
		int read = 0;		
		while (true) {
			int r = in.read(temp.array(), 0, temp.capacity());
			if (r == -1) {
				break;
			}
			md.update(temp.array(), 0, r);
			read += r;
		}
	}
	
}
