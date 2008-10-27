package org.jdmp.core.algorithm.basic;

import java.util.HashMap;
import java.util.Map;

import org.jdmp.core.algorithm.AlgorithmTwoSources;
import org.jdmp.core.variable.Variable;
import org.ujmp.core.Matrix;
import org.ujmp.core.exceptions.MatrixException;

public class Times extends AlgorithmTwoSources {
	private static final long serialVersionUID = 7647708274140595747L;

	public Times(Variable... variables) {
		super(variables);
		setDescription("target = source1 .* source2");
	}

	public Map<Object, Matrix> calculate(Map<Object, Matrix> input) throws MatrixException {
		Map<Object, Matrix> result = new HashMap<Object, Matrix>();

		Matrix source1 = input.get(SOURCE1);
		Matrix source2 = input.get(SOURCE2);

		result.put(TARGET, source1.times(source2));

		return result;

	}
}
