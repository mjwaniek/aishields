package aishields.utils.anet;

import java.util.List;

/**
 * Representation of a source of a network in ANET format.
 * 
 * @author Marcin Waniek
 */
public abstract class ANETSource {

	public abstract List<List<String>> getContent();
}
