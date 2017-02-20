package omrecorder.widget;

import android.os.Environment;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.RandomAccessFile;

/**
 * Created by xieqi on 2017/2/20.
 */

public class FileUtil {
	private static String path = Environment.getExternalStorageDirectory()
			+ "/"
			+ Environment.DIRECTORY_MUSIC
			+ "/audio_processed.wav";

	private static File file = null;
	private static RandomAccessFile wavFile;

	public static OutputStream getOutputStream() {
		file = createFile(new File(path));
		OutputStream outputStream;
		try {
			outputStream = new FileOutputStream(file);
		} catch (FileNotFoundException e) {
			throw new RuntimeException("could not build OutputStream from" +
					" this file" + file.getName(), e);
		}
		return outputStream;
	}

	public static boolean mkdir(File file) {
		while (!file.getParentFile().exists()) {
			mkdir(file.getParentFile());
		}
		return file.mkdir();
	}

	public static File createFile(File file) {
		try {
			if (!file.getParentFile().exists()) {
				mkdir(file.getParentFile());
			}
			boolean success = file.createNewFile();
			return file;
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
	}

	public static void writeWavHeader() {
		try {
			wavFile = randomAccessFile(file);
			long totalAudioLen = new FileInputStream(file).getChannel().size();
			wavFile.seek(0); // to the beginning
			wavFile.write(new WavHeader(totalAudioLen).toBytes());
			wavFile.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private static RandomAccessFile randomAccessFile(File file) {
		RandomAccessFile randomAccessFile;
		try {
			randomAccessFile = new RandomAccessFile(file, "rw");
		} catch (FileNotFoundException e) {
			throw new RuntimeException(e);
		}
		return randomAccessFile;
	}
}
