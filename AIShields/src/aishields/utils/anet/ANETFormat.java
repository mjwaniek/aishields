package aishields.utils.anet;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Description of the ANET network file format.
 * 
 * @author Marcin Waniek
 */
public class ANETFormat {

	public static final String FILE_EXT = ".anet";
	public static final String ASSIGN = "=";
	public static final String ASSIGN_REGEX = "=";
	public static final String SEP = " ";
	public static final String SEP_REGEX = "\\s+";
	
	public static final String NAME_ATTR = "name";
	public static final String SIZE_ATTR = "n";
	public static final String DIRECTED_ATTR = "directed";	private static final Boolean DEF_DIRECTED_ATTR = false;
	public static final String HEADER_ATTR = "header";		private static final Boolean DEF_HEADER_ATTR = false;
	public static final String LW_ATTR = "lw";				private static final Boolean DEF_LW_ATTR = false;
	public static final String L_VOID_ATTR = "lvoid";		private static final Boolean DEF_L_VOID_ATTR = true;
	public static final String W_VOID_ATTR = "wvoid";		private static final Boolean DEF_W_VOID_ATTR = true;
	public static final String LAYERS_ATTR = "layers";
	public static final String TEMPORAL_ATTR = "temporal";
	
	public static final String LABELED_ATTR = "labeled";
	
	public static Map<String, String> readParams(List<String> line){
		Map<String, String> params = new HashMap<>();
		params.put(DIRECTED_ATTR, DEF_DIRECTED_ATTR.toString());
		params.put(HEADER_ATTR, DEF_HEADER_ATTR.toString());
		params.put(LW_ATTR, DEF_LW_ATTR.toString());
		params.put(L_VOID_ATTR, DEF_L_VOID_ATTR.toString());
		params.put(W_VOID_ATTR, DEF_W_VOID_ATTR.toString());
		for (String s : line) {
			String[] parts = s.split(ANETFormat.ASSIGN_REGEX);
			params.put(parts[0], parts[1]);
		}
		return params;
	}
}
