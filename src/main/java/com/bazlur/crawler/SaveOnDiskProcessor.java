package com.bazlur.crawler;

import org.jsoup.nodes.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.channels.ReadableByteChannel;
import java.nio.charset.StandardCharsets;

/**
 * @author Bazlur Rahman Rokon
 * @since 10/27/16.
 */
public class SaveOnDiskProcessor implements PageProcessor {
	private static final Logger LOGGER = LoggerFactory.getLogger(SaveOnDiskProcessor.class);

	private String baseDir;

	public SaveOnDiskProcessor(String baseDir) {
		this.baseDir = baseDir;
	}

	@Override
	public void process(Document document) {
		String title = document.title();
		title = title.replaceAll("[^a-zA-Z0-9]", "-");
		title = title.toLowerCase();

		String text = document.text();
		InputStream inputStream = new ByteArrayInputStream(text.getBytes(StandardCharsets.UTF_8));

		File outputFile = new File(baseDir+ File.separator + title + ".html");
		try (FileChannel out = new FileOutputStream(outputFile).getChannel();
		     ReadableByteChannel source = Channels.newChannel(inputStream)) {

			ByteBuffer buffer = ByteBuffer.allocateDirect(4096);

			while (source.read(buffer) != -1) {
				buffer.flip();
				out.write(buffer);
				buffer.clear();
			}
		} catch (IOException e) {
			LOGGER.info("Could not save info in file:{}");
		}
	}
}
