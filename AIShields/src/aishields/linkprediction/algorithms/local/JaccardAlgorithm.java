package aishields.linkprediction.algorithms.local;

import aishields.core.Coalition;
import aishields.core.Graph;
import aishields.linkprediction.algorithms.LinkPredictionAlgorithm;

/**
 * Jaccard link prediction algorithm.

@book{jaccard1901etude,
  title={Etude comparative de la distribution florale dans une portion des Alpes et du Jura},
  author={Jaccard, Paul},
  year={1901},
  publisher={Impr. Corbaz}
}

 * @author Marcin Waniek
 */
public class JaccardAlgorithm extends LinkPredictionAlgorithm {

	@Override
	public String getName() {
		return "jaccard";
	}
	
	@Override
	protected Double computeScore(Graph g, int i, int j) {
		if (g.getDegree(i) == 0 || g.getDegree(j) == 0)
			return 0.;
		return (double) g.getNumberOfCommonNeighs(i, j) / Coalition.add(g.getNeighs(i), g.getNeighs(j)).size();
	}
}
