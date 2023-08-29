package aishields.utils.visualizing.layout;

import aishields.core.Graph;

/**
 * Layout for arranging nodes of a graph into circle. 
 * 
 * @author Marcin Waniek
 */
public class CircleNodeLayout extends NodeLayout {

	public CircleNodeLayout(Graph g, int width, int height) {
		super(g, width, height);
	}

	public CircleNodeLayout(Graph g) {
		super(g);
	}

	@Override
	protected void assignPositions() {
		int r = Math.min(getInnerDim().width, getInnerDim().height) / 2;
		double d = 2. * Math.PI / g.size();
		for (int v : g.nodes()) {
			setInnerX(v, getInnerDim().width / 2 + (int)(Math.sin(d * v) * r));
			setInnerY(v, getInnerDim().height / 2 - (int)(Math.cos(d * v) * r));
		}
	}
}
