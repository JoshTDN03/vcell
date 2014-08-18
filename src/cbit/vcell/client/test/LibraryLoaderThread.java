package cbit.vcell.client.test;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import cbit.vcell.resource.NativeLib;

/**
 * Start and monitor Monitor library loading in thread, show error message when done
 * if exception
 * @author gweatherby
 */
public class LibraryLoaderThread extends Thread {
	private final boolean isGui;
	
	private static Logger lg = Logger.getLogger(LibraryLoaderThread.class);
	
	/**
	 * execute thread
	 * @param hasGui if true, GUI (Swing available)
	 */
	public LibraryLoaderThread(boolean hasGui ) {
		super("LibraryLoader");
		this.isGui = hasGui;
		setDaemon(true);
	}

	@Override
	public void run() {
		char newline = '\n';
		StringBuilder sb = null;
		for (NativeLib librarySet :NativeLib.values( )) {
			try {
				if (lg.isTraceEnabled()) {
					lg.trace("loading " + librarySet);
				}
				librarySet.load( ); //blocks until done
				if (lg.isTraceEnabled()) {
					lg.trace("completed " + librarySet);
				}
			} catch (Exception e) {
				if (sb == null) {
					sb = new StringBuilder("Unable to load runtime libraries for ");
					sb.append(newline);
				}
				sb.append(librarySet);
				sb.append(newline);
			} 
		}
		if (sb != null) {
			if (isGui) {
			if (lg.isEnabledFor(Level.WARN)) {
				lg.warn("scheduling display of " + sb.toString());
			}
			SwingUtilities.invokeLater(new Reporter(sb.toString()));
			}
			else {
				if (lg.isTraceEnabled()) {
					lg.trace("printing error " + sb.toString());
				}
				System.err.println(sb.toString( ));
			}
		}
		lg.debug("library loading complete");
	}
	
	/**
	 * show exception dialog (on Swing thread)
	 */
	private static class Reporter implements Runnable {
		final String msg;

		public Reporter(String msg) {
			this.msg = msg;
		}

		@Override
		public void run() {
			JOptionPane.showMessageDialog(null,msg,"Error loading libraries",JOptionPane.ERROR_MESSAGE);
		}
	}
}
