package aishields.utils.anet;

import java.util.LinkedList;
import java.util.List;

import aishields.core.Edge;
import aishields.core.Graph;
import aishields.utils.FileReaderWriter;
import aishields.utils.Utils;

/**
 * Exporting graph to file.
 * 
 * @author Marcin Waniek
 */
public class GraphExporter {

	public static final String ENCODING = "UTF-8";
	
	public static void exportToANET(Graph g, String filePath, List<String> header) {
		List<List<String>> content = new LinkedList<>();
		content.add(Utils.aList(
			ANETFormat.NAME_ATTR + ANETFormat.ASSIGN + g.getName(),
			ANETFormat.SIZE_ATTR + ANETFormat.ASSIGN + g.size(),
			ANETFormat.DIRECTED_ATTR + ANETFormat.ASSIGN + g.isDirected(),
			ANETFormat.HEADER_ATTR + ANETFormat.ASSIGN + (header != null)));
		if (header != null)
			content.add(header);
		for (Edge e : g.edges())
			content.add(Utils.aList(e.i().toString(), e.j().toString()));
		FileReaderWriter.exportToCSV(content, filePath, ANETFormat.SEP);
	}
	
	public static void exportEdges(Graph g, String filePath){
		List<String> lines = new LinkedList<>();
		for (int i = 0; i < g.size(); ++i)
			for (int j = i + 1; j < g.size(); ++j){
				if (g.containsEdge(i, j))
					lines.add(i + " " + j);
				if (g.isDirected() && g.containsEdge(j, i))
					lines.add(j + " " + i);
			}
		FileReaderWriter.exportToFile(lines, filePath);
	}
}
