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

package org.jdmp.matrix.calculation.entrywise.replace;

import java.util.regex.Pattern;

import org.jdmp.matrix.Matrix;
import org.jdmp.matrix.MatrixFactory;
import org.jdmp.matrix.Matrix.EntryType;
import org.jdmp.matrix.calculation.StringCalculation;
import org.jdmp.matrix.exceptions.MatrixException;

public class ReplaceRegex extends StringCalculation {
	private static final long serialVersionUID = 8158807887609103123L;

	private Pattern searchPattern = null;

	private String replaceString = null;

	public ReplaceRegex(Matrix matrix, String searchString, String replaceString) {
		this(matrix, Pattern.compile(searchString), replaceString);
	}

	public ReplaceRegex(Matrix matrix, Pattern searchPattern, String replaceString) {
		super(matrix);
		this.searchPattern = searchPattern;
		this.replaceString = replaceString;
	}

	@Override
	public String getString(long... coordinates) throws MatrixException {
		String src = getSource().getString(coordinates);

		return (src == null) ? null : searchPattern.matcher(src).replaceAll(replaceString);
	}

	public static Matrix calc(Matrix source, Pattern search, String replacement) throws MatrixException {
		Matrix ret = MatrixFactory.zeros(EntryType.STRING, source.getSize());
		for (long[] c : source.availableCoordinates()) {
			String src = source.getString(c);
			ret.setString((src == null) ? null : search.matcher(src).replaceAll(replacement), c);
		}
		return ret;
	}

	public static Matrix calc(Matrix source, String searchString, String replacement) throws MatrixException {
		return calc(source, Pattern.compile(searchString), replacement);
	}
}
