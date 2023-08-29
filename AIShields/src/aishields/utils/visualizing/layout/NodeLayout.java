package aishields.utils.visualizing.layout;

import java.awt.Dimension;
import java.awt.Toolkit;

import aishields.core.Graph;

/**
 * Abstract representation of arranging nodes of a graph for printing.
 * 
 * @author Marcin Waniek
 */
public abstract class NodeLayout {
	
	protected Graph g;
	private Dimension totalDim;
	private int margin;
	private int[] innerX;
	private int[] innerY;
	
	public NodeLayout(Graph g, int width, int height) {
		this.g = g;
		this.totalDim = new Dimension(width, height);
		this.margin = 25;
		this.innerX = null;
		this.innerY = null;
	}
	
	public NodeLayout(Graph g, int width, int height, int margin) {
		this(g, width, height);
		this.margin = margin;
	}
	
	public NodeLayout(Graph g) {
		this(g, (int)(.8 * Toolkit.getDefaultToolkit().getScreenSize().getWidth()),
					(int)(.8 * Toolkit.getDefaultToolkit().getScreenSize().getHeight()));
	}
	
	public int getMargin() {
		return margin;
	}
	
	public NodeLayout setMargin(int margin) {
		this.margin = margin;
		return this;
	}
	
	public Dimension getTotalDim() {
		return totalDim;
	}
	
	protected Dimension getInnerDim() {
		return new Dimension(Math.max(10, totalDim.width - 2 * margin),
						Math.max(10, totalDim.height - 2 * margin));
	}
	
	public int getTotalX(int v) {
		return getInnerX(v) + margin;
	}
	
	protected int getInnerX(int v) {
		if (innerX == null || innerY == null) {
			innerX = new int[g.size()];
			innerY = new int[g.size()];
			assignPositions();
		}
		return innerX[v];
	}
	
	public void setTotalX(int v, int x) {
		setInnerX(v, Math.max(0, Math.min(getInnerDim().width, x - margin)));
	}
	
	protected void setInnerX(int v, int x) {
		this.innerX[v] = x;
	}
	
	public int getTotalY(int v) {
		return getInnerY(v) + margin;
	}
	
	protected int getInnerY(int v) {
		if (innerX == null || innerY == null) {
			innerX = new int[g.size()];
			innerY = new int[g.size()];
			assignPositions();
		}
		return innerY[v];
	}
	
	public void setTotalY(int v, int y) {
		setInnerY(v, Math.max(0, Math.min(getInnerDim().height, y - margin)));
	}
	
	protected void setInnerY(int v, int y) {
		this.innerY[v] = y;
	}
	
	protected abstract void assignPositions();
}
