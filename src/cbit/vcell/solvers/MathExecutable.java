package cbit.vcell.solvers;

/*�
 * (C) Copyright University of Connecticut Health Center 2001.
 * All rights reserved.
�*/

public class MathExecutable extends org.vcell.util.Executable {
	private int currentStringPosition = 0;
	protected transient java.beans.PropertyChangeSupport propertyChange;
	private java.lang.String fieldApplicationMessage = new String();


public MathExecutable(String[] command) {	
	super(command);
}

/**
 * The addPropertyChangeListener method was generated to support the propertyChange field.
 */
public synchronized void addPropertyChangeListener(java.beans.PropertyChangeListener listener) {
	getPropertyChange().addPropertyChangeListener(listener);
}
/**
 * The addPropertyChangeListener method was generated to support the propertyChange field.
 */
public synchronized void addPropertyChangeListener(java.lang.String propertyName, java.beans.PropertyChangeListener listener) {
	getPropertyChange().addPropertyChangeListener(propertyName, listener);
}
/**
 * Insert the method's description here.
 * Creation date: (6/27/01 12:13:37 PM)
 */
private void checkForNewApplicationMessages(String str) {
	//   "[[[msg]]]"
	String START_TOKEN = "[[[";
	String END_TOKEN = "]]]";
	boolean bDone = false;
	while (!bDone){
		int nextMsgBegin = str.indexOf(START_TOKEN,currentStringPosition);
		int nextMsgEnd = str.indexOf(END_TOKEN,currentStringPosition);
		if (nextMsgBegin>=0 && nextMsgEnd > nextMsgBegin){
			String msg = str.substring(nextMsgBegin+START_TOKEN.length(),nextMsgEnd);
			setApplicationMessage(msg);
			currentStringPosition = nextMsgEnd+END_TOKEN.length();
		}else{
			bDone = true;
		}
	}
}
/**
 * The firePropertyChange method was generated to support the propertyChange field.
 */
public void firePropertyChange(java.beans.PropertyChangeEvent evt) {
	getPropertyChange().firePropertyChange(evt);
}
/**
 * The firePropertyChange method was generated to support the propertyChange field.
 */
public void firePropertyChange(java.lang.String propertyName, int oldValue, int newValue) {
	getPropertyChange().firePropertyChange(propertyName, oldValue, newValue);
}
/**
 * The firePropertyChange method was generated to support the propertyChange field.
 */
public void firePropertyChange(java.lang.String propertyName, java.lang.Object oldValue, java.lang.Object newValue) {
	getPropertyChange().firePropertyChange(propertyName, oldValue, newValue); 
}
/**
 * The firePropertyChange method was generated to support the propertyChange field.
 */
public void firePropertyChange(java.lang.String propertyName, boolean oldValue, boolean newValue) {
	getPropertyChange().firePropertyChange(propertyName, oldValue, newValue);
}
/**
 * Gets the applicationMessage property (java.lang.String) value.
 * @return The applicationMessage property value.
 * @see #setApplicationMessage
 */
public java.lang.String getApplicationMessage() {
	return fieldApplicationMessage;
}
/**
 * Accessor for the propertyChange field.
 */
protected java.beans.PropertyChangeSupport getPropertyChange() {
	if (propertyChange == null) {
		propertyChange = new java.beans.PropertyChangeSupport(this);
	};
	return propertyChange;
}
/**
 * The hasListeners method was generated to support the propertyChange field.
 */
public synchronized boolean hasListeners(java.lang.String propertyName) {
	return getPropertyChange().hasListeners(propertyName);
}
public static void main(java.lang.String[] args) {
	try {
		MathExecutable mathExecutable = new MathExecutable(args);
		mathExecutable.start();
	}catch (org.vcell.util.ExecutableException e) {
		System.out.println("\nExecutable Exception thrown, normally handled upstream by other classes...");
	}
}
/**
 * The removePropertyChangeListener method was generated to support the propertyChange field.
 */
public synchronized void removePropertyChangeListener(java.beans.PropertyChangeListener listener) {
	getPropertyChange().removePropertyChangeListener(listener);
}
/**
 * The removePropertyChangeListener method was generated to support the propertyChange field.
 */
public synchronized void removePropertyChangeListener(java.lang.String propertyName, java.beans.PropertyChangeListener listener) {
	getPropertyChange().removePropertyChangeListener(propertyName, listener);
}
/**
 * Sets the applicationMessage property (java.lang.String) value.
 * @param applicationMessage The new value for the property.
 * @see #getApplicationMessage
 */
public void setApplicationMessage(java.lang.String applicationMessage) {
//	System.out.println(">>>>>>>>>>>>>>>>>>>>>>>>>>" + applicationMessage);
	String oldValue = fieldApplicationMessage;
	fieldApplicationMessage = applicationMessage;
	firePropertyChange("applicationMessage", oldValue, applicationMessage);
}
/**
 * Insert the method's description here.
 * Creation date: (10/23/2002 3:30:03 PM)
 * @param newOutputString java.lang.String
 */
protected void setOutputString(String newOutputString) {
	super.setOutputString(newOutputString);
	checkForNewApplicationMessages(getStdoutString());
}
}
