package org.jdmp.gui.matrix;

import java.awt.Component;
import java.awt.image.BufferedImage;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.event.EventListenerList;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableModel;

import org.jdmp.core.util.FastListSelectionModel;
import org.jdmp.gui.AbstractGUIObject;
import org.ujmp.core.Matrix;
import org.ujmp.core.calculation.Calculation.Ret;
import org.ujmp.core.calculation.entrywise.creators.Fill;
import org.ujmp.core.calculation.entrywise.creators.Rand;
import org.ujmp.core.calculation.entrywise.creators.Randn;
import org.ujmp.core.coordinates.Coordinates;
import org.ujmp.core.exceptions.MatrixException;
import org.ujmp.core.interfaces.HasColumnSelectionModel;
import org.ujmp.core.interfaces.HasRowSelectionModel;
import org.ujmp.core.util.UJMPSettings;
import org.ujmp.core.util.StringUtil;

public class MatrixGUIObject extends AbstractGUIObject implements TableModel,
		HasColumnSelectionModel, HasRowSelectionModel {
	private static final long serialVersionUID = -5777110889052748093L;

	private Matrix matrix = null;

	private transient String tooltipText = null;

	private transient ListSelectionModel rowSelectionModel = null;

	private transient ListSelectionModel columnSelectionModel = null;

	private transient EventListenerList listenerList = null;

	public MatrixGUIObject(Matrix m) {
		this.matrix = m;
	}

	public Matrix getMatrix() {
		return matrix;
	}

	public void clear() {
		matrix.clear();
		fireValueChanged();
	}

	public double getEstimatedMaxValue(long timeOut) throws MatrixException {
		double max = -Double.MAX_VALUE;
		long t0 = System.currentTimeMillis();
		long t1;
		double v = 0.0;
		for (long[] c : matrix.availableCoordinates()) {
			max = (v = matrix.getAsDouble(c)) > max ? v : max;
			t1 = System.currentTimeMillis();
			if (t1 - t0 > timeOut) {
				return max;
			}
		}
		return max;
	}

	public double getEstimatedMinValue(long timeOut) throws MatrixException {
		double min = Double.MAX_VALUE;
		long t0 = System.currentTimeMillis();
		long t1;
		double v = 0.0;
		for (long[] c : matrix.availableCoordinates()) {
			min = (v = matrix.getAsDouble(c)) < min ? v : min;
			t1 = System.currentTimeMillis();
			if (t1 - t0 > timeOut) {
				return min;
			}
		}
		return min;
	}

	public long getValueCount() {
		return matrix.getValueCount();
	}

	public final EventListenerList getListenerList() {
		if (listenerList == null) {
			listenerList = new EventListenerList();
		}
		return listenerList;
	}

	public String getLabel() {
		return matrix.getLabel();
	}

	public void setLabel(String label) {
		matrix.setLabel(label);
	}

	public final void addTableModelListener(TableModelListener l) {
		getListenerList().add(TableModelListener.class, l);
	}

	public final void removeTableModelListener(TableModelListener l) {
		getListenerList().add(TableModelListener.class, l);
	}

	public final String getToolTipText() {
		try {
			if (tooltipText == null) {
				StringBuffer s = new StringBuffer();
				s.append("<html>");
				s.append("<table>");
				s.append("<tr>");
				s.append("<td colspan=2><h3>Matrix</h3></td>");
				s.append("</tr>");
				s.append("<tr>");
				s.append("<td><b>Label:</b></td>");
				s.append("<td>" + getLabel() + "</td>");
				s.append("</tr>");
				s.append("<tr>");
				s.append("<td><b>Size:</b></td>");
				s.append("<td>" + getRowCount() + "x" + getColumnCount() + "</td>");
				s.append("</tr>");
				s.append("<tr>");
				s.append("<td><b>Values:</b></td>");
				s.append("<td>");
				s.append("<table border=1>");
				int rowCount = getRowCount();
				int columnCount = getColumnCount();

				// header
				s.append("<tr>");
				s.append("<th></th>");
				for (int col = 0; col < columnCount && col < UJMPSettings.getMaxToolTipCols(); col++) {
					s.append("<th>" + matrix.getColumnLabel(col) + "</th>");
				}
				if (getColumnCount() > UJMPSettings.getMaxToolTipCols()) {
					s.append("<th>...</th>");
				}
				s.append("</tr>");

				for (int row = 0; row < rowCount && row < UJMPSettings.getMaxToolTipRows(); row++) {
					s.append("<tr>");
					s.append("<th>" + matrix.getRowLabel(row) + "</th>");
					for (int col = 0; col < columnCount && col < UJMPSettings.getMaxToolTipCols(); col++) {
						s.append("<td align=right>" + StringUtil.format(matrix.getAsDouble(row, col))
								+ "</td>");
					}
					if (getColumnCount() > UJMPSettings.getMaxToolTipCols()) {
						s.append("<td align=right>...</td>");
					}
					s.append("</tr>");
				}
				if (getRowCount() > UJMPSettings.getMaxToolTipRows()) {
					s.append("<tr>");
					s.append("<td></td>");
					for (int col = 0; col < getColumnCount()
							&& col < UJMPSettings.getMaxToolTipCols(); col++) {
						s.append("<td align=right>...</td>");
					}
					if (getColumnCount() > UJMPSettings.getMaxToolTipCols()) {
						s.append("<td align=right>...</td>");
					}
					s.append("</tr>");
				}
				s.append("</table>");
				s.append("</td>");
				s.append("</tr>");
				s.append("</table>");
				s.append("</html>");
				tooltipText = s.toString();
			}
			return tooltipText;
		} catch (Exception e) {
			// TODO
			e.printStackTrace();
			return "error getting tooltip text";
		}
	}

	public final void fireValueChanged() {
		for (Object o : getListenerList().getListenerList()) {
			if (o instanceof TableModelListener)
				((TableModelListener) o).tableChanged(new TableModelEvent(this));
		}
	}

	public final void fireValueChanged(int row, int column, Object value) {
		for (Object o : getListenerList().getListenerList()) {
			if (o instanceof TableModelListener)
				((TableModelListener) o).tableChanged(new TableModelEvent(this, row, row, column,
						TableModelEvent.UPDATE));
		}
	}

	public final Class<?> getColumnClass(int columnIndex) {
		return Object.class;
	}

	public int getColumnCount() {
		return (int) matrix.getColumnCount();
	}

	public String getColumnName(int columnIndex) {
		String label = matrix.getColumnLabel(columnIndex);
		return label == null || "".equals(label) ? "" + columnIndex : label;
	}

	public int getRowCount() {
		return (int) matrix.getRowCount();
	}

	public Object getValueAt(long[] c) {
		return getValueAt((int) c[Matrix.ROW], (int) c[Matrix.COLUMN]);
	}

	public Object getValueAt(int rowIndex, int columnIndex) {
		try {
			return matrix.getObject(rowIndex, columnIndex);
		} catch (MatrixException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
	}

	public boolean isCellEditable(int rowIndex, int columnIndex) {
		return !matrix.isReadOnly();
	}

	public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
		try {
			matrix.setObject(aValue, rowIndex, columnIndex);
		} catch (MatrixException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		fireValueChanged(rowIndex, columnIndex, aValue);
		tooltipText = null;
	}

	public Iterable<long[]> coordinates() {
		return matrix.allCoordinates();
	}

	public final String getRowName(int row) {
		String label = matrix.getRowLabel(row);
		return label == null || "".equals(label) ? "" + row : label;
	}

	public int getZCount() {
		return (int) matrix.getZCount();
	}

	public boolean isSquare() {
		return matrix.isSquare();
	}

	public Double getDoubleValueAt(long... coordinates) throws MatrixException {
		return matrix.getAsDouble(coordinates);
	}

	public boolean isSparse() {
		return matrix.isSparse();
	}

	public boolean isScalar() {
		return matrix.isScalar();
	}

	public void setEntriesTo_(double value) throws MatrixException {
		new Fill(matrix, value).calc(Ret.ORIG);
		fireValueChanged();
	}

	public ListSelectionModel getColumnSelectionModel() {
		if (columnSelectionModel == null) {
			columnSelectionModel = new FastListSelectionModel();
		}
		return columnSelectionModel;
	}

	public void setColumnSelectionModel(ListSelectionModel columnSelectionModel) {
		this.columnSelectionModel = columnSelectionModel;
	}

	public ListSelectionModel getRowSelectionModel() {
		if (rowSelectionModel == null) {
			rowSelectionModel = new FastListSelectionModel();
		}
		return rowSelectionModel;
	}

	public void setRowSelectionModel(ListSelectionModel rowSelectionModel) {
		this.rowSelectionModel = rowSelectionModel;
	}

	public long[] getSize() {
		return matrix.getSize();
	}

	public void setEntriesGaussian_(double mean, double variance) throws MatrixException {
		new Randn(matrix).calc(Ret.ORIG);
		fireValueChanged();
	}

	public void setEntriesUniform_(double min, double max) throws MatrixException {
		new Rand(matrix).calc(Ret.ORIG);
		fireValueChanged();
	}

	public final Icon getIcon() {
		try {
			TableModel dataModel = new AbstractTableModel() {
				private static final long serialVersionUID = 5562866897873790623L;

				public int getColumnCount() {
					return 1;
				}

				public int getRowCount() {
					return 1;
				}

				public Object getValueAt(int row, int col) {
					return this;
				}
			};
			JTable table = new JTable(dataModel);
			table.getColumnModel().getColumn(0).setWidth(32);
			table.setRowHeight(32);

			int WIDTH = table.getColumnModel().getColumn(0).getWidth() - 1;
			int HEIGHT = table.getRowHeight(0) - 1;

			Class<?> cl = Class.forName("org.jdmp.gui.matrix.MatrixRenderer");
			DefaultTableCellRenderer mr = (DefaultTableCellRenderer) cl.newInstance();
			Component c = mr.getTableCellRendererComponent(table, this, false, false, 0, 0);
			BufferedImage bi = new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_INT_RGB);
			c.paint(bi.getGraphics());
			return new ImageIcon(bi);

		} catch (Exception e) {
			return new ImageIcon("resources/icons/rebuild.png");
		}
	}

	// Description not supported for Matrix
	public String getDescription() {
		return null;
	}

	// Description not supported for Matrix
	public void setDescription(String description) {
	}

	public String toString() {
		if (matrix.getLabel() != null) {
			return "[" + Coordinates.toString(matrix.getSize()) + "] "
					+ matrix.getClass().getSimpleName() + " [" + matrix.getLabel() + "]";
		} else {
			return "[" + Coordinates.toString(matrix.getSize()) + "] "
					+ matrix.getClass().getSimpleName();
		}
	}
}
