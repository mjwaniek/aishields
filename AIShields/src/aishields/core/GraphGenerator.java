package aishields.core;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import aishields.utils.ProbabilityDistribution;
import aishields.utils.Utils;

/**
 * Class for generating graphs using different models and modifying them.
 * 
 * @author Marcin Waniek
 */
public class GraphGenerator {
	
	public static Graph combine(List<Graph> gs){
		int n = gs.stream().mapToInt(g -> g.size()).sum();
		String name = gs.stream().map(g -> g.getName()).reduce((s1,s2) -> s1 + "+" + s2).orElse("G");
		Graph res = new Graph(name, n);
		int offset = 0;
		for (Graph g : gs) {
			for (Edge e : g.edges())
				res.addEdge(e.i() + offset, e.j() + offset);
			offset += g.size();
		}
		return res;
	}
	
	public static Graph randomlyDistort(Graph g, double prob){
		Graph res = new Graph(g.getName(), g.size());
		for (int i : g.nodes())
			for (int j : g.nodes())
				if (i < j || (g.isDirected() && i != j)){
					double r = Utils.RAND.nextDouble();
					if ((!g.containsEdge(i, j) && r <= prob) || (g.containsEdge(i, j) && r > prob)) 
						res.addEdge(i, j);
				}
		return res;
	}
	
	public static Graph randomlyRemove(Graph g, double prob){
		Graph res = new Graph(g.getName(), g.size());
		for (int i : g.nodes())
			for (int j : g.nodes())
				if ((i < j || (g.isDirected() && i != j))
						&& g.containsEdge(i, j) && Utils.RAND.nextDouble() > prob) 
					res.addEdge(i, j);
		return res;
	}

	public static Graph randomlyAdd(Graph g, double prob){
		Graph res = new Graph(g.getName(), g.size());
		for (int i : g.nodes())
			for (int j : g.nodes())
				if ((i < j || (g.isDirected() && i != j))
						&& (g.containsEdge(i, j) || Utils.RAND.nextDouble() <= prob)) 
					res.addEdge(i, j);
		return res;
	}

	public static Graph generateCycle(int n) {
		return generateCycle(n, false);
	}
	
	public static Graph generateCycle(int n, boolean directed) {
		Graph res = new Graph("cycle(" + n + ")", n, directed);
		for (int i : res.nodes()) {
			res.addEdge(i, (i + 1) % n);
			if (directed)
				res.addEdge((i + 1) % n, i);
		}
		return res;
	}

	public static Graph generateClique(int n) {
		return generateClique(n, false);
	}
	
	public static Graph generateClique(int n, boolean directed) {
		Graph res = new Graph("clique(" + n + ")", n, directed);
		for (int i : res.nodes())
			for (int j = i + 1; j < n; ++j){
				res.addEdge(i, j);
				if (directed)
					res.addEdge(j, i);
			}
		return res;
	}
	
	public static Graph generateGrid(int width, int height) {
		return generateGrid(width, height, false);
	}
	
	public static Graph generateGrid(int width, int height, boolean directed) {
		Graph res = new Graph("grid(" + width + "," + height + ")", width * height, directed);
		for (int i = 0; i < height; ++i)
			for (int j = 0; j < width; ++j){
				int num = i * width + j;
				if (j + 1 < width) {
					res.addEdge(num, num + 1);
					if (directed)
						res.addEdge(num + 1, num);
				}
				if (i + 1 < height) {
					res.addEdge(num, num + width);
					if (directed)
						res.addEdge(num + width, num);
				}
			}
		return res;
	}
	
/*
@article{barabasi1999emergence,
	title={Emergence of scaling in random networks},
	author={Barab{\'a}si, Albert-L{\'a}szl{\'o} and Albert, R{\'e}ka},
	journal={science},
	volume={286},
	number={5439},
	pages={509--512},
	year={1999},
	publisher={American Association for the Advancement of Science}
}
*/
	public static Graph generateBarabasiAlbertGraph(int n, int avgDegree){
		return generateBarabasiAlbertGraph(n, avgDegree, false);
	}
	
	public static Graph generateBarabasiAlbertGraph(int n, int avgDegree, boolean directed){
		String name = "ba-" + n + "-" + avgDegree;
		if (directed)
			name = "d" + name;
		int m = avgDegree / 2; // how many edges are added with each node
		Graph res = new Graph(name, n, directed);
		ProbabilityDistribution<Integer> pd = new ProbabilityDistribution<>(
				Utils.asList(IntStream.range(0, n).boxed()), i -> (double)res.getDegree(i));
		for (int i = 0; i <= m; ++i)
			for (int j = 0; j < i; ++j) {
				res.addEdge(i, j);
				if (directed)
					res.addEdge(j, i);
			}
		for (int i = m + 1; i < n; ++i) {
			for (int edge = 0; edge < m; ++edge){
				int fi = i;
				int j = pd.drawFiltered(k -> k != fi && !res.containsEdge(fi, k));
				res.addEdge(i, j);
				if (directed)
					res.addEdge(j, i);
			}
			pd.reset();
		}
		return res;
	}
	
/*
@article{erdds1959random,
	title={On random graphs I.},
	author={Erd{\H{o}}s, Paul and R{\'e}nyi, Alfr{\'e}d},
	journal={Publ. Math. Debrecen},
	volume={6},
	pages={290--297},
	year={1959}
}
*/
	
	public static Graph generateErdosRenyiGraph(int n, Integer avgDegree){
		return generateErdosRenyiGraph(n, avgDegree, false);
	}
	
	public static Graph generateErdosRenyiGraph(int n, Integer avgDegree, boolean directed){
		String name = "er-" + n + "-" + avgDegree;
		double prob = (double) avgDegree / (n - 1);
		if (directed) {
			name = "d" + name;
			prob /= 2.;
		}
		Graph res = new Graph(name, n, directed);
		for (int i : res.nodes())
			for (int j = i + 1; j < n; ++j){
				if (Utils.RAND.nextDouble() <= prob)
					res.addEdge(i, j);
				if (directed && Utils.RAND.nextDouble() <= prob)
					res.addEdge(j, i);
			}
		res.forceConnectivity();
		return res;
	}
	
/*
@article{watts1998collective,
	title={Collective dynamics of small-world networks},
	author={Watts, Duncan J and Strogatz, Steven H},
	journal={nature},
	volume={393},
	number={6684},
	pages={440--442},
	year={1998},
	publisher={Nature Publishing Group}
}
*/

	public static Graph generateSmallWorldGraph(int n, int avgDegree, double beta){
		return generateSmallWorldGraph(n, avgDegree, beta, false);
	}
	
	public static Graph generateSmallWorldGraph(int n, int avgDegree, double beta, boolean directed){
		String name = "ws-" + n + "-" + avgDegree + "-" + Math.round(beta * 100.);
		if (directed)
			name = "d" + name;
		Graph res = new Graph(name, n, directed);
		for (int i : res.nodes())
			for (int j = i + 1; j <= i + avgDegree / 2; ++j) {
				res.addEdge(i, j % n);
				if (directed)
					res.addEdge(j % n, i);
			}
		if (avgDegree < n - 1)
			for (Edge e : res.edgesStream().collect(Collectors.toList()))
				if (Utils.RAND.nextDouble() < beta && res.getOutDegree(e.i()) < res.size() - 1){
					int i = e.i();
					int j = i;
					if (res.getOutDegree(i) > res.size() / 1000)
						j = Utils.getRandom(
								res.nodesStream().filter(k -> k != i && !res.getSuccs(i).contains(k)).boxed(),
								n - res.getOutDegree(i) - 1);
					else
						while (j == i || res.getSuccs(i).contains(j))
							j = Utils.RAND.nextInt(res.size());
					res.removeEdge(e);
					res.addEdge(i, j);
				}
		res.forceConnectivity();
		return res;
	}
}