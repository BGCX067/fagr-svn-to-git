package il.ac.tau.arielgue.fagr;

public class FaoInvasion extends AbstractOutcrosserInvasion {

	private int faoInvadingAllele = Outcrosser.HOMOZYGOT_BB; // 3. this is the
																// default

	@Override
	protected Outcrosser transform(Outcrosser o) {
		o.setOutcrossingModifier(getFaoInvadingAllele(), getFaoInvadingAllele());

		return o;
	}

	@Override
	public String getInvaderName() {
		return Outcrosser.class.getSimpleName()
				+ " homozygote in outcrossing modifier";
	}

	public void setFaoInvadingAllele(int faoAllele) {
		this.faoInvadingAllele = faoAllele;
	}

	public int getFaoInvadingAllele() {
		return faoInvadingAllele;
	}

}
