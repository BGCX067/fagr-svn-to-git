package il.ac.tau.arielgue.fagr;

import java.io.Serializable;

public interface OutcrossingStrategy extends Serializable {
	double outcross(Population deme, Population pool);
}
