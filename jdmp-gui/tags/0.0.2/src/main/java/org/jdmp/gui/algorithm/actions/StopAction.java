package org.jdmp.gui.algorithm.actions;

import javax.swing.Action;
import javax.swing.JComponent;

import org.jdmp.gui.algorithm.AlgorithmGUIObject;

public class StopAction extends AlgorithmAction {
	private static final long serialVersionUID = 2540510976900188791L;

	public StopAction(JComponent c, AlgorithmGUIObject w) {
		super(c, w);
		putValue(Action.NAME, "Stop");
		putValue(Action.SHORT_DESCRIPTION, "Stop execution of this algorithm");
	}

	public Object call() {
		getAlgorithm().getAlgorithm().stop();
		return null;
	}

}
