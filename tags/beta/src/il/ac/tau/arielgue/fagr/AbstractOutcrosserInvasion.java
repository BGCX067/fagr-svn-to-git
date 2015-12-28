package il.ac.tau.arielgue.fagr;

import il.ac.tau.yoavram.pes.AbstractInvasion;

import java.util.List;

import com.google.common.collect.Lists;

public abstract class AbstractOutcrosserInvasion extends
		AbstractInvasion<Outcrosser, Outcrosser> {

	public List<Population> invadePopulations(List<Population> populations) {
		List<List<Outcrosser>> list = Lists.newArrayList();
		for (Population pop : populations)
			list.add(pop);
		
		list = super.invade(list);
		
		List<Population> result = Lists.newArrayList();
		for (List<Outcrosser> pop : list) {
			result.add(new Population(pop));
		}
		return result;
	}

}
