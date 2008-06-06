package org.jdmp.core.util;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

import javax.swing.event.EventListenerList;

public interface CoreObject {

	public EventListenerList getListenerList();

	public String getLongStatus();

	public String getShortStatus();

	public String getLabel();

	public void setLabel(String label);

	public String getDescription();

	public void setDescription(String description);

	public void addPropertyChangeListener(PropertyChangeListener l);

	public void removePropertyChangeListener(PropertyChangeListener l);

	public PropertyChangeSupport getProptertyChangeSupport();

	public void fireValueChanged();

	public boolean isInListenerList(Object listener);

	public void showGUI();
}
