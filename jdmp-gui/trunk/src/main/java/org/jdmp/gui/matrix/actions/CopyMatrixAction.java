package org.jdmp.gui.matrix.actions;

import java.awt.event.KeyEvent;

import javax.swing.Action;
import javax.swing.JComponent;
import javax.swing.KeyStroke;

import org.jdmp.gui.matrix.MatrixGUIObject;
import org.ujmp.core.Matrix;
import org.ujmp.core.interfaces.HasMatrixList;

public class CopyMatrixAction extends MatrixAction {
	private static final long serialVersionUID = 6122103774731476379L;

	public CopyMatrixAction(JComponent c, MatrixGUIObject m, HasMatrixList v) {
		super(c, m, v);
		putValue(Action.NAME, "Copy Matrix");
		putValue(Action.SHORT_DESCRIPTION, "copy the content of this matrix to a new matrix");
		putValue(Action.MNEMONIC_KEY, KeyEvent.VK_C);
		putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_C, 0));
	}

	@Override
	public Object call() {
		Matrix m = getMatrixObject().getMatrix().clone();
		m.showGUI();
		return m;
	}

}
