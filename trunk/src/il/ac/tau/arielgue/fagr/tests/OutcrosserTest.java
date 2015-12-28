package il.ac.tau.arielgue.fagr.tests;

/*
 * r-click - run as JUnit test
 */
import static org.junit.Assert.*;
import il.ac.tau.arielgue.fagr.IdealAncestor;
import il.ac.tau.arielgue.fagr.Outcrosser;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class OutcrosserTest {
	Outcrosser o1;
	Outcrosser o2;

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
	}

	@Before
	public void setUp() throws Exception {
		o1 = new IdealAncestor();
		o2 = new IdealAncestor();
	}

	@Test
	public void testReproduce() {
		for (int i = 0; i < 10; i++) {
			o1.getGenome().put(i, 3); // hom mutations at locus i
		}
			}

	

	@Test
	public void testGetFitness1() {
		double fitness = o1.getFitness();
		assertEquals(1, fitness, 0);
		o1.mutate();
		fitness = o1.getFitness();
		assertEquals(1, fitness, 0);
	}

	@Test
	public void testGetMutationHeterozygosity() {
		assertEquals(0, o2.getDeleteriousMutationHeterozygosity(), 0);
		o2.getGenome().put(5, 1); // het mutation at locus 5
		o2.getGenome().put(6, 3); // hom mutation at locus 6
		assertEquals(0.0001, o2.getDeleteriousMutationHeterozygosity(), 0); // one mutation
																	// in a
																	// 10000
																	// long
																	// genome
		
	}

	@Test
	public void testGetMutationHomozygosity() {
		assertEquals(0, o2.getDeleteriousMutationHomozygosity(), 0);
		o2.getGenome().put(5, 1); // het mutation at locus 5
		o2.getGenome().put(6, 3); // hom mutation at locus 6
		assertEquals(0.0001, o2.getDeleteriousMutationHomozygosity(), 0); // one homozygotemutation
																	// in a
																	// 10000
																	// long
																	// genome
		assertEquals(0.0001, o2.getDeleteriousMutationHeterozygosity(), 0);
	}

	
	@Test
	public void testGetFitness2() {
		o1.getGenome().put(5, 1); // het mutation at locus 5
		o1.getGenome().put(6, 3); // het mutation at locus 5
		o1.setHeterozygoteCoefficient(0.3);
		o1.setSelectionCoefficient(0.2);
		double fitness = (1 - o1.getSelectionCoefficient())
				* (1 - o1.getHeterozygoteCoefficient()
						* o1.getSelectionCoefficient());
		assertEquals(fitness, o1.getFitness(), 0);
	}

	@Test
	public void testViable() {
		o1.setViabilityLimitFitness(3);
		assertTrue(o1.viable());
	}

	@Test
	public void testMutate() {
		o1.mutate();
		assertTrue(o1.getGenome().isEmpty());
	}

	@Test
	public void testCompareTo() {
		o1.getGenome().put(5, 1); // het mutation at locus 5
		// Outcrosser o2 = new IdealAncestor();
		int i1 = o1.compareTo(o2);
		assertEquals(i1, 1, 0);
	}

	// @Test
	// public void testGetOutcrossingTendency() {
	// double p = 0.4;
	// o1.setUniformOutcrossingProbability(p);
	// assertEquals(p, o1.getOutcrossingTendency(0.5)[1], 0);
	// o1.setFao(true);
	// assertEquals(p * 0.666666666666, o1.getOutcrossingTendency(0.5)[1],
	// 0.001);
	// }

	@Test
	public void testIsFao() {
		o1.setOutcrossingModifier(Outcrosser.HOMOZYGOT_AA,Outcrosser.HOMOZYGOT_BB);
		boolean b = o1.isFao();
		assertTrue(o1.isFao() == b);
		// Outcrosser o2 = new IdealAncestor();
		o2.setOutcrossingModifier(Outcrosser.HOMOZYGOT_BB,Outcrosser.HOMOZYGOT_BB);
		assertTrue(o2.isFao());
		o2.setOutcrossingModifier(Outcrosser.HOMOZYGOT_CC,Outcrosser.HOMOZYGOT_CC);
		assertTrue(o2.isFao());
	}

}
