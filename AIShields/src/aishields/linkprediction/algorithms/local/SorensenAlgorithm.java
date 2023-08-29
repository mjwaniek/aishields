package aishields.linkprediction.algorithms.local;

import aishields.core.Graph;
import aishields.linkprediction.algorithms.LinkPredictionAlgorithm;

/**
 * Sorensen link prediction algorithm.

@article{sorensen1948method,
  title={$\{$A method of establishing groups of equal amplitude in plant sociology based on similarity of species and its application to analyses of the vegetation on Danish commons$\}$},
  author={S{\o}rensen, Thorvald},
  journal={Biol. Skr.},
  volume={5},
  pages={1--34},
  year={1948}
}

 * @author Marcin Waniek
 */
public class SorensenAlgorithm extends LinkPredictionAlgorithm {
	
	@Override
	public String getName() {
		return "sorensen";
	}

	@Override
	protected Double computeScore(Graph g, int i, int j) {
		if (g.getDegree(i) == 0 || g.getDegree(j) == 0)
			return 0.;
		return (double) (2 * g.getNumberOfCommonNeighs(i, j)) / (g.getDegree(i) + g.getDegree(j));
	}
}
