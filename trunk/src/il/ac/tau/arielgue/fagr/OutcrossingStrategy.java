package il.ac.tau.arielgue.fagr;

import java.io.Serializable;
import java.util.List;

public interface OutcrossingStrategy extends Serializable {
	void outcross(List<Population> demes, Population pool);

	void outcross(Population deme, Population pool);

	double getOutcrossingCost();

	double getStepValue();

	int getEffectiveOutcrossings();
}
