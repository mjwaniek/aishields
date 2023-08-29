package aishields.utils.visualizing;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.util.function.Function;

import javax.imageio.ImageIO;

import com.itextpdf.awt.PdfGraphics2D;
import com.itextpdf.text.Document;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.pdf.PdfContentByte;
import com.itextpdf.text.pdf.PdfTemplate;
import com.itextpdf.text.pdf.PdfWriter;

import aishields.core.Edge;
import aishields.core.Graph;
import aishields.utils.visualizing.layout.*;

/**
 * Visualizing graph.
 * 
 * @author Marcin Waniek
 */
public class GraphVisualizer {

	public static void exportGraphToPDF(Graph g, String filePath, VisualParameters params) {
		new File(filePath).getParentFile().mkdirs();
		try {
			float w = params.getNodeLayout().getTotalDim().width;
			float h = params.getNodeLayout().getTotalDim().height;
			Document document = new Document(new Rectangle(w, h));
			PdfWriter writer = PdfWriter.getInstance(document, new FileOutputStream(filePath));
			document.open();
			PdfContentByte canvas = writer.getDirectContent();
			PdfTemplate template = canvas.createTemplate(w, h);
			
			Graphics2D g2d = new PdfGraphics2D(template, w, h);
			drawGraph(g, g2d, params);
			g2d.dispose();
			
			canvas.addTemplate(template, 0, 0);
			document.newPage();
			document.close();
		} catch (Exception e) {
			System.err.println("Error while exporting graph to PDF file.");
			e.printStackTrace();
		}
	}
	
	public static void exportGraphToPNG(Graph g, String filePath, VisualParameters params) {
		new File(filePath).getParentFile().mkdirs();
		try {
			int w = params.getNodeLayout().getTotalDim().width;
			int h = params.getNodeLayout().getTotalDim().height;
		    BufferedImage img = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
		    Graphics2D g2d = img.createGraphics();
		    drawGraph(g, g2d, params);
		    ImageIO.write(img, "png", new File(filePath));			
		} catch (Exception e) {
			System.err.println("Error while exporting graph to PNG file.");
			e.printStackTrace();
		}
	}

	private static void drawGraph(Graph g, Graphics2D g2d, VisualParameters vp) {
		NodeLayout p = vp.getNodeLayout();
	    g2d.setColor(vp.getBackgroundColor());
	    g2d.fillRect(0, 0, p.getTotalDim().width, p.getTotalDim().height);
	    drawEdges(g, g2d, vp, p);
	    drawNodes(g, g2d, vp, p);
	}
	
	private static void drawEdges(Graph g, Graphics2D g2d, VisualParameters vp, NodeLayout p) {
		Function<Edge,Double> eo = vp.getEdgeOrder();
		g.edgesStream().sorted((e1,e2) -> eo.apply(e1).compareTo(eo.apply(e2))).filter(e -> vp.getEdgeStroke(e) != null)
				.forEach(e -> {
			g2d.setColor(vp.getEdgeColor(e));
			g2d.setStroke(vp.getEdgeStroke(e));
			if (!g.isDirected()){
				g2d.drawLine(p.getTotalX(e.i()), p.getTotalY(e.i()), p.getTotalX(e.j()), p.getTotalY(e.j()));
			} else {
				double dx = p.getTotalX(e.j()) - p.getTotalX(e.i());
				double dy = p.getTotalY(e.j()) - p.getTotalY(e.i());
				double angle = Math.atan2(dy, dx);
				int nr = vp.getNodeRadius(e.j());
				int len = (int) Math.sqrt(dx*dx + dy*dy) - nr;
				AffineTransform ot = g2d.getTransform();
				AffineTransform at = AffineTransform.getTranslateInstance(p.getTotalX(e.i()), p.getTotalY(e.i()));
				at.concatenate(AffineTransform.getRotateInstance(angle));
				g2d.setTransform(at);
				int al = vp.getArrowLength(e);
				int aw = vp.getArrowWidth(e);
				g2d.drawLine(g.containsEdge(e.j(), e.i()) ? nr + al : 0, 0, len - al, 0);
				g2d.fillPolygon(new int[] {len, len-al, len-al, len}, new int[] {0, -aw/2, aw/2, 0}, 4);
				g2d.setTransform(ot);
			}
			if (vp.getDrawEdgeLabel(e)) {
				String label = vp.getEdgeLabel(e);
				int x = (p.getTotalX(e.j()) + p.getTotalX(e.i())) / 2;
				int y = (p.getTotalY(e.j()) + p.getTotalY(e.i())) / 2;
				g2d.setFont(vp.getEdgeLabelFont(e));
				int frameW = g2d.getFontMetrics().stringWidth(label);
				int frameH = g2d.getFontMetrics().getHeight();
				g2d.drawRect(x - frameW / 2, y - frameH / 2, frameW, frameH);
				g2d.setColor(Color.WHITE);
				g2d.fillRect(x - frameW / 2, y - frameH / 2, frameW, frameH);
				g2d.setColor(vp.getEdgeLabelColor(e));
				g2d.drawString(label,
						x - g2d.getFontMetrics().stringWidth(label) / 2,
						y + (g2d.getFontMetrics().getAscent() - g2d.getFontMetrics().getDescent()) / 2);
			}
		});
	}

	private static void drawNodes(Graph g, Graphics2D g2d, VisualParameters vp, NodeLayout p) {
		for (Integer v : g.nodes())
			if (vp.getNodeDraw(v)){
				int x = p.getTotalX(v);
				int y = p.getTotalY(v);
				int r = vp.getNodeRadius(v);
				g2d.setColor(vp.getNodeFillColor(v));
				if (vp.getNodeRectangle(v))
					g2d.fillRect(x - r, y - r, r * 2, r * 2);
				else
					g2d.fillOval(x - r, y - r, r * 2, r * 2);
				g2d.setColor(vp.getNodeBorderColor(v));
				if (vp.getNodeBorderStroke(v) != null) {
					g2d.setStroke(vp.getNodeBorderStroke(v));
					if (vp.getNodeRectangle(v))
						g2d.drawRect(x - r, y - r, r * 2, r * 2);
					else
						g2d.drawOval(x - r, y - r, r * 2, r * 2);
				}
				if (vp.getDrawNodeLabel(v)) {
					g2d.setColor(vp.getNodeLabelColor(v));
					g2d.setFont(vp.getNodeLabelFont(v));
					String label = vp.getNodeLabel(v);
					g2d.drawString(label,
							x - g2d.getFontMetrics().stringWidth(label) / 2,
							y + (g2d.getFontMetrics().getAscent() - g2d.getFontMetrics().getDescent()) / 2);
				}
			}
	}
}
