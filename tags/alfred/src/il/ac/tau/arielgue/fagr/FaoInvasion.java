package il.ac.tau.arielgue.fagr;

import il.ac.tau.yoavram.pes.utils.RandomUtils;

public class FaoInvasion extends AbstractOutcrosserInvasion {

	@Override
	protected Outcrosser transform(Outcrosser o) {
		o.setOutcrossingModifier(Outcrosser.HOMOZYGOT_BB);
		
//		if (RandomUtils.coinToss())
//			o.setOutcrossingModifier(Outcrosser.HETEROZYGOT_AB);
//		else
//			o.setOutcrossingModifier(Outcrosser.HETEROZYGOT_BA);
		return o;
	}

	@Override
	public String getInvaderName() {
		return Outcrosser.class.getSimpleName()
				+ " heterozygot in outcrossing modifier";
	}

}
