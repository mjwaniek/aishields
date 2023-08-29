package aishields.utils.anet;

import java.util.List;

import aishields.utils.FileReaderWriter;

/**
 * Loading ANET contents from resource in JAR file.
 * 
 * @author Marcin Waniek
 */
public class ANETResource extends ANETSource {
	
	private String path;

	public ANETResource(String path) {
		this.path = path;
	}

	@Override
	public List<List<String>> getContent() {
		return FileReaderWriter.importCSVFromResource(path, ANETFormat.SEP_REGEX);
	}
}
