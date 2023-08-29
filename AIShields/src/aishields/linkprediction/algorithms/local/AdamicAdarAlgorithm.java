package aishields.linkprediction.algorithms.local;

import aishields.core.Graph;
import aishields.linkprediction.algorithms.LinkPredictionAlgorithm;

/**
 * Adamic-Adar link prediction algorithm.

@article{adamic2003friends,
  title={Friends and neighbors on the web},
  author={Adamic, Lada A and Adar, Eytan},
  journal={Social networks},
  volume={25},
  number={3},
  pages={211--230},
  year={2003},
  publisher={Elsevier}
}

 * @author Marcin Waniek
 */
public class AdamicAdarAlgorithm extends LinkPredictionAlgorithm {

	@Override
	public String getName() {
		return "adamicAdar";
	}
	
	@Override
	protected Double computeScore(Graph g, int i, int j) {
		double res = 0.;
		for (int k : g.getCommonNeighs(i, j))
			res += 1. / Math.log(g.getDegree(k));
		return res;
	}
}
