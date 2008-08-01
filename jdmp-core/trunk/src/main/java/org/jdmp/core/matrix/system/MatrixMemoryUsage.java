package org.jdmp.core.matrix.system;

import java.util.TimerTask;

import org.jdmp.core.util.GlobalTimer;
import org.ujmp.core.Matrix;
import org.ujmp.core.doublematrix.AbstractDenseDoubleMatrix2D;

public class MatrixMemoryUsage extends AbstractDenseDoubleMatrix2D {
	private static final long serialVersionUID = -3863745960302379726L;

	private static Matrix matrix = null;

	private MatrixMemoryUsage() {
		GlobalTimer.getInstance().schedule(new TimerTask() {

			@Override
			public void run() {
				matrix.notifyGUIObject();

			}
		}, 1000, 1000);
	}

	public static Matrix getInstance() {
		if (matrix == null) {
			matrix = new MatrixMemoryUsage();
		}
		return matrix;
	}

	public double getDouble(long row, long column) {
		switch ((int) row) {
		case 0:
			return Runtime.getRuntime().freeMemory();
		case 1:
			return Runtime.getRuntime().maxMemory();
		default:
			return Runtime.getRuntime().totalMemory();
		}
	}

	public long[] getSize() {
		return new long[] { 3, 1 };
	}

	public void setDouble(double value, long row, long column) {
	}

	public boolean isReadOnly() {
		return true;
	}

}
