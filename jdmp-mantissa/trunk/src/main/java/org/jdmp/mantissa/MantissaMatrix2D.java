/*
 * Copyright (C) 2008-2009 Holger Arndt, A. Naegele and M. Bundschus
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

package org.jdmp.mantissa;

import org.spaceroots.mantissa.linalg.GeneralMatrix;
import org.ujmp.core.Matrix;
import org.ujmp.core.coordinates.Coordinates;
import org.ujmp.core.doublematrix.AbstractDenseDoubleMatrix2D;
import org.ujmp.core.exceptions.MatrixException;
import org.ujmp.core.interfaces.Wrapper;

public class MantissaMatrix2D extends AbstractDenseDoubleMatrix2D implements
		Wrapper<org.spaceroots.mantissa.linalg.Matrix> {
	private static final long serialVersionUID = 6954233090244549806L;

	private org.spaceroots.mantissa.linalg.Matrix matrix = null;

	public MantissaMatrix2D(org.spaceroots.mantissa.linalg.Matrix m) {
		this.matrix = m;
	}

	public MantissaMatrix2D(long... size) {
		if (size[ROW] > 0 && size[COLUMN] > 0) {
			this.matrix = new GeneralMatrix((int) size[ROW], (int) size[COLUMN]);
		}
	}

	public MantissaMatrix2D(Matrix source) throws MatrixException {
		this(source.getSize());
		for (long[] c : source.availableCoordinates()) {
			setAsDouble(source.getAsDouble(c), c);
		}
	}

	public double getDouble(long row, long column) {
		return matrix.getElement((int) row, (int) column);
	}

	public long[] getSize() {
		return matrix == null ? Coordinates.ZERO2D : new long[] { matrix.getRows(),
				matrix.getColumns() };
	}

	public void setDouble(double value, long row, long column) {
		matrix.setElement((int) row, (int) column, value);
	}

	@Override
	public Matrix transpose() {
		return new MantissaMatrix2D(matrix.getTranspose());
	}

	public org.spaceroots.mantissa.linalg.Matrix getWrappedObject() {
		return matrix;
	}

	public void setWrappedObject(org.spaceroots.mantissa.linalg.Matrix object) {
		this.matrix = object;
	}

}
