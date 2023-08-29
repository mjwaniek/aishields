package aishields.utils.visualizing;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Stroke;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import aishields.core.Edge;
import aishields.utils.visualizing.layout.NodeLayout;

/**
 * Parametrization of visual features of printed graph.
 * 
 * @author Marcin Waniek
 */
public class VisualParameters {
	private Color backgroundColor;
	private NodeLayout nodePos;
	private Function<Edge, Double> edgeOrder;

	private Map<Integer, Boolean> nodeDraw;
	private Map<Integer, Integer> nodeRadius;
	private Map<Integer, Color> nodeFillColor;
	private Map<Integer, Stroke> nodeBorderStroke;
	private Map<Integer, Color> nodeBorderColor;
	private Map<Integer, Boolean> nodeRectangle;
	private Map<Edge, Stroke> edgeStroke;
	private Map<Edge, Color> edgeColor;
	private Map<Edge, Integer> arrowLength;
	private Map<Edge, Integer> arrowWidth;
	private Map<Integer, String> nodeLabel;
	private Map<Integer, Boolean> drawNodeLabel;
	private Map<Integer, Font> nodeLabelFont;
	private Map<Integer, Color> nodeLabelColor;
	private Map<Edge, String> edgeLabel;
	private Map<Edge, Boolean> drawEdgeLabel;
	private Map<Edge, Font> edgeLabelFont;
	private Map<Edge, Color> edgeLabelColor;

	public VisualParameters(NodeLayout pos) {
		this.nodePos = pos;
		this.nodeDraw = new HashMap<>();
		this.nodeRadius = new HashMap<>();
		this.nodeFillColor = new HashMap<>();
		this.nodeBorderStroke = new HashMap<>();
		this.nodeBorderColor = new HashMap<>();
		this.nodeRectangle = new HashMap<>();
		this.edgeStroke = new HashMap<>();
		this.edgeColor = new HashMap<>();
		this.arrowLength = new HashMap<>();
		this.arrowWidth = new HashMap<>();
		this.nodeLabel = new HashMap<>();
		this.drawNodeLabel = new HashMap<>();
		this.nodeLabelFont = new HashMap<>();
		this.nodeLabelColor = new HashMap<>();
		this.edgeLabel = new HashMap<>();
		this.drawEdgeLabel = new HashMap<>();
		this.edgeLabelFont = new HashMap<>();
		this.edgeLabelColor = new HashMap<>();
		setDefault();
	}
	
	protected void setDefault() {
		this.backgroundColor = Color.WHITE;
		this.edgeOrder = e -> e.i().doubleValue() + e.j().doubleValue();
		this.nodeDraw.put(null, true);
		this.nodeRadius.put(null, 20);
		this.nodeFillColor.put(null, Color.LIGHT_GRAY);
		this.nodeBorderStroke.put(null, null);
		this.nodeBorderColor.put(null, Color.BLACK);
		this.nodeRectangle.put(null, false);
		this.edgeStroke.put(null, new BasicStroke(5));
		this.edgeColor.put(null, Color.BLACK);
		this.arrowLength.put(null, 20);
		this.arrowWidth.put(null, 16);
		this.drawNodeLabel.put(null, false);
		this.nodeLabelFont.put(null, new Font(null, Font.PLAIN, 28));
		this.nodeLabelColor.put(null, Color.BLACK);
		this.drawEdgeLabel.put(null, false);
		this.edgeLabelFont.put(null, new Font(null, Font.PLAIN, 28));
		this.edgeLabelColor.put(null, Color.BLACK);
	}
	
	public Color getBackgroundColor() { return backgroundColor; }
	public VisualParameters setBackgroundColor(Color color) { this.backgroundColor = color; return this; }
	
	public NodeLayout getNodeLayout() { return nodePos; }
	public VisualParameters setNodePos(NodeLayout nodePos) { this.nodePos = nodePos; return this; }

	public Function<Edge, Double> getEdgeOrder() { return edgeOrder; }
	public VisualParameters setEdgeOrder(Function<Edge, Double> eo) { this.edgeOrder = eo; return this; }

	public Boolean getNodeDraw(int v) { if (nodeDraw.containsKey(v)) return nodeDraw.get(v); else	return nodeDraw.get(null); }
	public VisualParameters setNodeDraw(int v, Boolean draw) { this.nodeDraw.put(v, draw); return this; }
	public VisualParameters setDefaultNodeDraw(Boolean draw) { this.nodeDraw.put(null, draw); return this; }
	
	public Integer getNodeRadius(int v) { if (nodeRadius.containsKey(v)) return nodeRadius.get(v); else	return nodeRadius.get(null); }
	public VisualParameters setNodeRadius(int v, Integer r) { this.nodeRadius.put(v, r); return this; }
	public VisualParameters setDefaultNodeRadius(Integer r) { this.nodeRadius.put(null, r); return this; }
	
	public Color getNodeFillColor(int v) { if (nodeFillColor.containsKey(v)) return nodeFillColor.get(v); else return nodeFillColor.get(null); }
	public VisualParameters setNodeFillColor(int v, Color color) { this.nodeFillColor.put(v, color); return this; }
	public VisualParameters setDefaultNodeFillColor(Color color) { this.nodeFillColor.put(null, color); return this; }

	public Stroke getNodeBorderStroke(int v) { if (nodeBorderStroke.containsKey(v)) return nodeBorderStroke.get(v); else return nodeBorderStroke.get(null); }
	public VisualParameters setNodeBorderStroke(int v, Stroke stroke) { this.nodeBorderStroke.put(v, stroke); return this; }
	public VisualParameters setDefaultNodeBorderStroke(Stroke stroke) { this.nodeBorderStroke.put(null, stroke); return this; }

	public Color getNodeBorderColor(int v) { if (nodeBorderColor.containsKey(v)) return nodeBorderColor.get(v); else return nodeBorderColor.get(null); }
	public VisualParameters setNodeBorderColor(int v, Color color) { this.nodeBorderColor.put(v, color); return this; }
	public VisualParameters setDefaultNodeBorderColor(Color color) { this.nodeBorderColor.put(null, color); return this; }
	
	public boolean getNodeRectangle(int v) { if (nodeRectangle.containsKey(v)) return nodeRectangle.get(v); else return nodeRectangle.get(null); }
	public VisualParameters setNodeRectangle(int v, boolean b) { this.nodeRectangle.put(v, b); return this; }
	public VisualParameters setDefaultNodeRectangle(boolean b) { this.nodeRectangle.put(null, b); return this; }
	
	public Stroke getEdgeStroke(Edge e) { if (edgeStroke.containsKey(e)) return edgeStroke.get(e); else return edgeStroke.get(null); }
	public VisualParameters setEdgeStroke(Edge e, Stroke stroke) { this.edgeStroke.put(e, stroke); return this; }
	public VisualParameters setDefaultEdgeStroke(Stroke stroke) { this.edgeStroke.put(null, stroke); return this; }
	
	public Color getEdgeColor(Edge e) { if (edgeColor.containsKey(e)) return edgeColor.get(e); else return edgeColor.get(null); }
	public VisualParameters setEdgeColor(Edge e, Color color) { this.edgeColor.put(e, color); return this; }
	public VisualParameters setDefaultEdgeColor(Color color) { this.edgeColor.put(null, color); return this; }
	
	public Integer getArrowLength(Edge e) { if (arrowLength.containsKey(e)) return arrowLength.get(e); else return arrowLength.get(null); }
	public VisualParameters setArrowLength(Edge e, Integer l) { this.arrowLength.put(e, l); return this; }
	public VisualParameters setDefaultArrowLength(Integer l) { this.arrowLength.put(null, l); return this; }
	
	public Integer getArrowWidth(Edge e) { if (arrowWidth.containsKey(e)) return arrowWidth.get(e); else return arrowWidth.get(null); }
	public VisualParameters setArrowWidth(Edge e, Integer w) { this.arrowWidth.put(e, w); return this; }
	public VisualParameters setDefaultArrowWidth(Integer w) { this.arrowWidth.put(null, w); return this; }

	public String getNodeLabel(Integer v) { if (nodeLabel.containsKey(v)) return nodeLabel.get(v); else return v.toString(); }
	public VisualParameters setNodeLabel(int v, String s) { this.nodeLabel.put(v, s); return this; }
	
	public boolean getDrawNodeLabel(int v) { if (drawNodeLabel.containsKey(v)) return drawNodeLabel.get(v); else return drawNodeLabel.get(null); }
	public VisualParameters setDrawNodeLabel(int v, boolean b) { this.drawNodeLabel.put(v, b); return this; }
	public VisualParameters setDefaultDrawNodeLabel(boolean b) { this.drawNodeLabel.put(null, b); return this; }

	public Font getNodeLabelFont(int v) { if (nodeLabelFont.containsKey(v)) return nodeLabelFont.get(v); else return nodeLabelFont.get(null); }
	public VisualParameters setNodeLabelFont(int v, Font font) { this.nodeLabelFont.put(v, font); return this; }
	public VisualParameters setDefaultNodeLabelFont(Font font) { this.nodeLabelFont.put(null, font); return this; }

	public Color getNodeLabelColor(int v) { if (nodeLabelColor.containsKey(v)) return nodeLabelColor.get(v); else return nodeLabelColor.get(null); }
	public VisualParameters setNodeLabelColor(int v, Color color) { this.nodeLabelColor.put(v, color); return this; }
	public VisualParameters setDefaultNodeLabelColor(Color color) { this.nodeLabelColor.put(null, color); return this; }
	
	public String getEdgeLabel(Edge e) { if (edgeLabel.containsKey(e)) return edgeLabel.get(e); else return e.toString(); }
	public VisualParameters setEdgeLabel(Edge e, String s) { this.edgeLabel.put(e, s); return this; }
	
	public boolean getDrawEdgeLabel(Edge e) { if (drawEdgeLabel.containsKey(e)) return drawEdgeLabel.get(e); else return drawEdgeLabel.get(null); }
	public VisualParameters setDrawEdgeLabel(Edge e, boolean b) { this.drawEdgeLabel.put(e, b); return this; }
	public VisualParameters setDefaultDrawEdgeLabel(boolean b) { this.drawEdgeLabel.put(null, b); return this; }

	public Font getEdgeLabelFont(Edge e) { if (edgeLabelFont.containsKey(e)) return edgeLabelFont.get(e); else return edgeLabelFont.get(null); }
	public VisualParameters setEdgeLabelFont(Edge e, Font font) { this.edgeLabelFont.put(e, font); return this; }
	public VisualParameters setDefaultEdgeLabelFont(Font font) { this.edgeLabelFont.put(null, font); return this; }

	public Color getEdgeLabelColor(Edge e) { if (edgeLabelColor.containsKey(e)) return edgeLabelColor.get(e); else return edgeLabelColor.get(null); }
	public VisualParameters setEdgeLabelColor(Edge e, Color color) { this.edgeLabelColor.put(e, color); return this; }
	public VisualParameters setDefaultEdgeLabelColor(Color color) { this.edgeLabelColor.put(null, color); return this; }
}
