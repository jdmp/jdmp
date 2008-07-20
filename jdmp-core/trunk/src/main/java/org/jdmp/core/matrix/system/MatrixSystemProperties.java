package org.jdmp.core.matrix.system;

import java.util.Map;

import org.ujmp.core.Matrix;
import org.ujmp.core.matrices.stubs.AbstractMapMatrix;

public class MatrixSystemProperties extends AbstractMapMatrix {
	private static final long serialVersionUID = -5746939082111495919L;

	private static MatrixSystemProperties matrix = null;

	public static Matrix getInstance() {
		if (matrix == null) {
			matrix = new MatrixSystemProperties();
		}
		return matrix;
	}

	@Override
	public Map getMap() {
		return System.getProperties();
	}

}
