package org.jdmp.gui.actions;

import java.awt.event.ActionEvent;
import java.beans.PropertyChangeListener;
import java.io.Serializable;
import java.util.HashMap;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.Action;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.UIManager;
import javax.swing.event.SwingPropertyChangeSupport;

import org.jdmp.matrix.Matrix;
import org.jdmp.matrix.interfaces.GUIObject;

public abstract class ObjectAction implements Action, Callable<Object>, Serializable {

	protected static final Logger logger = Logger.getLogger(ObjectAction.class.getName());

	public static final int ROW = Matrix.ROW;

	public static final int COLUMN = Matrix.COLUMN;

	public static final int ALL = Matrix.ALL;

	private transient GUIObject object = null;

	private transient JComponent component = null;

	private transient Icon icon = null;

	private boolean enabled = true;

	protected transient SwingPropertyChangeSupport changeSupport;

	private transient HashMap<String, Object> arrayTable = new HashMap<String, Object>();

	public ObjectAction(JComponent c, GUIObject o) {
		setObject(o);
		this.component = c;
		icon = UIManager.getIcon("JDMP.icon." + getClass().getSimpleName());
		// putValue(Action.MNEMONIC_KEY, UIManager.get("JDMP.mnemonicKey." +
		// getClass().getName()));
		// putValue(Action.ACCELERATOR_KEY,
		// UIManager.get("JDMP.acceleratorKey." + getClass().getName()));
	}

	public final void setComponent(JComponent component) {
		this.component = component;
	}

	public final void setStatus(String status) {
		TaskQueue.setStatus(status);
	}

	public final void setProgress(double progress) {
		TaskQueue.setProgress(progress);
	}

	public final String toString() {
		return (String) getValue(Action.NAME) + " (" + getValue(Action.SHORT_DESCRIPTION) + ")";
	}

	public final GUIObject getObject() {
		// if (object == null)
		// object =
		// Workspace.getInstance().getObjectForReference(objectReference);
		return object;
	}

	public final void setObject(GUIObject o) {
		if (o != null) {
			this.object = o;
			// this.objectReference = o.getReference();
		}
	}

	public final void actionPerformed(ActionEvent e) {
		Future<?> f = TaskQueue.submit(this);
		try {
			// f.get();
		} catch (Exception ex) {
			logger.log(Level.WARNING, "error execution action " + e, ex);
		}
	}

	public final Future<?> executeInBackground() {
		Future<?> f = TaskQueue.submit(this);
		return f;
	}

	public abstract Object call() throws Exception;

	public final JComponent getComponent() {
		return component;
	}

	public Object getValue(String key) {
		if (arrayTable == null) {
			return null;
		}

		if ("enabled".equals(key)) {
			return enabled;
		} else if (key == Action.SMALL_ICON) {
			return icon;
		}

		return arrayTable.get(key);
	}

	public void setEnabled(boolean newValue) {
		this.enabled = newValue;
	}

	public boolean isEnabled() {
		return enabled;
	}

	protected void firePropertyChange(String propertyName, Object oldValue, Object newValue) {
		if (changeSupport == null
				|| (oldValue != null && newValue != null && oldValue.equals(newValue))) {
			return;
		}
		changeSupport.firePropertyChange(propertyName, oldValue, newValue);
	}

	public void putValue(String key, Object newValue) {
		Object oldValue = null;
		if (key == "enabled") {
			if (newValue == null || !(newValue instanceof Boolean)) {
				newValue = false;
			}
			oldValue = enabled;
			enabled = (Boolean) newValue;
		} else if (key == Action.SMALL_ICON) {
			oldValue = icon;
			icon = (ImageIcon) newValue;
		} else {
			if (arrayTable.containsKey(key))
				oldValue = arrayTable.get(key);
			if (newValue == null) {
				arrayTable.remove(key);
			} else {
				arrayTable.put(key, newValue);
			}
		}
		firePropertyChange(key, oldValue, newValue);
	}

	public synchronized void addPropertyChangeListener(PropertyChangeListener listener) {
		if (changeSupport == null) {
			changeSupport = new SwingPropertyChangeSupport(this);
		}
		changeSupport.addPropertyChangeListener(listener);
	}

	public synchronized void removePropertyChangeListener(PropertyChangeListener listener) {
		if (changeSupport == null) {
			return;
		}
		changeSupport.removePropertyChangeListener(listener);
	}

}
