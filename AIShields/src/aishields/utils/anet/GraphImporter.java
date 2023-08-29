package aishields.utils.anet;

import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import aishields.core.Graph;

/**
 * Importing graphs from file.
 * 
 * @author Marcin Waniek
 */
public class GraphImporter {
	
	public static Graph importGraph(ANETSource src) {
		return importGraph(src, null);
	}
	
	public static Graph importGraph(ANETSource src, Consumer<List<String>> headerConsumer) {
		List<List<String>> content = src.getContent();
		Map<String, String> params = ANETFormat.readParams(content.remove(0));
		processHeader(content, params, headerConsumer);
		
		Graph res = new Graph(params.get(ANETFormat.NAME_ATTR),
				Integer.parseInt(params.get(ANETFormat.SIZE_ATTR)), 
				Boolean.parseBoolean(params.get(ANETFormat.DIRECTED_ATTR)));
		if (Boolean.parseBoolean(params.get(ANETFormat.LW_ATTR))
				&& !Boolean.parseBoolean(params.get(ANETFormat.L_VOID_ATTR)))
			for (int i = 0; i < res.size(); ++i)
				content.remove(0);
		for (List<String> line : content)
			res.addEdge(Integer.parseInt(line.get(0)), Integer.parseInt(line.get(1)));
		return res;
	}
		
	private static void processHeader(List<List<String>> content, Map<String, String> params,
			Consumer<List<String>> headerConsumer){
		if (Boolean.parseBoolean(params.get(ANETFormat.HEADER_ATTR))){
			List<String> header = content.remove(0);
			if (headerConsumer != null)
				headerConsumer.accept(header);
		}
	}
}
