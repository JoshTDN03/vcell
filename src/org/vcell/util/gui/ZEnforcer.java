/*
 * Copyright (C) 1999-2011 University of Connecticut Health Center
 *
 * Licensed under the MIT License (the "License").
 * You may not use this file except in compliance with the License.
 * You may obtain a copy of the License at:
 *
 *  http://www.opensource.org/licenses/mit-license.php
 */

package org.vcell.util.gui;

import java.awt.Component;
import java.awt.Dialog;
import java.awt.Frame;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.Iterator;
import java.util.Vector;

import javax.swing.JOptionPane;
import javax.swing.Timer;
import org.vcell.util.BeanUtils;

/**
 * Insert the type's description here.
 * Creation date: (5/26/2004 9:04:03 PM)
 * @author: Ion Moraru
 */
public class ZEnforcer {
	private static Vector<Window> windowStack = new Vector<Window>();
	private static WindowAdapter listener = new WindowAdapter() {
		public void windowDeactivated(WindowEvent evt) {
//			if (evt.getWindow() == topWindow()) {
//				evt.getWindow().toFront();
//			}
		}
		public void windowIconified(WindowEvent evt) {
//			if (evt.getWindow() == topWindow()) {
//				evt.getWindow().setVisible(true);
//			}
		}
		public void windowClosed(WindowEvent evt) {
			removeFromStack(evt.getWindow());
		}
	};
	private static ActionListener timerListener = new ActionListener() {
		public void actionPerformed(ActionEvent a) {
			checkWindowStack();
		}
	};
	private static Timer timer = new Timer(100, timerListener);

/**
 * Insert the method's description here.
 * Creation date: (5/26/2004 9:18:41 PM)
 */
private static void checkWindowStack() {
	Iterator<Window> it = windowStack.iterator();
	while (it.hasNext()) {
		Window window = it.next();
		if (! window.isShowing()) {
			window.removeWindowListener(listener);
			it.remove();
			window.dispose();
			Window currentTop = topWindow();
			if (currentTop != null) {
				currentTop.setEnabled(true);
				topWindow().setVisible(true);
			}
		}
	}
	if (windowStack.isEmpty()) {
		timer.stop();
	}
}


/**
 * Insert the method's description here.
 * Creation date: (5/26/2004 9:09:45 PM)
 * @param window java.awt.Window
 */

/*
 Removves this window from our control
 */
 
public static void removeFromStack(Window window) {
	if (window != null && windowStack.contains(window)) {
		window.removeWindowListener(listener);
		windowStack.remove(window);
		window.dispose();
		Window currentTop = topWindow();
		if (currentTop != null) {
			currentTop.setEnabled(true);
			topWindow().setVisible(true);
		}
	}
	if (windowStack.isEmpty()) {
		timer.stop();
	}
}

/**
 * Insert the method's description here.
 * Creation date: (5/26/2004 11:38:45 PM)
 * @param dialog java.awt.Dialog
 */

/*** SO FAR WE ONLY ACCEPT MODAL DIALOGS ***/

public static void showModalDialogOnTop(Dialog dialog, Component toBeCenteredOn) {
	if (toBeCenteredOn == null) {
		System.out.println("ZEnforcer.showModalDialogOnTop(), toBeCenteredOn is null, please try best to fix it!!!");
		Thread.dumpStack();
	}
	
	if (dialog.getParent() == null) {
		System.out.println("ZEnforcer.showModalDialogOnTop(), dialog has no parent, please try best to fix it!!!");
		Thread.dumpStack();
	}

	if (dialog.isModal()) {
		Frame frame = JOptionPane.getFrameForComponent(toBeCenteredOn);
		if (frame != null) {
			frame.setState(Frame.NORMAL);
		}
		showOnTop(dialog, toBeCenteredOn);
	} else {
		try {
			throw new RuntimeException("ERROR - dialog is not modal: " + dialog);
		} catch (Exception exc) {
			exc.printStackTrace(System.out);
		}
		dialog.setVisible(true); // just show it
	}
}


/**
 * Insert the method's description here.
 * Creation date: (5/26/2004 9:09:45 PM)
 * @param dialog java.awt.Window
 */

/*
 Will keep this window on top of all others until it is disposed or specifically removed from control via the remove(Window) method
 Method ignores the request if we have it already in our stack of control windows, since:
 	a. it either is already the window maintained on top, or
 	b. other windows have been requested to be on top after the first request for this one, and have not yet been removed/disposed
 Once window is disposed or removed from control, the previous entry in our stack will stay on top, until the stack is empty
 */
 
private static void showOnTop(Dialog dialog, Component toBeCenteredOn) {
	if (dialog != null && (! windowStack.contains(dialog))) {
		if (topWindow() != null) {
			topWindow().setEnabled(false);
		}
		windowStack.add(dialog);
		dialog.addWindowListener(listener);
		BeanUtils.centerOnComponent(dialog, toBeCenteredOn);
		dialog.setVisible(true);
		timer.start(); // no need to check isRunning(), Timer checks it anyway...
	}
}


/**
 * Insert the method's description here.
 * Creation date: (5/26/2004 9:18:41 PM)
 * @return java.awt.Window
 */
private static Window topWindow() {
	if (windowStack.isEmpty()) {
		return null;
	} else {
		return (Window)windowStack.lastElement();
	}
}
}
