package org.jdmp.gui.actions;

import javax.swing.Action;
import javax.swing.JComponent;

import org.ujmp.core.interfaces.GUIObject;

public class AboutAction extends ObjectAction {
	private static final long serialVersionUID = 6829231878575873466L;

	public AboutAction(JComponent c, GUIObject o) {
		super(c, o);
		putValue(Action.NAME, "About");
		putValue(Action.SHORT_DESCRIPTION, "Info about JDMP");
	}

	public Object call() {
		return null;
	}

}
