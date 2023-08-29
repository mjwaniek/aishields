package aishields.utils.anet;

import java.util.List;

import aishields.utils.FileReaderWriter;

/**
 * Loading ANET contents from zipped resource in JAR file.
 * 
 * @author Marcin Waniek
 */
public class ANETZipResource extends ANETSource{
	
	private String path;
	private String zipEntry;

	public ANETZipResource(String path, String zipEntry) {
		this.path = path;
		this.zipEntry = zipEntry;
	}

	@Override
	public List<List<String>> getContent() {
		return FileReaderWriter.importCSVFromZipResource(path, zipEntry, ANETFormat.SEP_REGEX);
	}
}
