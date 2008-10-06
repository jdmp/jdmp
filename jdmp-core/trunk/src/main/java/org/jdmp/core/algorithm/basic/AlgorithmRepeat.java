package org.jdmp.core.algorithm.basic;

import java.util.List;

import org.jdmp.core.algorithm.AbstractAlgorithm;
import org.jdmp.core.algorithm.Algorithm;
import org.ujmp.core.Matrix;

public class AlgorithmRepeat extends AbstractAlgorithm {
	private static final long serialVersionUID = 1469854394519674382L;

	private final int REPEATEDALGORITHM = 0;

	private int repeatCount = 1;

	public AlgorithmRepeat() {
		super("Repeat");
		setDescription("calls another Algorithms several times");
	}

	@Override
	public List<Matrix> calculate(List<Matrix> matrixList) throws Exception {
		List<Matrix> result = null;
		for (int i = 0; i < repeatCount; i++) {
			result = getAlgorithmList().get(REPEATEDALGORITHM).calculate(matrixList);
		}
		return result;

	}

	@Override
	public String getEdgeLabelForAlgorithm(int i) {
		return "Repeat " + repeatCount + " times";
	}

	@Override
	public int getEdgeDirectionForAlgorithm(int i) {
		return Algorithm.OUTGOING;
	}

	public int getRepeatCount() {
		return repeatCount;
	}

	public void setRepeatCount(int repeatCount) {
		this.repeatCount = repeatCount;
	}

	public void setRepeatedAlgorithm(Algorithm a) {
		setAlgorithm(REPEATEDALGORITHM, a);
	}

	public Algorithm getRepeatedAlgorithm() {
		return getAlgorithmList().get(REPEATEDALGORITHM);
	}
}
