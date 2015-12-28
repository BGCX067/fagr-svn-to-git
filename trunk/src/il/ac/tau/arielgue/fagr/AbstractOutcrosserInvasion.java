package il.ac.tau.arielgue.fagr;

import il.ac.tau.yoavram.pes.AbstractInvasion;
import il.ac.tau.yoavram.pes.filters.Filter;

import java.io.Serializable;
import java.util.List;

import com.google.common.collect.Lists;

public abstract class AbstractOutcrosserInvasion extends
		AbstractInvasion<Outcrosser, Outcrosser> implements Serializable {

	private static final long serialVersionUID = -2498639380053052782L;
	private double invaderOutcrossingProbability;
	private Filter<Outcrosser> filter = null;

	public List<Population> invadePopulations(List<Population> populations) {
		// this screws up the capacity
		List<List<Outcrosser>> list = Lists.newArrayList();
		for (Population pop : populations)
			list.add(pop);

		list = super.invade(list);

		List<Population> result = Lists.newArrayList();
		for (List<Outcrosser> pop : list) {
			for (Outcrosser o : pop) {
				if (filter == null || filter.filter(o)) {
					// TODO check this
					if (Double.isNaN(o.getFaoOutcrossingProbability()[0]))
						o.getFaoOutcrossingProbability()[0] = getInvaderOutcrossingProbability();
					else
						o.getFaoOutcrossingProbability()[1] = getInvaderOutcrossingProbability();
				}
			}
			result.add(new Population(pop));
		}
		return result;
	}

	public double getInvaderOutcrossingProbability() {
		return invaderOutcrossingProbability;
	}

	public void setInvaderOutcrossingProbability(
			double invaderOutcrossingProbability) {
		this.invaderOutcrossingProbability = invaderOutcrossingProbability;
	}

}
