/*
 * Copyright (C) 1999-2011 University of Connecticut Health Center
 *
 * Licensed under the MIT License (the "License").
 * You may not use this file except in compliance with the License.
 * You may obtain a copy of the License at:
 *
 *  http://www.opensource.org/licenses/mit-license.php
 */

package cbit.vcell.client;
import cbit.vcell.client.PopupGenerator;
import cbit.vcell.client.TopLevelWindowManager;
import cbit.vcell.client.UserMessage;

import cbit.util.xml.VCLogger;

import java.util.ArrayList;

import org.vcell.util.UserCancelException;
/**
	* This class represents the otherwise missing link between the GUI layer classes and the XML translation package. 
	* It allows user interaction while importing/exporting a document, like providing extra parameters, or the option to 
 	* cancel the process altogether.
 	* 
 	* For HIGH_PRIORITY messages, the logger halts execution by throwing an exception
 	*  
 	* Creation date: (9/21/2004 10:36:23 AM)
 	* @author: Rashad Badrawi
 */
public class TranslationLogger extends VCLogger {
	  
	private static String OK_OPTION = "OK";
	private static String CANCEL_OPTION = "Cancel";
	private java.awt.Component requester;
	protected ArrayList<String> messages = new ArrayList<String>();

	public TranslationLogger(TopLevelWindowManager topLevelWindow) {
		
		if (topLevelWindow == null) {
			throw new IllegalArgumentException("Invalid top level window");
		}
		this.requester = topLevelWindow.getComponent();
	}


	public TranslationLogger(java.awt.Component requester) {
		this.requester = requester;
	}


	public boolean hasMessages() {
		return messages.size() > 0;
	}


//for now, same for all 
	private void processException(int messageType) throws UserCancelException {

		throw UserCancelException.CANCEL_XML_TRANSLATION;
	}

	public void sendAllMessages() {

		StringBuffer messageBuf = new StringBuffer("The translation process has encountered the following problem(s):\n ");
													//"which can affect the quality of the translation:\n");
		for (int i = 0; i < messages.size(); i++) {
			messageBuf.append(i+1 + ") " + messages.get(i) + "\n");
		}
		UserMessage userMessage = new UserMessage(messageBuf.toString(), new String [] {TranslationLogger.OK_OPTION}, 
			                                      TranslationLogger.OK_OPTION);
		PopupGenerator.showWarningDialog(requester, null, userMessage, null);       //'value' not used.
	}

	public void sendMessage(int messageLevel, int messageType) throws UserCancelException {

		String message = VCLogger.getDefaultMessage(messageType);
		sendMessage(messageLevel, messageType, message);	
	}

	public void sendMessage(int messageLevel, int messageType, String message) throws UserCancelException {

		if (message == null || message.length() == 0 || messageLevel < 0 || messageLevel > 2 || 
			!VCLogger.isValidMessageType(messageType)) {
			throw new IllegalArgumentException("Invalid params for sending translation message.");
		}
		if (messageLevel == TranslationLogger.LOW_PRIORITY || messageLevel == TranslationLogger.MEDIUM_PRIORITY) {
			messages.add(message);
			UserMessage userMessage = new UserMessage(message, new String [] {TranslationLogger.OK_OPTION}, TranslationLogger.OK_OPTION);
			PopupGenerator.showWarningDialog(requester, null, userMessage, null);
		} else if (messageLevel == TranslationLogger.HIGH_PRIORITY) {      
			UserMessage userMessage = new UserMessage(message, new String [] {TranslationLogger.CANCEL_OPTION}, TranslationLogger.CANCEL_OPTION);
			PopupGenerator.showWarningDialog(requester, null, userMessage, null);
			processException(messageType);                                        //regardless of the 'value'
		}
	}
}
