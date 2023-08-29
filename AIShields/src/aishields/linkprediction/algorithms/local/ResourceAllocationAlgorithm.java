package aishields.linkprediction.algorithms.local;

import aishields.core.Graph;
import aishields.linkprediction.algorithms.LinkPredictionAlgorithm;

/**
 * Resource allocation link prediction algorithm.

@article{zhou2009predicting,
  title={Predicting missing links via local information},
  author={Zhou, Tao and L{\"u}, Linyuan and Zhang, Yi-Cheng},
  journal={The European Physical Journal B},
  volume={71},
  number={4},
  pages={623--630},
  year={2009},
  publisher={Springer}
}

 * @author Marcin Waniek
 */
public class ResourceAllocationAlgorithm extends LinkPredictionAlgorithm {

	@Override
	public String getName() {
		return "resourceAllocation";
	}
	
	@Override
	protected Double computeScore(Graph g, int i, int j) {
		double res = 0.;
		for (int k : g.getCommonNeighs(i, j))
			res += 1. / g.getDegree(k);
		return res;
	}
}
