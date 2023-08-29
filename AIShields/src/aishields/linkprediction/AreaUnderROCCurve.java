package aishields.linkprediction;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import aishields.core.Edge;
import aishields.core.Graph;
import aishields.core.MonteCarloAlgorithm;
import aishields.core.Ranking;
import aishields.linkprediction.algorithms.LinkPredictionAlgorithm;
import aishields.utils.Utils;

/**
 * Implementation of the Area under ROC curve statistical measure.
 * 
 * @author Marcin Waniek
 */
public class AreaUnderROCCurve {
	private static final int MONTE_CARLO_THRESHOLD = 900;
	
	public static double computeAUC(Graph g, Set<Edge> probeSet, LinkPredictionAlgorithm alg){
		return computeAUC(g, probeSet, alg, (g.size() < MONTE_CARLO_THRESHOLD));
	}
	
	public static double computeAUC(Graph g, Set<Edge> probeSet, LinkPredictionAlgorithm alg,
			boolean exact){
		if (exact)
			return computeExact(g, probeSet, alg);
		else
			return computeMonteCarlo(g, probeSet, alg);
	}
	
	private static double computeExact(Graph g, Set<Edge> probeSet, LinkPredictionAlgorithm alg){
		Ranking<Edge> r = alg.getNonEdgesRanking(g);
		int probeLeft = probeSet.size();
		int otherLeft = r.size() - probeSet.size();
		double totalPairs = probeLeft * otherLeft; 

		double sum = 0;
		int i = 0; 
		while (i < r.size()){
			int probeSegment = 0;
			int otherSegment = 0;
			Edge e;
			do {
				e = r.get(i+1);
				if (probeSet.contains(e)){
					probeSegment++;
					probeLeft--;
				} else {
					otherSegment++;
					otherLeft--;
				}
				++i;
			} while (i < r.size() && r.getScore(e) == r.getScore(r.get(i+1)));
			sum += probeSegment * otherLeft + (double) probeSegment * otherSegment / 2; 
		}
		return sum / totalPairs;
	}
	
	private static double computeMonteCarlo(final Graph g, final Set<Edge> probeSet,
			final LinkPredictionAlgorithm alg){
		return new MonteCarloAlgorithm() {

			private double identifiedSum = 0.;
			private List<Edge> probeEdges = new ArrayList<>(probeSet);
									
			@Override
			protected void singleMCIteration() {
				Edge probe = probeEdges.get(Utils.RAND.nextInt(probeSet.size()));
				Edge rest = null;
				while (rest == null) {
					Edge e = new Edge(Utils.RAND.nextInt(g.size()),
							Utils.RAND.nextInt(g.size()), g.isDirected());
					if ((e.i() != e.j()) && !g.containsEdge(e) && !probeSet.contains(e))
						rest = e;
				}
				if (alg.getScore(g, probe) > alg.getScore(g, rest))
					identifiedSum += 1.;
				else if (alg.getScore(g, probe) == alg.getScore(g, rest))
					identifiedSum += 0.5;
			}
			
			@Override
			protected double getControlSum(int iter) {
				return identifiedSum / iter;
			}
			
			public int getMinIterations() {
				return 10000;
			}
		}.runProcess();
	}
}
