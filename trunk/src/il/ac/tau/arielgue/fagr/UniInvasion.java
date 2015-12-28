package il.ac.tau.arielgue.fagr;

public class UniInvasion extends AbstractOutcrosserInvasion {

	@Override
	protected Outcrosser transform(Outcrosser o) {
		o.setOutcrossingModifier(Outcrosser.HOMOZYGOT_AA,Outcrosser.HOMOZYGOT_AA);
		return o;
	}

	@Override
	public String getInvaderName() {
		return Outcrosser.class.getSimpleName()
				+ " homozygote in outcrossing modifier";
	}

}
