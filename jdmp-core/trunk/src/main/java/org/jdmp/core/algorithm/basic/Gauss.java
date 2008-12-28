/*
 * Copyright (C) 2008 Holger Arndt, Andreas Naegele and Markus Bundschus
 *
 * This file is part of the Java Data Mining Package (JDMP).
 * See the NOTICE file distributed with this work for additional
 * information regarding copyright ownership and licensing.
 *
 * JDMP is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * JDMP is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with JDMP; if not, write to the
 * Free Software Foundation, Inc., 51 Franklin St, Fifth Floor,
 * Boston, MA  02110-1301  USA
 */

package org.jdmp.core.algorithm.basic;

import java.util.HashMap;
import java.util.Map;

import org.jdmp.core.algorithm.AbstractAlgorithm;
import org.jdmp.core.variable.Variable;
import org.ujmp.core.Matrix;
import org.ujmp.core.MatrixFactory;
import org.ujmp.core.exceptions.MatrixException;

public class Gauss extends AbstractAlgorithm {
	private static final long serialVersionUID = 3127916742763698423L;

	private final double sigma = 1;

	private final double mu = 0.0;

	public Gauss(Variable... variables) {
		super();
		setDescription("target = gauss(source)");
		addVariableKey(SOURCE);
		addVariableKey(TARGET);
		setEdgeLabel(SOURCE, "Source");
		setEdgeLabel(TARGET, "Target");
		setEdgeDirection(SOURCE, EdgeDirection.Incoming);
		setEdgeDirection(TARGET, EdgeDirection.Outgoing);
		setVariables(variables);
	}

	@Override
	public Map<Object, Matrix> calculate(Map<Object, Matrix> input) throws MatrixException {
		Map<Object, Matrix> result = new HashMap<Object, Matrix>();

		Matrix in = MatrixFactory.copyFromMatrix(input.get(SOURCE));
		for (long[] c : in.allCoordinates()) {
			in.setAsDouble(getProbability(in.getAsDouble(c)), c);
		}

		result.put(TARGET, in);
		return result;
	}

	public double getProbability(double x) {
		return 1.0 / (sigma * Math.sqrt(2 * Math.PI))
				* Math.exp(-Math.pow(x - mu, 2) / (2 * sigma * sigma));
	}

	public void setSourceVariable(Variable source) {
		setVariable(SOURCE, source);
	}

	public void setTargetVariable(Variable target) {
		setVariable(TARGET, target);
	}

}
