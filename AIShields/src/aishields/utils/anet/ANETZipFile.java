package aishields.utils.anet;

import java.util.List;

import aishields.utils.FileReaderWriter;

/**
 * Loading ANET contents from ZIP file.
 * 
 * @author Marcin Waniek
 */
public class ANETZipFile extends ANETSource {
	
	private String path;
	private String zipEntry;
	
	public ANETZipFile(String path, String zipEntry) {
		this.path = path;
		this.zipEntry = zipEntry;
	}
	
	@Override
	public List<List<String>> getContent() {
		return FileReaderWriter.importCSVFromZipFile(path, zipEntry, ANETFormat.SEP_REGEX);
	}
}
