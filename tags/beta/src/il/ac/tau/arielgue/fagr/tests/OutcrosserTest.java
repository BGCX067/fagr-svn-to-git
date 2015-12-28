package il.ac.tau.arielgue.fagr.tests;

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
	public void testOutcrosserOutcrosser() {
		fail("Not yet implemented");
	}

	// @Test
	// public void testNewGenome() {
	// fail("Not yet implemented");
	// }
	//
	// @Test
	// public void testNewGenomeSortedMapOfIntegerInteger() {
	// fail("Not yet implemented");
	// }

	@Test
	public void testReproduce() {
		for (int i = 0; i < 10; i++) {
			o1.getGenome().put(i, 3); // hom mutations at locus i
		}
		;

	}

	@Test
	public void testSegragateInt() {
		fail("Not yet implemented");
	}

	@Test
	public void testZygote() {
		fail("Not yet implemented");
	}

	@Test
	public void testSegragateSortedMapOfIntegerInteger() {
		fail("Not yet implemented");
	}

	@Test
	public void testRecombinate() {
		fail("Not yet implemented");
	}

	@Test
	public void testCrossover() {
		fail("Not yet implemented");
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
	public void testGetFitness0_95() {
		o1.getGenome().put(5, 1); // het mutation at locus 5
		double fitness = o1.getFitness();
		assertEquals(
				1 - o1.getHeterozygoteCoefficient()
						* o1.getSelectionCoefficient(), fitness, 0);
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

	@Test
	public void testGetOutcrossingTendency() {
		double p = 0.4;
		o1.setUniformOutcrossingProbability(p);
		assertEquals(p, o1.getOutcrossingTendency(0.5)[1], 0);
		o1.setFao(true);
		assertEquals(p * 0.666666666666, o1.getOutcrossingTendency(0.5)[1],
				0.001);
	}

	@Test
	public void testIsFao() {
		o1.setOutcrossingModifier(Outcrosser.HETEROZYGOT_AB);
		boolean b = o1.isFao();
		assertTrue(o1.isFao() == b);
		// Outcrosser o2 = new IdealAncestor();
		o2.setOutcrossingModifier(Outcrosser.HOMOZYGOT_BB);
		assertTrue(o2.isFao());
	}

}
