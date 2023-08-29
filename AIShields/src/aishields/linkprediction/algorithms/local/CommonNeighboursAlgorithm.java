package aishields.linkprediction.algorithms.local;

import aishields.core.Graph;
import aishields.linkprediction.algorithms.LinkPredictionAlgorithm;

/**
 * Common neighbours link prediction algorithm.

@article{newman2001clustering,
  title={Clustering and preferential attachment in growing networks},
  author={Newman, Mark EJ},
  journal={Physical review E},
  volume={64},
  number={2},
  pages={025102},
  year={2001},
  publisher={APS}
}

 * @author Marcin Waniek
 */
public class CommonNeighboursAlgorithm extends LinkPredictionAlgorithm {

	@Override
	public String getName() {
		return "commonNeighbours";
	}
	
	@Override
	protected Double computeScore(Graph g, int i, int j) {
		return (double)g.getNumberOfCommonNeighs(i, j);
	}
}
