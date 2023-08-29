package aishields.linkprediction.algorithms;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Predicate;

import aishields.core.Edge;
import aishields.core.Graph;
import aishields.core.GraphChangeListener;
import aishields.core.LimitedMemoryRanking;
import aishields.core.Ranking;

/**
 * Representation of a link prediction algorithm.
 * 
 * @author Marcin Waniek
 */
public abstract class LinkPredictionAlgorithm implements GraphChangeListener {

	protected Graph lastGraph;
	protected Map<Edge,Double> scores;
	
	public abstract String getName();
	
	protected abstract Double computeScore(Graph g, int i, int j);

	public Ranking<Edge> getNonEdgesRanking(Graph g){
		Ranking<Edge> res = new Ranking<>();
		addNonEdges(g, res);
		return res;
	}
	
	public Ranking<Edge> getNonEdgesRanking(Graph g, int sizeLimit){
		Ranking<Edge> res = new LimitedMemoryRanking<>(sizeLimit);
		addNonEdges(g, res);
		return res;
	}
	
	public Ranking<Edge> getEdgesRanking(Graph g){
		Ranking<Edge> res = new Ranking<>();
		addEdges(g, res);
		return res;
	}
	
	public Ranking<Edge> getEdgesRanking(Graph g, int sizeLimit){
		Ranking<Edge> res = new LimitedMemoryRanking<>(sizeLimit);
		addEdges(g, res);
		return res;
	}
	
	public Ranking<Edge> getEdgesRanking(Graph g, Predicate<Integer> nodeFilter){
		Ranking<Edge> res = new Ranking<>();
		addEdges(g, res);
		return res;
	}
	
	private void addNonEdges(Graph g, Ranking<Edge> rank){
		for (Edge e : g.nonEdges())
			rank.setScore(e, getScore(g, e));
	}
	
	private void addEdges(Graph g, Ranking<Edge> rank){
		for (Edge e : g.edges())
			rank.setScore(e, getScore(g, e));
	}
	
	public double getScore(Graph g, int i, int j){
		if (i == j || g.containsEdge(i, j))
			return 0.;
		if (g != lastGraph && lastGraph != null)
			lastGraph.unsubscribe(this);
		if (g != lastGraph || scores == null) {
			lastGraph = g;
			g.subscribe(this);
			scores = new HashMap<>();
		}
		if (!scores.containsKey(g.e(i, j)))
			scores.put(g.e(i, j), computeScore(g, i, j));
		return scores.get(g.e(i, j));
	}
	
	public double getScore(Graph g, Edge e){
		return getScore(g, e.i(), e.j());
	}
	
	@Override
	public void notifyAdd(Graph g, Edge e) {
		scores = null;	
	}
	
	@Override
	public void notifyRemove(Graph g, Edge e) {
		scores = null;
	}
	
	@Override
	public void notifyReset(Graph g) {
		scores = null;
	}
	
	public static boolean isLocal(String name) {
		return name.equals("adamicAdar") || name.equals("commonNeighbours") || name.equals("hubDepressed")
				|| name.equals("hubPromoted") || name.equals("jaccard") || name.equals("leichtHolmeNewman")
				|| name.equals("resourceAllocation") || name.equals("salton") || name.equals("sorensen");
	}
}
