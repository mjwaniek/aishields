package aishields.linkprediction.algorithms.local;

import aishields.core.Graph;
import aishields.linkprediction.algorithms.LinkPredictionAlgorithm;

/**
 * Hub depressed link prediction algorithm.

@article{ravasz2002hierarchical,
  title={Hierarchical organization of modularity in metabolic networks},
  author={Ravasz, Erzs{\'e}bet and Somera, Anna Lisa and Mongru, Dale A and Oltvai, Zolt{\'a}n N and Barab{\'a}si, A-L},
  journal={science},
  volume={297},
  number={5586},
  pages={1551--1555},
  year={2002},
  publisher={American Association for the Advancement of Science}
}

 * @author Marcin Waniek
 */
public class HubDepressedAlgorithm extends LinkPredictionAlgorithm {

	@Override
	public String getName() {
		return "hubDepressed";
	}
	
	@Override
	protected Double computeScore(Graph g, int i, int j) {
		if (g.getDegree(i) == 0 || g.getDegree(j) == 0)
			return 0.;
		return (double) g.getNumberOfCommonNeighs(i, j) / Math.max(g.getDegree(i), g.getDegree(j));
	}
}
