/*
 * Copyright (C) 1999-2011 University of Connecticut Health Center
 *
 * Licensed under the MIT License (the "License").
 * You may not use this file except in compliance with the License.
 * You may obtain a copy of the License at:
 *
 *  http://www.opensource.org/licenses/mit-license.php
 */

package cbit.vcell.client.desktop.biomodel;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EventObject;
import java.util.List;

import javax.swing.Timer;

import org.vcell.util.Issue;
import org.vcell.util.Issue.Severity;
import org.vcell.util.IssueContext;
import org.vcell.util.document.VCDocument;

import cbit.vcell.client.desktop.VCDocumentDecorator;
import cbit.vcell.model.SimpleBoundsIssue;

@SuppressWarnings("serial")
public class IssueManager {
	private List<Issue> issueList = Collections.synchronizedList(new ArrayList<Issue>());
	private VCDocument vcDocument = null;
	private int numErrors, numWarnings;
	private long dirtyTimestamp = System.currentTimeMillis();
	private Timer timer = null;
	
	public IssueManager(){
		
		int delay = 1000; //  check each second ... wait 2 seconds after the last dirty
		
		timer = new Timer(delay,new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				try {
					updateIssues0();
				}catch (Exception ex){
					ex.printStackTrace(System.out);
				}
			}
		});
		timer.start();
	}

	public interface IssueEventListener {
		void issueChange(IssueEvent issueEvent);
	}
	public static class IssueEvent extends EventObject {
		private List<Issue> oldValue;
		private List<Issue> newValue;
		public IssueEvent(Object source, List<Issue> oldValue, List<Issue> newValue) {
			super(source);
			this.oldValue = oldValue;
			this.newValue = newValue;
		}
		public final List<Issue> getOldValue() {
			return oldValue;
		}
		public final List<Issue> getNewValue() {
			return newValue;
		}
		
	}
	public void addIssueEventListener(IssueEventListener listener){
		if (issueEventListeners.contains(listener)){
			return;
		}
		this.issueEventListeners.add(listener);
	}
	public void removeIssueEventListener(IssueEventListener listener){
		issueEventListeners.remove(listener);
	}
	void fireIssueEventListener(IssueEvent issueEvent){
		for (IssueEventListener listener : issueEventListeners){
			listener.issueChange(issueEvent);
		}
	}
	
	private List<IssueEventListener> issueEventListeners = new ArrayList<IssueEventListener>();
	
	private void updateIssues0() {
		if (vcDocument==null){
			return;
		}
		if (dirtyTimestamp==0){
			return;
		}
		long elapsedTime = System.currentTimeMillis() - dirtyTimestamp;
		if (elapsedTime<2000) {
			return;
		}
		try {
			VCDocumentDecorator decorator = VCDocumentDecorator.getDecorator(vcDocument);
			numErrors = 0;
			numWarnings = 0;
			ArrayList<Issue> oldIssueList = new ArrayList<Issue>(issueList);
			ArrayList<Issue> tempIssueList = new ArrayList<Issue>();
			IssueContext issueContext = new IssueContext();
			decorator.gatherIssues(issueContext, tempIssueList);
			//vcDocument.gatherIssues(issueContext,tempIssueList);
			
			issueList = new ArrayList<Issue>();
			for (Issue issue: tempIssueList) {
				if (issue instanceof SimpleBoundsIssue) {
					continue;
				}
				issueList.add(issue);
				Severity severity = issue.getSeverity();
				if (severity == Issue.Severity.ERROR) {
					numErrors ++;
				} else if (severity == Issue.Severity.WARNING) {
					numWarnings ++;
				}
			}
			fireIssueEventListener(new IssueEvent(vcDocument, oldIssueList, issueList));
//			System.out.println("\n................... update performed .................." + System.currentTimeMillis());
		} finally {
			dirtyTimestamp = 0;
		}
	}
	public void updateIssues() {
		dirtyTimestamp = System.currentTimeMillis() - 3000; // force update
		updateIssues0();
	}
	public void setVCDocument(VCDocument newValue) {
		if (newValue == vcDocument) {
			return;
		}
		vcDocument = newValue;
		updateIssues();
	}
	public final List<Issue> getIssueList() {
		return issueList;
	}
	public final int getNumErrors() {
		return numErrors;
	}
	public final int getNumWarnings() {
		return numWarnings;
	}
	
	public void setDirty() {
		dirtyTimestamp = System.currentTimeMillis();
	}
	
	@Deprecated
	public static String getHtmlIssueMessage(List<Issue> issueList) {
		return Issue.getHtmlIssueMessage(issueList);
	}
	public VCDocument getVCDocument() {
		return vcDocument;
	}
}
