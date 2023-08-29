package aishields.linkprediction.algorithms.local;

import aishields.core.Graph;
import aishields.linkprediction.algorithms.LinkPredictionAlgorithm;

/**
 * Salton link prediction algorithm.

@article{salton1986introduction,
  title={Introduction to modern information retrieval},
  author={Salton, Gerard and McGill, Michael J},
  year={1986},
  publisher={McGraw-Hill, Inc.}
}

 * @author Marcin Waniek
 */
public class SaltonAlgorithm extends LinkPredictionAlgorithm {

	@Override
	public String getName() {
		return "salton";
	}
	
	@Override
	protected Double computeScore(Graph g, int i, int j) {
		if (g.getDegree(i) == 0 || g.getDegree(j) == 0)
			return 0.;
		return (double) g.getNumberOfCommonNeighs(i, j) / Math.sqrt(g.getDegree(i) * g.getDegree(j));
	}
}
