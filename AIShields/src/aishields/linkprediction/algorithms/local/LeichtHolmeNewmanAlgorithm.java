package aishields.linkprediction.algorithms.local;

import aishields.core.Graph;
import aishields.linkprediction.algorithms.LinkPredictionAlgorithm;

/**
 * Leicht-Holme-Newman link prediction algorithm.

@article{leicht2006vertex,
  title={Vertex similarity in networks},
  author={Leicht, Elizabeth A and Holme, Petter and Newman, Mark EJ},
  journal={Physical Review E},
  volume={73},
  number={2},
  pages={026120},
  year={2006},
  publisher={APS}
}

 * @author Marcin Waniek
 */
public class LeichtHolmeNewmanAlgorithm extends LinkPredictionAlgorithm {

	@Override
	public String getName() {
		return "leichtHolmeNewman";
	}
	
	@Override
	protected Double computeScore(Graph g, int i, int j) {
		if (g.getDegree(i) == 0 || g.getDegree(j) == 0)
			return 0.;
		return (double) g.getNumberOfCommonNeighs(i, j) / (g.getDegree(i) * g.getDegree(j));
	}
}
