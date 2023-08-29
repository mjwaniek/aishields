package aishields;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Stroke;
import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Collectors;

import aishields.core.Edge;
import aishields.core.Graph;
import aishields.core.GraphGenerator;
import aishields.core.Ranking;
import aishields.experiment.ExperimentResult;
import aishields.experiment.Row;
import aishields.linkprediction.AreaUnderROCCurve;
import aishields.linkprediction.algorithms.LinkPredictionAlgorithm;
import aishields.linkprediction.algorithms.local.*;
import aishields.utils.Utils;
import aishields.utils.anet.ANETFile;
import aishields.utils.anet.GraphExporter;
import aishields.utils.anet.GraphImporter;
import aishields.utils.visualizing.GraphVisualizer;
import aishields.utils.visualizing.VisualParameters;
import aishields.utils.visualizing.layout.CircleNodeLayout;

/**
 * Generating link prediction survey instances for the AI shields study.
 * 
 * @author Marcin Waniek
 */
public class GenerateLinkPredictionInstances {
	
	private static final List<BiFunction<Integer,Integer,Graph>> GENS = Utils.aList(
			(n,d) -> GraphGenerator.generateBarabasiAlbertGraph(n,d),
			(n,d) -> GraphGenerator.generateErdosRenyiGraph(n, d),
			(n,d) -> GraphGenerator.generateSmallWorldGraph(n, d, .25));
	private static final List<LinkPredictionAlgorithm> ALGS = Utils.aList(new CommonNeighboursAlgorithm(),
			new SaltonAlgorithm(), new JaccardAlgorithm(), new SorensenAlgorithm(), new HubPromotedAlgorithm(),
			new HubDepressedAlgorithm(), new LeichtHolmeNewmanAlgorithm(), new AdamicAdarAlgorithm(),
			new ResourceAllocationAlgorithm());
	
	private static final String OUTPUT_PATH = "output/ai-shields/linkpred/";
	private static final String EYE_OUTPUT_RES_FILE = OUTPUT_PATH + "linkpred-eye.csv";
	private static final String EYE_OUTPUT_ANET_DIR = OUTPUT_PATH + "linkpred-eye-anet/";
	private static final String EYE_OUTPUT_IMAGE_DIR = OUTPUT_PATH + "linkpred-eye-image/";
	private static final String SHIELD_OUTPUT_RES_FILE = OUTPUT_PATH + "linkpred-shield.csv";
	private static final String SHIELD_OUTPUT_ANET_DIR = OUTPUT_PATH + "linkpred-shield-anet/";
	private static final String SHIELD_OUTPUT_IMAGE_DIR = OUTPUT_PATH + "linkpred-shield-image/";
	private static final int INSTANCES_PER_MODEL = 334;
	protected static final int NETWORK_SIZE = 15;
	protected static final int AVERAGE_DEGREE = 4;
	private static final int REM_OPTIONS = 4;
	private static final int ADD_OPTIONS = 4;
	private static final int HIDING_BUDGET = 3;
	private static final long REPEATABLE_SEED = 1138;
	
	private static final double MAX_SCORE_REQ = .025;
	private static final double MIN_SCORE_REQ = -.025;
	private static final double ST_DEV_REQ = .025;
	
	public static void main(String[] args) {
		generateEyeInstances();
		generateShieldInstances();
		
		printEyeInstances();
		printShieldInstances();
	}
	
	public static void generateEyeInstances() {
		Utils.RAND.setSeed(REPEATABLE_SEED);
		ExperimentResult eyeRes = new ExperimentResult(OUTPUT_PATH, Utils.aList("linkpred-eye-survey"),
				Utils.aList("id", "correct", "alg", "algProb", "edges"));
		int id = 0;
		for (BiFunction<Integer,Integer,Graph> gen : GENS)
			for (int instance = 0; instance < INSTANCES_PER_MODEL; ++instance){
				Graph g = gen.apply(NETWORK_SIZE, AVERAGE_DEGREE);
				int evader = Utils.argmax(g.nodesStream().boxed(), i -> g.getDegree(i));
				Edge h = g.e(evader, g.getNeighs(evader).getRandom());
				g.removeEdge(h);
				Ranking<LinkPredictionAlgorithm> rank = algsRank(g, h);
				eyeRes.addRowDontPrint(id, edgeToLetters(h), rank.getBest().getName(), rank.getBestScore(),
						g.edgesStream().map(e -> edgeToLetters(e)).collect(Collectors.joining(",")));
				exportGraph(g, h, EYE_OUTPUT_ANET_DIR + id + ".anet");
				++id;
				if (id % 10 == 0)
					System.out.println("Finished " + id + " eye instances.");
			}
		eyeRes.saveResult(EYE_OUTPUT_RES_FILE);
	}
	
	public static void generateShieldInstances() {
		Utils.RAND.setSeed(REPEATABLE_SEED);
		ExperimentResult shieldRes = new ExperimentResult(OUTPUT_PATH, Utils.aList("linkpred-shield-survey"),
				Utils.aList("id","correct","alg","algProb","highAdd","lowAdd","highRem","lowRem","effectiveness","best"));
		int id = 0;
		for (BiFunction<Integer,Integer,Graph> gen : GENS)
			for (int instance = 0; instance < INSTANCES_PER_MODEL; ++instance){
				boolean added = false;
				while (!added) {
					Graph g = gen.apply(NETWORK_SIZE, AVERAGE_DEGREE);
					int evader = Utils.argmax(g.nodesStream().boxed(), i -> g.getDegree(i));
					Edge h = g.e(evader, g.getNeighs(evader).getRandom());
					g.removeEdge(h);
					Ranking<LinkPredictionAlgorithm> rank = algsRank(g, h);	
					
					List<Edge> allRem = h.stream().boxed()
							.flatMap(i -> g.getNeighs(i).stream().mapToObj(j -> g.e(i, j))).collect(Collectors.toList());
					List<Edge> allAdd = h.stream().boxed()
							.flatMap(i -> g.getNeighs(i).stream().flatMap(j -> g.getNeighs(j).stream()).distinct()
									.filter(k -> !h.contains(k) && !g.getNeighs(i).contains(k))
									.mapToObj(k -> g.e(i, k)))
							.collect(Collectors.toList());
					if (allRem.size() < REM_OPTIONS || allAdd.size() < ADD_OPTIONS)
						continue;
					
					Ranking<Edge> remRank = new Ranking<>(allRem, e -> {
						g.removeEdge(e);
						Ranking<LinkPredictionAlgorithm> eRank = algsRank(g, h);
						g.addEdge(e);
						return eRank.getBestScore();
					});
					Ranking<Edge> addRank = new Ranking<>(allAdd, e -> {
						g.addEdge(e);
						Ranking<LinkPredictionAlgorithm> eRank = algsRank(g, h);
						g.removeEdge(e);
						return eRank.getBestScore();
					});
					List<Edge> highRem = remRank.getBottom(REM_OPTIONS / 2);
					List<Edge> lowRem = remRank.getTop(REM_OPTIONS / 2);
					List<Edge> highAdd = addRank.getBottom(ADD_OPTIONS / 2);
					List<Edge> lowAdd = addRank.getTop(ADD_OPTIONS / 2);
					List<Edge> all = Utils.concat(highAdd, lowAdd, highRem, lowRem);
					Function<List<Edge>,String> subRep = sub -> sub.stream().map(e -> Integer.toString(all.indexOf(e)))
							.collect(Collectors.joining(""));
					
					Map<List<Edge>, Double> scores = new HashMap<>();
					for (List<Edge> sub :
							Utils.sublistsOfSize(Utils.concat(highRem, lowRem, highAdd, lowAdd), HIDING_BUDGET)) {
						g.startRecordingHistory();
						for (Edge e : sub)
							g.swapEdge(e);
						scores.put(sub, rank.getBestScore() - algsRank(g, h).getBestScore());
						g.resetGraph();
					}
					DecimalFormat df = new DecimalFormat("#.####");
					if (scores.values().stream().mapToDouble(x -> x).max().getAsDouble() >= MAX_SCORE_REQ
							&& scores.values().stream().mapToDouble(x -> x).min().getAsDouble() <= MIN_SCORE_REQ
							&& Utils.sd(scores.values()) >= ST_DEV_REQ) {
						List<Edge> best = Utils.argmax(scores.keySet(), sub -> scores.get(sub));
						shieldRes.addRowDontPrint(id, edgeToLetters(h), rank.getBest().getName(), rank.getBestScore(),
								highAdd.stream().map(e -> edgeToLetters(e)).collect(Collectors.joining(",")),
								lowAdd.stream().map(e -> edgeToLetters(e)).collect(Collectors.joining(",")),
								highRem.stream().map(e -> edgeToLetters(e)).collect(Collectors.joining(",")),
								lowRem.stream().map(e -> edgeToLetters(e)).collect(Collectors.joining(",")),
								scores.keySet().stream().map(sub -> subRep.apply(sub) + ":" + df.format(scores.get(sub)))
										.collect(Collectors.joining(",")),
								subRep.apply(best) + ":" + df.format(scores.get(best)));
						exportGraph(g, h, SHIELD_OUTPUT_ANET_DIR + id + ".anet");
						++id;
						added = true;
					}
				}
				if (id % 10 == 0)
					System.out.println("Finished " + id + " shield instances.");
			}
		shieldRes.saveResult(SHIELD_OUTPUT_RES_FILE);
	}
	
	public static void printEyeInstances() {
		ExperimentResult res = ExperimentResult.loadResult(EYE_OUTPUT_RES_FILE);
		for (Row r : res) {
			Graph g = GraphImporter.importGraph(new ANETFile(EYE_OUTPUT_ANET_DIR + r.get("id") + ".anet"), null);
			VisualParameters vp = visualParams(g);
			g.nodesStream().forEach(i -> vp.setNodeLabel(i, nodeToLetter(i)));
			GraphVisualizer.exportGraphToPNG(g, EYE_OUTPUT_IMAGE_DIR + r.get("id") + ".png", vp);
		}
	}
	
	private static Color HIDDEN_COLOR = new Color(244, 195, 56);
	private static Stroke HIDDEN_STROKE =
			new BasicStroke(10, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL, 0, new float[]{20, 5}, 0);
	private static Color ADD_COLOR = new Color(105, 185, 224);
	private static Stroke ADD_STROKE =
			new BasicStroke(7, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL, 0, new float[]{10, 10}, 0);
	private static Color REM_COLOR = new Color(239, 77, 78);
	private static Stroke REM_STROKE = new BasicStroke(7);
	
	public static void printShieldInstances() {
		ExperimentResult res = ExperimentResult.loadResult(SHIELD_OUTPUT_RES_FILE);
		for (Row r : res) {
			Graph g = GraphImporter.importGraph(new ANETFile(SHIELD_OUTPUT_ANET_DIR + r.get("id") + ".anet"), null);
			Edge h = lettersToEdge(r.get("correct"), g);
			g.addEdge(h);
			List<Edge> adds = Arrays.stream((r.get("highAdd") + "," + r.get("lowAdd")).split(","))
					.map(es -> lettersToEdge(es, g)).collect(Collectors.toList());
			adds.forEach(e -> g.addEdge(e));
			List<Edge> rems = Arrays.stream((r.get("highRem") + "," + r.get("lowRem")).split(","))
					.map(es -> lettersToEdge(es, g)).collect(Collectors.toList());
			VisualParameters vp = visualParams(g);
			g.nodesStream().forEach(i -> vp.setNodeLabel(i, nodeToLetter(i)));
			vp.setEdgeOrder(e -> e.equals(h) || adds.contains(e) || rems.contains(e) ? 1. : 0.);
			vp.setEdgeColor(h, HIDDEN_COLOR).setEdgeStroke(h, HIDDEN_STROKE);
			adds.forEach(e -> vp.setEdgeColor(e, ADD_COLOR).setEdgeStroke(e, ADD_STROKE));
			rems.forEach(e -> vp.setEdgeColor(e, REM_COLOR).setEdgeStroke(e, REM_STROKE));
			GraphVisualizer.exportGraphToPNG(g, SHIELD_OUTPUT_IMAGE_DIR + r.get("id") + ".png", vp);
		}
	}
	
	private static String nodeToLetter(int i) {
		return Character.toString((char)('A' + i));
	}
	
	private static String edgeToLetters(Edge e) {
		return nodeToLetter(e.i()) + nodeToLetter(e.j());
	}
	
	private static Edge lettersToEdge(String s, Graph g) {
		return g.e(s.charAt(0) - 'A', s.charAt(1) - 'A');
	}
	
	private static Ranking<LinkPredictionAlgorithm> algsRank(Graph g, Edge h) {
		Set<Edge> probeSet = new HashSet<>();
		probeSet.add(h);
		return new Ranking<>(ALGS, alg -> AreaUnderROCCurve.computeAUC(g, probeSet, alg));
	}
	
	private static VisualParameters visualParams(Graph g) {
		return new VisualParameters(new CircleNodeLayout(g, 800, 800))
				.setDefaultDrawNodeLabel(true).setDefaultNodeLabelColor(Color.WHITE)
				.setDefaultNodeFillColor(Color.BLACK).setDefaultNodeBorderStroke(new BasicStroke(0));
	}
	
	private static void exportGraph(Graph g, Edge h, String path) {
		GraphExporter.exportToANET(g, path, Utils.aList("hidden", h.i().toString(), h.j().toString()));
	}
}
