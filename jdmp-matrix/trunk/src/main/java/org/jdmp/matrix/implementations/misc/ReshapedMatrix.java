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

package org.jdmp.matrix.implementations.misc;

import java.util.Iterator;

import org.jdmp.matrix.Matrix;
import org.jdmp.matrix.coordinates.Coordinates;
import org.jdmp.matrix.exceptions.MatrixException;
import org.jdmp.matrix.interfaces.HasSourceMatrix;
import org.jdmp.matrix.stubs.AbstractGenericMatrix;
import org.jdmp.matrix.util.collections.DefaultMatrixList;
import org.jdmp.matrix.util.collections.MatrixList;

public class ReshapedMatrix<A> extends AbstractGenericMatrix<A> implements HasSourceMatrix {
	private static final long serialVersionUID = -4298270756453090584L;

	private Matrix source = null;

	private long[] newSize = null;

	private long[] oldSize = null;

	public ReshapedMatrix(Matrix source, long... newSize) {
		this.source = source;
		this.newSize = newSize;
		this.oldSize = source.getSize();
	}

	public boolean contains(long... coordinates) {
		return false;
	}

	public Iterable<long[]> allCoordinates() {
		return new CoordinateIterable();
	}

	private class CoordinateIterable implements Iterable<long[]> {

		public Iterator<long[]> iterator() {
			return new CoordinateIterator();
		}

	}

	private class CoordinateIterator implements Iterator<long[]> {

		private Iterator<long[]> iterator = source.allCoordinates().iterator();

		public boolean hasNext() {
			return iterator.hasNext();
		}

		public long[] next() {
			return getNewCoordinates(iterator.next());
		}

		public void remove() {
		}

	}

	private long[] getOldCoordinates(long[] newCoordinates) {
		long[] oldCoordinates = Coordinates.copyOf(newCoordinates);
		long valueNumber = newCoordinates[ROW] * newSize[COLUMN] + newCoordinates[COLUMN];
		oldCoordinates[ROW] = valueNumber / oldSize[COLUMN];
		oldCoordinates[COLUMN] = valueNumber % oldSize[COLUMN];
		return oldCoordinates;
	}

	private long[] getNewCoordinates(long[] oldCoordinates) {
		long[] newCoordinates = Coordinates.copyOf(oldCoordinates);
		long valueNumber = oldCoordinates[ROW] * oldSize[COLUMN] + oldCoordinates[COLUMN];
		newCoordinates[ROW] = (valueNumber / newSize[COLUMN]);
		newCoordinates[COLUMN] = (valueNumber % newSize[COLUMN]);
		return newCoordinates;
	}

	public long[] getSize() {
		return newSize;
	}

	public double getDouble(long... coordinates) throws MatrixException {
		return source.getDouble(getOldCoordinates(coordinates));
	}

	public A getObject(long... coordinates) throws MatrixException {
		return (A) source.getObject(getOldCoordinates(coordinates));
	}

	@Override
	public long getValueCount() {
		return source.getValueCount();
	}

	@Override
	public boolean isReadOnly() {
		return source.isReadOnly();
	}

	public boolean isSparse() {
		return source.isSparse();
	}

	public void setDouble(double value, long... coordinates) throws MatrixException {
		source.setDouble(value, getOldCoordinates(coordinates));
	}

	public void setObject(Object value, long... coordinates) throws MatrixException {
		source.setObject(value, getOldCoordinates(coordinates));
	}

	public MatrixList getSourceMatrices() {
		MatrixList matrices = new DefaultMatrixList();
		if (getSourceMatrix() instanceof HasSourceMatrix) {
			matrices.addAll(((HasSourceMatrix) getSourceMatrix()).getSourceMatrices());
		}
		matrices.add(getSourceMatrix());
		return matrices;
	}

	public Matrix getSourceMatrix() {
		return source;
	}

	public EntryType getEntryType() {
		return getSourceMatrix().getEntryType();
	}

}
