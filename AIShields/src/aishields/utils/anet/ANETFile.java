package aishields.utils.anet;

import java.util.List;

import aishields.utils.FileReaderWriter;

/**
 * Loading ANET contents from file.
 * 
 * @author Marcin Waniek
 */
public class ANETFile extends ANETSource {
	
	private String path;
	
	public ANETFile(String path) {
		this.path = path;
	}

	@Override
	public List<List<String>> getContent() {
		return FileReaderWriter.importCSVFromFile(path, ANETFormat.SEP_REGEX);
	}
}
