package cbit.vcell.VirtualMicroscopy;


public class ImageLoadingProgress
{
	private double mainBeginProgress;
	private double mainEndProgress;
	private double subProgress;
	private java.beans.PropertyChangeSupport propertyChange;
	
	public void setMainProgressBegin(double mbprog)
	{
		mainBeginProgress = mbprog;
	}
	public void setMainProgressEnd(double meprog)
	{
		mainEndProgress = meprog;
	}
	public void setSubProgress(double sprog)
	{
		subProgress = sprog;
		firePropertyChange("ImageLoadingProgress", null, getProgress());
	}
	public int getProgress()
	{
		double prog = 0;
		prog = (mainBeginProgress + subProgress * (mainEndProgress - mainBeginProgress)) * 100;
		return (int)prog;
	}
	public java.beans.PropertyChangeSupport getPropertyChange() {
		if (propertyChange == null) {
			propertyChange = new java.beans.PropertyChangeSupport(this);
		};
		return propertyChange;
	}
	public void firePropertyChange(String propertyName, Object oldValue, Object newValue) {
		getPropertyChange().firePropertyChange(propertyName, oldValue, newValue);
	}
	
	public synchronized void addPropertyChangeListener(java.beans.PropertyChangeListener listener) {
		getPropertyChange().addPropertyChangeListener(listener);
	}
}
