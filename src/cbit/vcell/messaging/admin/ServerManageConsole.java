package cbit.vcell.messaging.admin;
import static cbit.vcell.messaging.admin.ManageConstants.MESSAGE_TYPE_IAMALIVE_VALUE;
import static cbit.vcell.messaging.admin.ManageConstants.MESSAGE_TYPE_ISSERVICEALIVE_VALUE;
import static cbit.vcell.messaging.admin.ManageConstants.MESSAGE_TYPE_PROPERTY;
import static cbit.vcell.messaging.admin.ManageConstants.MESSAGE_TYPE_REFRESHSERVERMANAGER_VALUE;
import static cbit.vcell.messaging.admin.ManageConstants.MESSAGE_TYPE_STARTSERVICE_VALUE;
import static cbit.vcell.messaging.admin.ManageConstants.MESSAGE_TYPE_STOPSERVICE_VALUE;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.ObjectMessage;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.Timer;

import org.vcell.util.BigString;
import org.vcell.util.DataAccessException;
import org.vcell.util.MessageConstants;
import org.vcell.util.MessageConstants.ServiceType;
import org.vcell.util.PropertyLoader;
import org.vcell.util.SessionLog;
import org.vcell.util.StdoutSessionLog;
import org.vcell.util.document.KeyValue;
import org.vcell.util.document.User;
import org.vcell.util.document.VCellServerID;
import org.vcell.util.gui.DateRenderer;
import org.vcell.util.gui.DialogUtils;
import org.vcell.util.gui.sorttable.JSortTable;

import cbit.sql.ConnectionFactory;
import cbit.sql.KeyFactory;
import cbit.vcell.messaging.ControlMessageCollector;
import cbit.vcell.messaging.ControlTopicListener;
import cbit.vcell.messaging.JmsClientMessaging;
import cbit.vcell.messaging.JmsConnection;
import cbit.vcell.messaging.JmsConnectionFactory;
import cbit.vcell.messaging.JmsConnectionFactoryImpl;
import cbit.vcell.messaging.JmsSession;
import cbit.vcell.messaging.JmsUtils;
import cbit.vcell.messaging.db.SimulationJobTable;
import cbit.vcell.messaging.server.RpcDbServerProxy;
import cbit.vcell.messaging.server.RpcSimServerProxy;
import cbit.vcell.modeldb.AdminDBTopLevel;
import cbit.vcell.modeldb.DbDriver;
import cbit.vcell.modeldb.UserTable;
import cbit.vcell.server.ServerInfo;
import cbit.vcell.server.VCellBootstrap;
import cbit.vcell.server.VCellServer;
import cbit.vcell.solver.Simulation;
import cbit.vcell.xml.XmlHelper;

/**
 * Insert the type's description here.
 * Creation date: (8/15/2003 4:19:19 PM)
 * @author: Fei Gao
 */
@SuppressWarnings("serial")
public class ServerManageConsole extends JFrame implements ControlTopicListener {
	private VCellBootstrap vcellBootstrap = null;
	private VCellServer vcellServer = null;
	private SessionLog log = null;
	private List<SimpleUserConnection> userList = Collections.synchronizedList(new LinkedList<SimpleUserConnection>());
	private List<ServiceStatus> serviceConfigList = Collections.synchronizedList(new LinkedList<ServiceStatus>());
	private List<ServiceInstanceStatus> serviceInstanceStatusList = Collections.synchronizedList(new LinkedList<ServiceInstanceStatus>());
	private JPanel ivjJFrameContentPane = null;
	private IvjEventHandler ivjEventHandler = new IvjEventHandler();
	private JmsConnection jmsConn = null;
	private JmsSession topicSession = null;
	private JmsConnectionFactory jmsConnFactory = null;
	private JTabbedPane ivjTabbedPane = null;
	private JPanel ivjServiceStatusPage = null;
	private JPanel ivjConfigPage = null;
	private JSortTable ivjConfigTable = null;
	private JSortTable ivjServiceStatusTable = null;
	private JButton ivjStartServiceButton = null;
	private JButton ivjStopServiceButton = null;
	private JPanel ivjQueryInputPanel = null;
	private JSplitPane ivjJSplitPane1 = null;
	private JPanel ivjQueryPage = null;
	private JSortTable ivjQueryResultTable = null;
	private JPanel ivjQueryStatusPanel = null;
	private JCheckBox ivjQueryCompletedCheck = null;
	private JCheckBox ivjQueryFailedCheck = null;
	private JCheckBox ivjQueryRunningCheck = null;
	private JCheckBox ivjQueryWaitingCheck = null;
	private JTextField ivjQueryHostField = null;
	private JTextField ivjQuerySimField = null;
	private JTextField ivjQueryUserField = null;
	private JCheckBox ivjQueryAllStatusCheck = null;
	private JButton ivjQueryGoButton = null;
	private JButton ivjQueryResetButton = null;
	private AdminDBTopLevel adminDbTop = null;
	private List<JCheckBox> statusChecks = new ArrayList<JCheckBox>();
	private JCheckBox ivjQueryStartDateCheck = null;
	private JCheckBox ivjQuerySubmitDateCheck = null;
	private JLabel ivjNumResultsLabel = null;
	private JLabel ivjNumConfigsLabel = null;
	private JCheckBox ivjQueryEndDateCheck = null;
	private DatePanel ivjQueryEndFromDate = null;
	private DatePanel ivjQueryEndToDate = null;
	private DatePanel ivjQueryStartFromDate = null;
	private DatePanel ivjQuerySubmitToDate = null;
	private DatePanel ivjQueryStartToDate = null;
	private JPanel ivjQuerySubmitDatePanel = null;
	private JPanel ivjQueryEndDatePanel = null;
	private JPanel ivjQueryStartDatePanel = null;
	private DatePanel ivjQuerySubmitFromDate = null;
	private JLabel ivjNumServiceLabel = null;
	private JCheckBox ivjQueryQueuedCheck = null;
	private JCheckBox ivjQueryStoppedCheck = null;
	private JCheckBox ivjQueryDispatchedCheck = null;
	private HashMap<User, RpcDbServerProxy> dbProxyHash = null;
	private HashMap<User, RpcSimServerProxy> simProxyHash = null;
	private JButton ivjExitButton = null;
	private JButton ivjRefreshButton = null;
	private JButton ivjRemoveFromListButton = null;
	private JButton ivjSubmitSelectedButton = null;
	private JTextField ivjQueryServerIDField = null;
	private JPanel ivjUserConnectionPage = null;
	private org.vcell.util.gui.sorttable.JSortTable ivjUserConnectionTable = null;
	private JLabel ivjNumUserConnectionLabel = null;
	private JPanel ivjBroadcastPanel = null;
	private JButton ivjMessageResetButton = null;
	private JButton ivjSendMessageButton = null;
	private JTextArea ivjBroadcastMessageTextArea = null;
	private JTextField ivjBroadcastMessageToTextField = null;
	private JButton ivjNewServiceButton = null;
	private JButton ivjDeleteServiceButton = null;
	private JButton ivjModifyServiceButton = null;
	private JButton ivjRefreshServerManagerButton = null;
	private JProgressBar ivjProgressBar = null;
	private JLabel ivjNumSelectedLabel = null;
	
	private JButton ivjStopSelectedButton = null;
	
	private class IvjEventHandler implements java.awt.event.ActionListener, java.awt.event.ItemListener, java.awt.event.MouseListener, javax.swing.event.ChangeListener {
		public void actionPerformed(java.awt.event.ActionEvent e) {
			try {
				if (e.getSource() == ServerManageConsole.this.getStopServiceButton()) 
					stopServiceButton_ActionPerformed(e);
				if (e.getSource() == ServerManageConsole.this.getStartServiceButton()) 
					startServiceButton_ActionPerformed(e);
				if (e.getSource() == ServerManageConsole.this.getQueryGoButton()) 
					queryGoButton_ActionPerformed(e);
				if (e.getSource() == ServerManageConsole.this.getQueryResetButton()) 
					queryResetButton_ActionPerformed(e);
				if (e.getSource() == ServerManageConsole.this.getRefreshButton()) 
					refreshButton_ActionPerformed(e);
				if (e.getSource() == ServerManageConsole.this.getExitButton()) 
					exitButton_ActionPerformed(e);
				if (e.getSource() == ServerManageConsole.this.getRemoveFromListButton()) 
					removeFromListButton_ActionPerformed(e);
				if (e.getSource() == ServerManageConsole.this.getSubmitSelectedButton()) 
					submitSelectedButton_ActionPerformed(e);
				if (e.getSource() == ServerManageConsole.this.getSendMessageButton()) 
					sendMessageButton_ActionPerformed(e);
				if (e.getSource() == ServerManageConsole.this.getMessageResetButton()) 
					messageResetButton_ActionEvents();
				if (e.getSource() == getNewServiceButton()) {
					newService();
				}
				if (e.getSource() == getDeleteServiceButton()) {
					deleteService();
				}
				if (e.getSource() == getModifyServiceButton()) {
					modifyService();
				}
				if (e.getSource() == getRefreshServerManagerButton()) {
					refreshServerManager();
				}
				if (e.getSource() == ServerManageConsole.this.getStopSelectedButton()) { 
					stopSelectedButton_ActionPerformed(e);
				}
			} catch (java.lang.Throwable ivjExc) {
				handleException(ivjExc);
			}
		};		
		public void itemStateChanged(java.awt.event.ItemEvent e) {
			try {
				if (e.getSource() == ServerManageConsole.this.getQueryWaitingCheck())
					queryWaitingCheck_ItemStateChanged(e);
				if (e.getSource() == ServerManageConsole.this.getQueryQueuedCheck()) 
					queryQueuedCheck_ItemStateChanged(e);
				if (e.getSource() == ServerManageConsole.this.getQueryFailedCheck()) 
					queryFailedCheck_ItemStateChanged(e);
				if (e.getSource() == ServerManageConsole.this.getQueryRunningCheck()) 
					queryRunningCheck_ItemStateChanged(e);
				if (e.getSource() == ServerManageConsole.this.getQueryStoppedCheck()) 
					queryStoppedCheck_ItemStateChanged(e);
				if (e.getSource() == ServerManageConsole.this.getQueryCompletedCheck()) 
					queryCompletedCheck_ItemStateChanged(e);
				if (e.getSource() == ServerManageConsole.this.getQueryAllStatusCheck()) 
					queryAllStatusCheck_ItemStateChanged(e);
				if (e.getSource() == ServerManageConsole.this.getQuerySubmitDateCheck()) 
					querySubmitDateCheck_ItemStateChanged(e);
				if (e.getSource() == ServerManageConsole.this.getQueryStartDateCheck()) 
					queryStartDateCheck_ItemStateChanged(e);
				if (e.getSource() == ServerManageConsole.this.getQueryEndDateCheck()) 
					queryEndDateSubmit_ItemStateChanged(e);
				if (e.getSource() == ServerManageConsole.this.getQueryDispatchedCheck()) 
					queryDispatchedCheck_ItemStateChanged(e);
			} catch (java.lang.Throwable ivjExc) {
				handleException(ivjExc);
			}
		};
		public void mouseClicked(java.awt.event.MouseEvent e) {
			try {
				if (e.getSource() == ServerManageConsole.this.getServiceStatusTable()) 
					serviceStatusTable_MouseClicked(e);
				if (e.getSource() == ServerManageConsole.this.getQueryResultTable()) 
					queryResultTable_MouseClicked(e);
				if (e.getSource() == ServerManageConsole.this.getConfigTable()) {
					configTable_mouseClicked(e);
				}
			} catch (java.lang.Throwable ivjExc) {
				handleException(ivjExc);
			}
		};
		public void mouseEntered(java.awt.event.MouseEvent e) {};
		public void mouseExited(java.awt.event.MouseEvent e) {};
		public void mousePressed(java.awt.event.MouseEvent e) {};
		public void mouseReleased(java.awt.event.MouseEvent e) {};
		public void stateChanged(javax.swing.event.ChangeEvent e) {
			try {
				if (e.getSource() == ServerManageConsole.this.getTabbedPane())
					tabbedPane_ChangeEvents();
			} catch (java.lang.Throwable ivjExc) {
				handleException(ivjExc);
			}
		};
	};

/**
 * ServerManageConsole constructor comment.
 */
public ServerManageConsole() throws java.io.IOException, java.io.FileNotFoundException, org.jdom.JDOMException, javax.jms.JMSException {
	super();
	initialize();
}

void newService() {	
	AddNewServiceDialog dialog = new AddNewServiceDialog(this);
	dialog.setLocationRelativeTo(this);
	dialog.setVisible(true);
	
	if (dialog.isAction()) {
		ServiceSpec ss = dialog.getServiceSpec();
		ServiceStatus config = new ServiceStatus(ss, null, ManageConstants.SERVICE_STATUS_NOTRUNNING, "newly created",	null);
		try {
			config = adminDbTop.insertServiceStatus(config, true);
		} catch (Exception e) {
			e.printStackTrace();
			javax.swing.JOptionPane.showMessageDialog(this, e.getMessage(), "Error", javax.swing.JOptionPane.ERROR_MESSAGE);
		}
		refresh();
	}	
}

private void deleteService() {
	int srow = getConfigTable().getSelectedRow();
	ServiceStatus config = (ServiceStatus)((ServiceStatusTableModel)getConfigTable().getModel()).getValueAt(srow);
	int n = javax.swing.JOptionPane.showConfirmDialog(this, "You are going to delete " + config + ". Continue?", "Confirm", javax.swing.JOptionPane.YES_NO_OPTION);
	if (n == javax.swing.JOptionPane.NO_OPTION) {
		return;
	}
	
	try {
		adminDbTop.deleteServiceStatus(config, true);
		stopService(config.getServiceSpec());
	} catch (Exception e) {
		e.printStackTrace();
		javax.swing.JOptionPane.showMessageDialog(this, e.getMessage(), "Error", javax.swing.JOptionPane.ERROR_MESSAGE);
	}
	refresh();
}

private void modifyService() {	
	int srow = getConfigTable().getSelectedRow();
	ServiceStatus oldConfig = (ServiceStatus)((ServiceStatusTableModel)getConfigTable().getModel()).getValueAt(srow);
	ServiceSpec oldSpec = oldConfig.getServiceSpec();
	
	AddNewServiceDialog dialog = new AddNewServiceDialog(this);
	dialog.modifyService(oldSpec);
	dialog.setLocationRelativeTo(this);
	dialog.setVisible(true);
	
	if (dialog.isAction()) {
		ServiceSpec newSpec = dialog.getServiceSpec();
		if (newSpec.getMemoryMB() == oldSpec.getMemoryMB() && newSpec.getStartupType() == oldSpec.getStartupType()) {
			return;
		}
		ServiceStatus newConfig = new ServiceStatus(newSpec, null, ManageConstants.SERVICE_STATUS_NOTRUNNING, "newly modified",	null);
		try {
			newConfig = adminDbTop.modifyServiceStatus(oldConfig, newConfig, true);
			stopService(newConfig.getServiceSpec());						
		} catch (Exception e) {
			e.printStackTrace();
			javax.swing.JOptionPane.showMessageDialog(this, e.getMessage(), "Error", javax.swing.JOptionPane.ERROR_MESSAGE);
		}
		refresh();
	}
	
}
/**
 * Insert the method's description here.
 * Creation date: (7/6/2004 1:36:54 PM)
 */
private void clearServiceStatusTab() {
	getServiceStatusTable().clearSelection();
	getStopServiceButton().setEnabled(false);
	getStartServiceButton().setEnabled(false);
}

/**
 * Comment
 */
public void exitButton_ActionPerformed(ActionEvent actionEvent) {
	dispose();
	System.exit(0);
	return;
}

/**
 * Return the BroadcastMessageTextArea property value.
 * @return javax.swing.JTextArea
 */
/* WARNING: THIS METHOD WILL BE REGENERATED. */
private javax.swing.JTextArea getBroadcastMessageTextArea() {
	if (ivjBroadcastMessageTextArea == null) {
		try {
			ivjBroadcastMessageTextArea = new javax.swing.JTextArea();
			ivjBroadcastMessageTextArea.setName("BroadcastMessageTextArea");
			ivjBroadcastMessageTextArea.setLineWrap(true);
			ivjBroadcastMessageTextArea.setWrapStyleWord(true);
			ivjBroadcastMessageTextArea.setText("");
			ivjBroadcastMessageTextArea.setFont(new java.awt.Font("Arial", 1, 12));
			ivjBroadcastMessageTextArea.setBounds(0, 0, 376, 68);
			ivjBroadcastMessageTextArea.setMargin(new java.awt.Insets(5, 5, 5, 5));
			// user code begin {1}
			// user code end
		} catch (java.lang.Throwable ivjExc) {
			// user code begin {2}
			// user code end
			handleException(ivjExc);
		}
	}
	return ivjBroadcastMessageTextArea;
}

/**
 * Return the BroadcastMessageToTextField property value.
 * @return javax.swing.JTextField
 */
/* WARNING: THIS METHOD WILL BE REGENERATED. */
private javax.swing.JTextField getBroadcastMessageToTextField() {
	if (ivjBroadcastMessageToTextField == null) {
		try {
			ivjBroadcastMessageToTextField = new javax.swing.JTextField();
			ivjBroadcastMessageToTextField.setName("BroadcastMessageToTextField");
			ivjBroadcastMessageToTextField.setFont(new java.awt.Font("Arial", 1, 12));
			ivjBroadcastMessageToTextField.setText("All");
			ivjBroadcastMessageToTextField.setMargin(new java.awt.Insets(0, 10, 0, 0));
			ivjBroadcastMessageToTextField.setColumns(59);
			// user code begin {1}
			// user code end
		} catch (java.lang.Throwable ivjExc) {
			// user code begin {2}
			// user code end
			handleException(ivjExc);
		}
	}
	return ivjBroadcastMessageToTextField;
}

/**
 * Return the BroadcastPanel property value.
 * @return javax.swing.JPanel
 */
/* WARNING: THIS METHOD WILL BE REGENERATED. */
private javax.swing.JPanel getBroadcastPanel() {
	if (ivjBroadcastPanel == null) {
		try {
			ivjBroadcastPanel = new javax.swing.JPanel();
			ivjBroadcastPanel.setName("BroadcastPanel");
			ivjBroadcastPanel.setPreferredSize(new java.awt.Dimension(495, 500));
			//ivjBroadcastPanel.setLayout(new java.awt.BorderLayout());
			ivjBroadcastPanel.setLayout(new javax.swing.BoxLayout(ivjBroadcastPanel, javax.swing.BoxLayout.Y_AXIS));
			
			JPanel panel14 = new javax.swing.JPanel();
			panel14.setPreferredSize(new java.awt.Dimension(610, 270));
			panel14.setLayout(new java.awt.FlowLayout());			
			JScrollPane scrollPane5 = new javax.swing.JScrollPane();
			scrollPane5.setPreferredSize(new java.awt.Dimension(700, 250));
			scrollPane5.setViewportView(getBroadcastMessageTextArea());			
			panel14.add(scrollPane5);			
			getBroadcastPanel().add(panel14);
			
			JPanel panel13 = new javax.swing.JPanel();
			panel13.setLayout(new java.awt.BorderLayout());
			JPanel panel = new javax.swing.JPanel();
			panel.setLayout(new java.awt.FlowLayout());
			panel.add(getSendMessageButton(), getSendMessageButton().getName());
			panel.add(getMessageResetButton(), getMessageResetButton().getName());			
			panel13.add(panel, "Center");			
			
			panel = new javax.swing.JPanel();
			panel.setLayout(new java.awt.FlowLayout());			
			panel.add(new javax.swing.JLabel("To:      "));
			panel.add(getBroadcastMessageToTextField(), getBroadcastMessageToTextField().getName());			
			panel13.add(panel, "North");			
			getBroadcastPanel().add(panel13);
			
			
			// user code begin {1}
			// user code end
		} catch (java.lang.Throwable ivjExc) {
			// user code begin {2}
			// user code end
			handleException(ivjExc);
		}
	}
	return ivjBroadcastPanel;
}

/**
 * Insert the method's description here.
 * Creation date: (7/19/2004 3:44:01 PM)
 * @return cbit.vcell.messaging.server.RpcDbServerProxy
 */
private RpcDbServerProxy getDbProxy(User user) throws JMSException, DataAccessException, java.rmi.RemoteException {
	if (dbProxyHash == null) {
		dbProxyHash = new HashMap<User, RpcDbServerProxy>();
	}

	RpcDbServerProxy dbProxy = (RpcDbServerProxy)dbProxyHash.get(user);

	if (dbProxy == null) {
		JmsClientMessaging jmsClientMessaging = new JmsClientMessaging(jmsConn, log);		
		dbProxy = new RpcDbServerProxy(user, jmsClientMessaging, log);
		dbProxyHash.put(user, dbProxy);
	}
	
	return dbProxy;
}


/**
 * Return the ExitButton property value.
 * @return javax.swing.JButton
 */
/* WARNING: THIS METHOD WILL BE REGENERATED. */
private javax.swing.JButton getExitButton() {
	if (ivjExitButton == null) {
		try {
			ivjExitButton = new javax.swing.JButton();
			ivjExitButton.setText("Exit");
			ivjExitButton.setForeground(java.awt.Color.red);
			// user code begin {1}
			// user code end
		} catch (java.lang.Throwable ivjExc) {
			// user code begin {2}
			// user code end
			handleException(ivjExc);
		}
	}
	return ivjExitButton;
}

/**
 * Return the JFrameContentPane property value.
 * @return javax.swing.JPanel
 */
/* WARNING: THIS METHOD WILL BE REGENERATED. */
private javax.swing.JPanel getJFrameContentPane() {
	if (ivjJFrameContentPane == null) {
		try {
			ivjJFrameContentPane = new javax.swing.JPanel();
			ivjJFrameContentPane.setName("JFrameContentPane");
			ivjJFrameContentPane.setLayout(new java.awt.BorderLayout());
			getJFrameContentPane().add(getTabbedPane(), "Center");
			
			JPanel panel = new javax.swing.JPanel();			
			panel.setLayout(new FlowLayout(FlowLayout.LEFT));
			panel.add(getExitButton());
			panel.add(getRefreshButton(), getRefreshButton().getName());
			
			final JLabel timeLabel = new JLabel("      ");
			timeLabel.setFont(new Font(timeLabel.getFont().getName(), Font.BOLD, timeLabel.getFont().getSize()));
			Box box = Box.createHorizontalBox();
			box.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createRaisedBevelBorder(), BorderFactory.createLoweredBevelBorder()));
			box.add(panel);
			box.add(Box.createHorizontalGlue());	
			box.add(timeLabel);
			box.add(Box.createHorizontalGlue());
			box.add(getProgressBar());
			final DateFormat df =  new SimpleDateFormat("HH:mm:ss    EEEE    MMM dd, yyyy");
	        new Timer(1000,new ActionListener()
	        {
	            public void actionPerformed(ActionEvent e)
	            {
	                timeLabel.setText(df.format(new Date()));
	            }
	        }).start();			
			
			getJFrameContentPane().add(box, "North");
			// user code begin {1}
			// user code end
		} catch (java.lang.Throwable ivjExc) {
			// user code begin {2}
			// user code end
			handleException(ivjExc);
		}
	}
	return ivjJFrameContentPane;
}

private JProgressBar getProgressBar() {
	if (ivjProgressBar == null) {
		ivjProgressBar = new JProgressBar();
	}
	return ivjProgressBar;
}
/**
 * Return the JPanel3 property value.
 * @return javax.swing.JPanel
 */
/* WARNING: THIS METHOD WILL BE REGENERATED. */
private javax.swing.JPanel getQueryInputPanel() {
	if (ivjQueryInputPanel == null) {
		try {
			ivjQueryInputPanel = new javax.swing.JPanel();
			ivjQueryInputPanel.setName("JPanel3");
			ivjQueryInputPanel.setLayout(new javax.swing.BoxLayout(ivjQueryInputPanel, javax.swing.BoxLayout.Y_AXIS));
			ivjQueryInputPanel.setBounds(0, 0, 160, 120);
			
			JPanel panel4 = new javax.swing.JPanel();
			panel4.setLayout(new FlowLayout(FlowLayout.LEFT));			
			JLabel label3 = new javax.swing.JLabel("Simulation ID");
			label3.setMaximumSize(new java.awt.Dimension(70, 14));
			label3.setPreferredSize(new java.awt.Dimension(70, 14));
			label3.setMinimumSize(new java.awt.Dimension(70, 14));
			label3.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);			
			panel4.add(label3);
			panel4.add(getQuerySimField(), getQuerySimField().getName());			
			ivjQueryInputPanel.add(panel4);
			
			
			JPanel panel9 = new javax.swing.JPanel();
			panel9.setLayout(new FlowLayout(FlowLayout.LEFT));			
			JLabel label = new javax.swing.JLabel("Compute Host");
			label.setMaximumSize(new java.awt.Dimension(70, 14));
			label.setPreferredSize(new java.awt.Dimension(70, 14));
			label.setMinimumSize(new java.awt.Dimension(70, 14));
			label.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);			
			panel9.add(label);
			panel9.add(getQueryHostField(), getQueryHostField().getName());			
			ivjQueryInputPanel.add(panel9);
			
			JPanel panel = new javax.swing.JPanel();
			panel.setLayout(new FlowLayout(FlowLayout.LEFT));
			
			label = new javax.swing.JLabel("Server ID");
			label.setMaximumSize(new java.awt.Dimension(70, 14));
			label.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
			label.setPreferredSize(new java.awt.Dimension(70, 14));
			label.setMinimumSize(new java.awt.Dimension(70, 14));
			label.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
			
			panel.add(label);
			panel.add(getQueryServerIDField(), getQueryServerIDField().getName());
			
			ivjQueryInputPanel.add(panel);
			
			panel = new javax.swing.JPanel();
			panel.setLayout(new FlowLayout(FlowLayout.LEFT));			
			label = new javax.swing.JLabel("User ID");
			label.setMaximumSize(new java.awt.Dimension(70, 14));
			label.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
			label.setPreferredSize(new java.awt.Dimension(70, 14));
			label.setMinimumSize(new java.awt.Dimension(70, 14));
			label.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);			
			panel.add(label);
			panel.add(getQueryUserField(), getQueryUserField().getName());			
			ivjQueryInputPanel.add(panel);
			
			ivjQueryInputPanel.add(getQueryStatusPanel(), getQueryStatusPanel().getName());
			ivjQueryInputPanel.add(getQuerySubmitDatePanel(), getQuerySubmitDatePanel().getName());
			ivjQueryInputPanel.add(getQueryStartDatePanel(), getQueryStartDatePanel().getName());
			ivjQueryInputPanel.add(getQueryEndDatePanel(), getQueryEndDatePanel().getName());
			
			JPanel panel15 = new javax.swing.JPanel();
			panel15.setLayout(new java.awt.FlowLayout());
			panel15.add(getQueryGoButton(), getQueryGoButton().getName());
			panel15.add(getQueryResetButton(), getQueryResetButton().getName());		
			ivjQueryInputPanel.add(panel15);
			// user code begin {1}
			// user code end
		} catch (java.lang.Throwable ivjExc) {
			// user code begin {2}
			// user code end
			handleException(ivjExc);
		}
	}
	return ivjQueryInputPanel;
}

/**
 * Return the JSplitPane1 property value.
 * @return javax.swing.JSplitPane
 */
/* WARNING: THIS METHOD WILL BE REGENERATED. */
private javax.swing.JSplitPane getJSplitPane1() {
	if (ivjJSplitPane1 == null) {
		try {
			ivjJSplitPane1 = new javax.swing.JSplitPane(javax.swing.JSplitPane.HORIZONTAL_SPLIT);
			ivjJSplitPane1.setName("JSplitPane1");
			ivjJSplitPane1.setDividerSize(2);
			ivjJSplitPane1.setLastDividerLocation(1);
			ivjJSplitPane1.setComponentOrientation(java.awt.ComponentOrientation.UNKNOWN);
			ivjJSplitPane1.setDividerLocation(220);

			JScrollPane scrollPane3 = new javax.swing.JScrollPane();
			scrollPane3.setViewportView(getQueryInputPanel());			
			getJSplitPane1().add(scrollPane3, "left");
						
			JPanel panel7 = new javax.swing.JPanel();
			panel7.setLayout(new java.awt.BorderLayout());			
			JPanel panel8 = new javax.swing.JPanel();
			panel8.setLayout(new java.awt.BorderLayout());
			JPanel textPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
			textPanel.add(getNumResultsLabel());
			textPanel.add(new JLabel(" returned    "));
			textPanel.add(getNumSelectedLabel());
			textPanel.add(new JLabel(" selected "));
			panel8.add(textPanel, "West");
			JPanel panel = new JPanel();
			panel.add(getStopSelectedButton());
			panel.add(getSubmitSelectedButton());
			panel.add(getRemoveFromListButton());
			panel8.add(panel, "East");			
			panel7.add(panel8, "North");			
			panel7.add(getQueryResultTable().getEnclosingScrollPane(), "Center");			
			getJSplitPane1().add(panel7, "right");
			// user code begin {1}
			// user code end
		} catch (java.lang.Throwable ivjExc) {
			// user code begin {2}
			// user code end
			handleException(ivjExc);
		}
	}
	return ivjJSplitPane1;
}

/**
 * Insert the method's description here.
 * Creation date: (4/5/2006 10:07:25 AM)
 * @return java.lang.String
 */
private String getLocalVCellBootstrapUrl() {
	String rmiHost = "ms3";
	int rmiPort = PropertyLoader.getIntProperty(PropertyLoader.rmiPortRegistry, 1099);
	return "//" + rmiHost + ":" + rmiPort + "/VCellBootstrapServer";
}


/**
 * Return the MessageResetButton property value.
 * @return javax.swing.JButton
 */
/* WARNING: THIS METHOD WILL BE REGENERATED. */
private javax.swing.JButton getMessageResetButton() {
	if (ivjMessageResetButton == null) {
		try {
			ivjMessageResetButton = new javax.swing.JButton();
			ivjMessageResetButton.setName("MessageResetButton");
			ivjMessageResetButton.setText("Reset");
			// user code begin {1}
			// user code end
		} catch (java.lang.Throwable ivjExc) {
			// user code begin {2}
			// user code end
			handleException(ivjExc);
		}
	}
	return ivjMessageResetButton;
}


/**
 * Return the NumResultsLabel property value.
 * @return javax.swing.JLabel
 */
/* WARNING: THIS METHOD WILL BE REGENERATED. */
private javax.swing.JLabel getNumResultsLabel() {
	if (ivjNumResultsLabel == null) {
		try {
			ivjNumResultsLabel = new javax.swing.JLabel();
			ivjNumResultsLabel.setText("0");
			ivjNumResultsLabel.setForeground(java.awt.Color.red);
			ivjNumResultsLabel.setFont(new Font(ivjNumResultsLabel.getFont().getName(), Font.BOLD, ivjNumResultsLabel.getFont().getSize()));
			// user code begin {1}
			// user code end
		} catch (java.lang.Throwable ivjExc) {
			// user code begin {2}
			// user code end
			handleException(ivjExc);
		}
	}
	return ivjNumResultsLabel;
}

/* WARNING: THIS METHOD WILL BE REGENERATED. */
private javax.swing.JLabel getNumSelectedLabel() {
	if (ivjNumSelectedLabel == null) {
		try {
			ivjNumSelectedLabel = new javax.swing.JLabel();
			ivjNumSelectedLabel.setText("0");
			ivjNumSelectedLabel.setForeground(java.awt.Color.blue);
			ivjNumSelectedLabel.setFont(new Font(ivjNumSelectedLabel.getFont().getName(), Font.BOLD, ivjNumSelectedLabel.getFont().getSize()));
			// user code begin {1}
			// user code end
		} catch (java.lang.Throwable ivjExc) {
			// user code begin {2}
			// user code end
			handleException(ivjExc);
		}
	}
	return ivjNumSelectedLabel;
}

/**
 * Return the NumServiceLabel property value.
 * @return javax.swing.JLabel
 */
/* WARNING: THIS METHOD WILL BE REGENERATED. */
private javax.swing.JLabel getNumServiceLabel() {
	if (ivjNumServiceLabel == null) {
		try {
			ivjNumServiceLabel = new javax.swing.JLabel();
			ivjNumServiceLabel.setName("NumServiceLabel");
			ivjNumServiceLabel.setPreferredSize(new java.awt.Dimension(100, 14));
			ivjNumServiceLabel.setText("0");
			ivjNumServiceLabel.setMaximumSize(new java.awt.Dimension(100, 14));
			ivjNumServiceLabel.setForeground(java.awt.Color.red);
			ivjNumServiceLabel.setMinimumSize(new java.awt.Dimension(100, 14));
			// user code begin {1}
			// user code end
		} catch (java.lang.Throwable ivjExc) {
			// user code begin {2}
			// user code end
			handleException(ivjExc);
		}
	}
	return ivjNumServiceLabel;
}

/**
 * Return the TotalUserConnectionLabel property value.
 * @return javax.swing.JLabel
 */
/* WARNING: THIS METHOD WILL BE REGENERATED. */
private javax.swing.JLabel getNumUserConnectionLabel() {
	if (ivjNumUserConnectionLabel == null) {
		try {
			ivjNumUserConnectionLabel = new javax.swing.JLabel();
			ivjNumUserConnectionLabel.setName("NumUserConnectionLabel");
			ivjNumUserConnectionLabel.setText("0");
			ivjNumUserConnectionLabel.setForeground(java.awt.Color.red);
			// user code begin {1}
			// user code end
		} catch (java.lang.Throwable ivjExc) {
			// user code begin {2}
			// user code end
			handleException(ivjExc);
		}
	}
	return ivjNumUserConnectionLabel;
}

/**
 * Return the QueryAllCheck property value.
 * @return javax.swing.JCheckBox
 */
/* WARNING: THIS METHOD WILL BE REGENERATED. */
private javax.swing.JCheckBox getQueryAllStatusCheck() {
	if (ivjQueryAllStatusCheck == null) {
		try {
			ivjQueryAllStatusCheck = new javax.swing.JCheckBox();
			ivjQueryAllStatusCheck.setName("QueryAllStatusCheck");
			ivjQueryAllStatusCheck.setSelected(true);
			ivjQueryAllStatusCheck.setFont(new java.awt.Font("Arial", 1, 12));
			ivjQueryAllStatusCheck.setText("All");
			// user code begin {1}
			// user code end
		} catch (java.lang.Throwable ivjExc) {
			// user code begin {2}
			// user code end
			handleException(ivjExc);
		}
	}
	return ivjQueryAllStatusCheck;
}

/**
 * Return the JCheckBox7 property value.
 * @return javax.swing.JCheckBox
 */
/* WARNING: THIS METHOD WILL BE REGENERATED. */
private javax.swing.JCheckBox getQueryCompletedCheck() {
	if (ivjQueryCompletedCheck == null) {
		try {
			ivjQueryCompletedCheck = new javax.swing.JCheckBox();
			ivjQueryCompletedCheck.setName("QueryCompletedCheck");
			ivjQueryCompletedCheck.setSelected(true);
			ivjQueryCompletedCheck.setText("Completed");
			// user code begin {1}
			// user code end
		} catch (java.lang.Throwable ivjExc) {
			// user code begin {2}
			// user code end
			handleException(ivjExc);
		}
	}
	return ivjQueryCompletedCheck;
}

/**
 * Return the QueryDispatchedCheckBox property value.
 * @return javax.swing.JCheckBox
 */
/* WARNING: THIS METHOD WILL BE REGENERATED. */
private javax.swing.JCheckBox getQueryDispatchedCheck() {
	if (ivjQueryDispatchedCheck == null) {
		try {
			ivjQueryDispatchedCheck = new javax.swing.JCheckBox();
			ivjQueryDispatchedCheck.setName("QueryDispatchedCheck");
			ivjQueryDispatchedCheck.setSelected(true);
			ivjQueryDispatchedCheck.setText("Dispatched");
			// user code begin {1}
			// user code end
		} catch (java.lang.Throwable ivjExc) {
			// user code begin {2}
			// user code end
			handleException(ivjExc);
		}
	}
	return ivjQueryDispatchedCheck;
}

/**
 * Return the QueryEndDateCheck property value.
 * @return javax.swing.JCheckBox
 */
/* WARNING: THIS METHOD WILL BE REGENERATED. */
private javax.swing.JCheckBox getQueryEndDateCheck() {
	if (ivjQueryEndDateCheck == null) {
		try {
			ivjQueryEndDateCheck = new javax.swing.JCheckBox();
			ivjQueryEndDateCheck.setName("QueryEndDateCheck");
			ivjQueryEndDateCheck.setText("End Between");
			// user code begin {1}
			// user code end
		} catch (java.lang.Throwable ivjExc) {
			// user code begin {2}
			// user code end
			handleException(ivjExc);
		}
	}
	return ivjQueryEndDateCheck;
}

/**
 * Return the QueryEndDatePanel property value.
 * @return javax.swing.JPanel
 */
/* WARNING: THIS METHOD WILL BE REGENERATED. */
private javax.swing.JPanel getQueryEndDatePanel() {
	if (ivjQueryEndDatePanel == null) {
		try {
			ivjQueryEndDatePanel = new javax.swing.JPanel();
			ivjQueryEndDatePanel.setName("QueryEndDatePanel");
			ivjQueryEndDatePanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder()));
			ivjQueryEndDatePanel.setLayout(new GridLayout(3, 0));
			getQueryEndDatePanel().add(getQueryEndDateCheck(), getQueryEndDateCheck().getName());
			getQueryEndDatePanel().add(getQueryEndFromDate(), getQueryEndFromDate().getName());
			getQueryEndDatePanel().add(getQueryEndToDate(), getQueryEndToDate().getName());
			// user code begin {1}
			// user code end
		} catch (java.lang.Throwable ivjExc) {
			// user code begin {2}
			// user code end
			handleException(ivjExc);
		}
	}
	return ivjQueryEndDatePanel;
}

/**
 * Return the DatePanel3 property value.
 * @return cbit.vcell.messaging.admin.DatePanel
 */
/* WARNING: THIS METHOD WILL BE REGENERATED. */
private DatePanel getQueryEndFromDate() {
	if (ivjQueryEndFromDate == null) {
		try {
			ivjQueryEndFromDate = new DatePanel();
			ivjQueryEndFromDate.setName("QueryEndFromDate");
			ivjQueryEndFromDate.setLayout(new FlowLayout());
			ivjQueryEndFromDate.setEnabled(false);
			// user code begin {1}
			// user code end
		} catch (java.lang.Throwable ivjExc) {
			// user code begin {2}
			// user code end
			handleException(ivjExc);
		}
	}
	return ivjQueryEndFromDate;
}

/**
 * Return the QueryEndToDate property value.
 * @return cbit.vcell.messaging.admin.DatePanel
 */
/* WARNING: THIS METHOD WILL BE REGENERATED. */
private DatePanel getQueryEndToDate() {
	if (ivjQueryEndToDate == null) {
		try {
			ivjQueryEndToDate = new DatePanel();
			ivjQueryEndToDate.setName("QueryEndToDate");
			ivjQueryEndToDate.setLayout(new FlowLayout());
			ivjQueryEndToDate.setEnabled(false);
			// user code begin {1}
			// user code end
		} catch (java.lang.Throwable ivjExc) {
			// user code begin {2}
			// user code end
			handleException(ivjExc);
		}
	}
	return ivjQueryEndToDate;
}

/**
 * Return the JCheckBox4 property value.
 * @return javax.swing.JCheckBox
 */
/* WARNING: THIS METHOD WILL BE REGENERATED. */
private javax.swing.JCheckBox getQueryFailedCheck() {
	if (ivjQueryFailedCheck == null) {
		try {
			ivjQueryFailedCheck = new javax.swing.JCheckBox();
			ivjQueryFailedCheck.setName("QueryFailedCheck");
			ivjQueryFailedCheck.setSelected(true);
			ivjQueryFailedCheck.setText("Failed");
			// user code begin {1}
			// user code end
		} catch (java.lang.Throwable ivjExc) {
			// user code begin {2}
			// user code end
			handleException(ivjExc);
		}
	}
	return ivjQueryFailedCheck;
}

/**
 * Return the JButton1 property value.
 * @return javax.swing.JButton
 */
/* WARNING: THIS METHOD WILL BE REGENERATED. */
private javax.swing.JButton getQueryGoButton() {
	if (ivjQueryGoButton == null) {
		try {
			ivjQueryGoButton = new javax.swing.JButton();
			ivjQueryGoButton.setName("QueryGoButton");
			ivjQueryGoButton.setText("Go!");
			// user code begin {1}
			// user code end
		} catch (java.lang.Throwable ivjExc) {
			// user code begin {2}
			// user code end
			handleException(ivjExc);
		}
	}
	return ivjQueryGoButton;
}

/**
 * Return the JTextField2 property value.
 * @return javax.swing.JTextField
 */
/* WARNING: THIS METHOD WILL BE REGENERATED. */
private javax.swing.JTextField getQueryHostField() {
	if (ivjQueryHostField == null) {
		try {
			ivjQueryHostField = new javax.swing.JTextField();
			ivjQueryHostField.setName("QueryHostField");
			ivjQueryHostField.setColumns(13);
			// user code begin {1}
			// user code end
		} catch (java.lang.Throwable ivjExc) {
			// user code begin {2}
			// user code end
			handleException(ivjExc);
		}
	}
	return ivjQueryHostField;
}

/**
 * Return the QueryPage property value.
 * @return javax.swing.JPanel
 */
/* WARNING: THIS METHOD WILL BE REGENERATED. */
private javax.swing.JPanel getQueryPage() {
	if (ivjQueryPage == null) {
		try {
			ivjQueryPage = new javax.swing.JPanel();
			ivjQueryPage.setName("QueryPage");
			ivjQueryPage.setLayout(new java.awt.BorderLayout());
			getQueryPage().add(getJSplitPane1(), "Center");
			// user code begin {1}
			// user code end
		} catch (java.lang.Throwable ivjExc) {
			// user code begin {2}
			// user code end
			handleException(ivjExc);
		}
	}
	return ivjQueryPage;
}


/**
 * Return the JCheckBox2 property value.
 * @return javax.swing.JCheckBox
 */
/* WARNING: THIS METHOD WILL BE REGENERATED. */
private javax.swing.JCheckBox getQueryQueuedCheck() {
	if (ivjQueryQueuedCheck == null) {
		try {
			ivjQueryQueuedCheck = new javax.swing.JCheckBox();
			ivjQueryQueuedCheck.setName("QueryQueuedCheck");
			ivjQueryQueuedCheck.setSelected(true);
			ivjQueryQueuedCheck.setText("Queued");
			// user code begin {1}
			// user code end
		} catch (java.lang.Throwable ivjExc) {
			// user code begin {2}
			// user code end
			handleException(ivjExc);
		}
	}
	return ivjQueryQueuedCheck;
}

/**
 * Return the JButton2 property value.
 * @return javax.swing.JButton
 */
/* WARNING: THIS METHOD WILL BE REGENERATED. */
private javax.swing.JButton getQueryResetButton() {
	if (ivjQueryResetButton == null) {
		try {
			ivjQueryResetButton = new javax.swing.JButton();
			ivjQueryResetButton.setName("QueryResetButton");
			ivjQueryResetButton.setText("Reset");
			// user code begin {1}
			// user code end
		} catch (java.lang.Throwable ivjExc) {
			// user code begin {2}
			// user code end
			handleException(ivjExc);
		}
	}
	return ivjQueryResetButton;
}

/**
 * Return the QueryResultTable property value.
 * @return cbit.vcell.messaging.admin.sorttable.JSortTable
 */
/* WARNING: THIS METHOD WILL BE REGENERATED. */
private JSortTable getQueryResultTable() {
	if (ivjQueryResultTable == null) {
		try {
			ivjQueryResultTable = new JSortTable();
			ivjQueryResultTable.setName("QueryResultTable");
			ivjQueryResultTable.setModel(new JobTableModel());
			ivjQueryResultTable.disableUneditableForeground();
			// user code begin {1}
			// user code end
		} catch (java.lang.Throwable ivjExc) {
			// user code begin {2}
			// user code end
			handleException(ivjExc);
		}
	}
	return ivjQueryResultTable;
}


/**
 * Return the JCheckBox1 property value.
 * @return javax.swing.JCheckBox
 */
/* WARNING: THIS METHOD WILL BE REGENERATED. */
private javax.swing.JCheckBox getQueryRunningCheck() {
	if (ivjQueryRunningCheck == null) {
		try {
			ivjQueryRunningCheck = new javax.swing.JCheckBox();
			ivjQueryRunningCheck.setName("QueryRunningCheck");
			ivjQueryRunningCheck.setSelected(true);
			ivjQueryRunningCheck.setText("Running");
			// user code begin {1}
			// user code end
		} catch (java.lang.Throwable ivjExc) {
			// user code begin {2}
			// user code end
			handleException(ivjExc);
		}
	}
	return ivjQueryRunningCheck;
}

/**
 * Return the QueryServerIDField property value.
 * @return javax.swing.JTextField
 */
/* WARNING: THIS METHOD WILL BE REGENERATED. */
private javax.swing.JTextField getQueryServerIDField() {
	if (ivjQueryServerIDField == null) {
		try {
			ivjQueryServerIDField = new javax.swing.JTextField();
			ivjQueryServerIDField.setName("QueryServerIDField");
			ivjQueryServerIDField.setColumns(13);
			// user code begin {1}
			// user code end
		} catch (java.lang.Throwable ivjExc) {
			// user code begin {2}
			// user code end
			handleException(ivjExc);
		}
	}
	return ivjQueryServerIDField;
}


/**
 * Return the JTextField1 property value.
 * @return javax.swing.JTextField
 */
/* WARNING: THIS METHOD WILL BE REGENERATED. */
private javax.swing.JTextField getQuerySimField() {
	if (ivjQuerySimField == null) {
		try {
			ivjQuerySimField = new javax.swing.JTextField();
			ivjQuerySimField.setName("QuerySimField");
			ivjQuerySimField.setColumns(13);
			// user code begin {1}
			// user code end
		} catch (java.lang.Throwable ivjExc) {
			// user code begin {2}
			// user code end
			handleException(ivjExc);
		}
	}
	return ivjQuerySimField;
}

/**
 * Return the QueryStartDateCheck property value.
 * @return javax.swing.JCheckBox
 */
/* WARNING: THIS METHOD WILL BE REGENERATED. */
private javax.swing.JCheckBox getQueryStartDateCheck() {
	if (ivjQueryStartDateCheck == null) {
		try {
			ivjQueryStartDateCheck = new javax.swing.JCheckBox();
			ivjQueryStartDateCheck.setName("QueryStartDateCheck");
			ivjQueryStartDateCheck.setText("Start Between");
			ivjQueryStartDateCheck.setHorizontalTextPosition(javax.swing.SwingConstants.RIGHT);
			// user code begin {1}
			// user code end
		} catch (java.lang.Throwable ivjExc) {
			// user code begin {2}
			// user code end
			handleException(ivjExc);
		}
	}
	return ivjQueryStartDateCheck;
}

/**
 * Return the QueryStartDatePanel property value.
 * @return javax.swing.JPanel
 */
/* WARNING: THIS METHOD WILL BE REGENERATED. */
private javax.swing.JPanel getQueryStartDatePanel() {
	if (ivjQueryStartDatePanel == null) {
		try {
			ivjQueryStartDatePanel = new javax.swing.JPanel();
			ivjQueryStartDatePanel.setName("QueryStartDatePanel");
			ivjQueryStartDatePanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder()));
			ivjQueryStartDatePanel.setLayout(new java.awt.GridLayout(3, 0));
			getQueryStartDatePanel().add(getQueryStartDateCheck(), getQueryStartDateCheck().getName());
			getQueryStartDatePanel().add(getQueryStartFromDate(), getQueryStartFromDate().getName());
			getQueryStartDatePanel().add(getQueryStartToDate(), getQueryStartToDate().getName());
			// user code begin {1}
			// user code end
		} catch (java.lang.Throwable ivjExc) {
			// user code begin {2}
			// user code end
			handleException(ivjExc);
		}
	}
	return ivjQueryStartDatePanel;
}

/**
 * Return the DatePanel2 property value.
 * @return cbit.vcell.messaging.admin.DatePanel
 */
/* WARNING: THIS METHOD WILL BE REGENERATED. */
private DatePanel getQueryStartFromDate() {
	if (ivjQueryStartFromDate == null) {
		try {
			ivjQueryStartFromDate = new DatePanel();
			ivjQueryStartFromDate.setName("QueryStartFromDate");
			ivjQueryStartFromDate.setLayout(new FlowLayout());
			ivjQueryStartFromDate.setEnabled(false);
			// user code begin {1}
			// user code end
		} catch (java.lang.Throwable ivjExc) {
			// user code begin {2}
			// user code end
			handleException(ivjExc);
		}
	}
	return ivjQueryStartFromDate;
}

/**
 * Return the DatePanel1 property value.
 * @return cbit.vcell.messaging.admin.DatePanel
 */
/* WARNING: THIS METHOD WILL BE REGENERATED. */
private DatePanel getQueryStartToDate() {
	if (ivjQueryStartToDate == null) {
		try {
			ivjQueryStartToDate = new DatePanel();
			ivjQueryStartToDate.setName("QueryStartToDate");
			ivjQueryStartToDate.setLayout(new FlowLayout());
			ivjQueryStartToDate.setEnabled(false);
			// user code begin {1}
			// user code end
		} catch (java.lang.Throwable ivjExc) {
			// user code begin {2}
			// user code end
			handleException(ivjExc);
		}
	}
	return ivjQueryStartToDate;
}

/**
 * Return the QueryStatusPanel property value.
 * @return javax.swing.JPanel
 */
/* WARNING: THIS METHOD WILL BE REGENERATED. */
private javax.swing.JPanel getQueryStatusPanel() {
	if (ivjQueryStatusPanel == null) {
		try {
			ivjQueryStatusPanel = new javax.swing.JPanel();
			ivjQueryStatusPanel.setName("QueryStatusPanel");
			ivjQueryStatusPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), "Status"));
			ivjQueryStatusPanel.setLayout(new java.awt.GridLayout(0, 2));
			getQueryStatusPanel().add(getQueryWaitingCheck(), getQueryWaitingCheck().getName());
			getQueryStatusPanel().add(getQueryQueuedCheck(), getQueryQueuedCheck().getName());
			getQueryStatusPanel().add(getQueryDispatchedCheck(), getQueryDispatchedCheck().getName());
			getQueryStatusPanel().add(getQueryRunningCheck(), getQueryRunningCheck().getName());
			getQueryStatusPanel().add(getQueryCompletedCheck(), getQueryCompletedCheck().getName());
			getQueryStatusPanel().add(getQueryFailedCheck(), getQueryFailedCheck().getName());
			getQueryStatusPanel().add(getQueryStoppedCheck(), getQueryStoppedCheck().getName());
			getQueryStatusPanel().add(getQueryAllStatusCheck(), getQueryAllStatusCheck().getName());
			// user code begin {1}
			// user code end
		} catch (java.lang.Throwable ivjExc) {
			// user code begin {2}
			// user code end
			handleException(ivjExc);
		}
	}
	return ivjQueryStatusPanel;
}

/**
 * Return the JCheckBox6 property value.
 * @return javax.swing.JCheckBox
 */
/* WARNING: THIS METHOD WILL BE REGENERATED. */
private javax.swing.JCheckBox getQueryStoppedCheck() {
	if (ivjQueryStoppedCheck == null) {
		try {
			ivjQueryStoppedCheck = new javax.swing.JCheckBox();
			ivjQueryStoppedCheck.setName("QueryStoppedCheck");
			ivjQueryStoppedCheck.setSelected(true);
			ivjQueryStoppedCheck.setText("Stopped");
			// user code begin {1}
			// user code end
		} catch (java.lang.Throwable ivjExc) {
			// user code begin {2}
			// user code end
			handleException(ivjExc);
		}
	}
	return ivjQueryStoppedCheck;
}

/**
 * Return the QuerySubmitDateCheck property value.
 * @return javax.swing.JCheckBox
 */
/* WARNING: THIS METHOD WILL BE REGENERATED. */
private javax.swing.JCheckBox getQuerySubmitDateCheck() {
	if (ivjQuerySubmitDateCheck == null) {
		try {
			ivjQuerySubmitDateCheck = new javax.swing.JCheckBox();
			ivjQuerySubmitDateCheck.setName("QuerySubmitDateCheck");
			ivjQuerySubmitDateCheck.setSelected(true);
			ivjQuerySubmitDateCheck.setText("Submit Between");
			// user code begin {1}
			// user code end
		} catch (java.lang.Throwable ivjExc) {
			// user code begin {2}
			// user code end
			handleException(ivjExc);
		}
	}
	return ivjQuerySubmitDateCheck;
}

/**
 * Return the QuerySubmitDatePanel property value.
 * @return javax.swing.JPanel
 */
/* WARNING: THIS METHOD WILL BE REGENERATED. */
private javax.swing.JPanel getQuerySubmitDatePanel() {
	if (ivjQuerySubmitDatePanel == null) {
		try {
			ivjQuerySubmitDatePanel = new javax.swing.JPanel();
			ivjQuerySubmitDatePanel.setName("QuerySubmitDatePanel");
			ivjQuerySubmitDatePanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder()));
			ivjQuerySubmitDatePanel.setLayout(new java.awt.GridLayout(3, 0));
			getQuerySubmitDatePanel().add(getQuerySubmitDateCheck(), getQuerySubmitDateCheck().getName());
			getQuerySubmitDatePanel().add(getQuerySubmitFromDate(), getQuerySubmitFromDate().getName());
			getQuerySubmitDatePanel().add(getQuerySubmitToDate(), getQuerySubmitToDate().getName());
			// user code begin {1}
			// user code end
		} catch (java.lang.Throwable ivjExc) {
			// user code begin {2}
			// user code end
			handleException(ivjExc);
		}
	}
	return ivjQuerySubmitDatePanel;
}

/**
 * Return the DatePanel1 property value.
 * @return cbit.vcell.messaging.admin.DatePanel
 */
/* WARNING: THIS METHOD WILL BE REGENERATED. */
private DatePanel getQuerySubmitFromDate() {
	if (ivjQuerySubmitFromDate == null) {
		try {
			ivjQuerySubmitFromDate = new DatePanel();
			ivjQuerySubmitFromDate.setName("QuerySubmitFromDate");
			ivjQuerySubmitFromDate.setLayout(new FlowLayout());
			ivjQuerySubmitFromDate.setEnabled(true);
			// user code begin {1}
			// user code end
		} catch (java.lang.Throwable ivjExc) {
			// user code begin {2}
			// user code end
			handleException(ivjExc);
		}
	}
	return ivjQuerySubmitFromDate;
}

/**
 * Return the QuerySubmitToDate property value.
 * @return cbit.vcell.messaging.admin.DatePanel
 */
/* WARNING: THIS METHOD WILL BE REGENERATED. */
private DatePanel getQuerySubmitToDate() {
	if (ivjQuerySubmitToDate == null) {
		try {
			ivjQuerySubmitToDate = new DatePanel();
			ivjQuerySubmitToDate.setName("QuerySubmitToDate");
			ivjQuerySubmitToDate.setLayout(new FlowLayout());
			ivjQuerySubmitToDate.setEnabled(true);
			// user code begin {1}
			// user code end
		} catch (java.lang.Throwable ivjExc) {
			// user code begin {2}
			// user code end
			handleException(ivjExc);
		}
	}
	return ivjQuerySubmitToDate;
}

/**
 * Return the JTextField3 property value.
 * @return javax.swing.JTextField
 */
/* WARNING: THIS METHOD WILL BE REGENERATED. */
private javax.swing.JTextField getQueryUserField() {
	if (ivjQueryUserField == null) {
		try {
			ivjQueryUserField = new javax.swing.JTextField();
			ivjQueryUserField.setName("QueryUserField");
			ivjQueryUserField.setColumns(13);
			// user code begin {1}
			// user code end
		} catch (java.lang.Throwable ivjExc) {
			// user code begin {2}
			// user code end
			handleException(ivjExc);
		}
	}
	return ivjQueryUserField;
}

/**
 * Return the JCheckBox3 property value.
 * @return javax.swing.JCheckBox
 */
/* WARNING: THIS METHOD WILL BE REGENERATED. */
private javax.swing.JCheckBox getQueryWaitingCheck() {
	if (ivjQueryWaitingCheck == null) {
		try {
			ivjQueryWaitingCheck = new javax.swing.JCheckBox();
			ivjQueryWaitingCheck.setName("QueryWaitingCheck");
			ivjQueryWaitingCheck.setSelected(true);
			ivjQueryWaitingCheck.setText("Waiting");
			// user code begin {1}
			// user code end
		} catch (java.lang.Throwable ivjExc) {
			// user code begin {2}
			// user code end
			handleException(ivjExc);
		}
	}
	return ivjQueryWaitingCheck;
}

/**
 * Return the RefreshButton property value.
 * @return javax.swing.JButton
 */
/* WARNING: THIS METHOD WILL BE REGENERATED. */
private javax.swing.JButton getRefreshButton() {
	if (ivjRefreshButton == null) {
		try {
			ivjRefreshButton = new javax.swing.JButton();
			ivjRefreshButton.setName("RefreshButton");
			ivjRefreshButton.setText("Refresh");
			ivjRefreshButton.setForeground(java.awt.Color.blue);
			// user code begin {1}
			// user code end
		} catch (java.lang.Throwable ivjExc) {
			// user code begin {2}
			// user code end
			handleException(ivjExc);
		}
	}
	return ivjRefreshButton;
}

/**
 * Return the RemoveFromListButton property value.
 * @return javax.swing.JButton
 */
/* WARNING: THIS METHOD WILL BE REGENERATED. */
private javax.swing.JButton getRemoveFromListButton() {
	if (ivjRemoveFromListButton == null) {
		try {
			ivjRemoveFromListButton = new javax.swing.JButton();
			ivjRemoveFromListButton.setName("RemoveFromListButton");
			ivjRemoveFromListButton.setText("Remove From List");
			ivjRemoveFromListButton.setEnabled(false);
			// user code begin {1}
			// user code end
		} catch (java.lang.Throwable ivjExc) {
			// user code begin {2}
			// user code end
			handleException(ivjExc);
		}
	}
	return ivjRemoveFromListButton;
}


/**
 * Return the RemoveFromListButton property value.
 * @return javax.swing.JButton
 */
/* WARNING: THIS METHOD WILL BE REGENERATED. */
private javax.swing.JButton getSubmitSelectedButton() {
	if (ivjSubmitSelectedButton == null) {
		try {
			ivjSubmitSelectedButton = new javax.swing.JButton();
			ivjSubmitSelectedButton.setName("SubmitSelected");
			ivjSubmitSelectedButton.setText("Submit selected jobs");
			ivjSubmitSelectedButton.setEnabled(false);
			// user code begin {1}
			// user code end
		} catch (java.lang.Throwable ivjExc) {
			// user code begin {2}
			// user code end
			handleException(ivjExc);
		}
	}
	return ivjSubmitSelectedButton;
}

private javax.swing.JButton getStopSelectedButton() {
	if (ivjStopSelectedButton == null) {
		try {
			ivjStopSelectedButton = new javax.swing.JButton();
			ivjStopSelectedButton.setText("Stop selected jobs");
			ivjStopSelectedButton.setEnabled(false);
			// user code begin {1}
			// user code end
		} catch (java.lang.Throwable ivjExc) {
			// user code begin {2}
			// user code end
			handleException(ivjExc);
		}
	}
	return ivjStopSelectedButton;
}

/**
 * Return the SendMessageButton property value.
 * @return javax.swing.JButton
 */
/* WARNING: THIS METHOD WILL BE REGENERATED. */
private javax.swing.JButton getSendMessageButton() {
	if (ivjSendMessageButton == null) {
		try {
			ivjSendMessageButton = new javax.swing.JButton();
			ivjSendMessageButton.setName("SendMessageButton");
			ivjSendMessageButton.setText("Send");
			// user code begin {1}
			// user code end
		} catch (java.lang.Throwable ivjExc) {
			// user code begin {2}
			// user code end
			handleException(ivjExc);
		}
	}
	return ivjSendMessageButton;
}

/**
 * Return the ServicePage property value.
 * @return javax.swing.JPanel
 */
/* WARNING: THIS METHOD WILL BE REGENERATED. */
private javax.swing.JPanel getServiceStatusPage() {
	if (ivjServiceStatusPage == null) {
		try {
			ivjServiceStatusPage = new javax.swing.JPanel();
			ivjServiceStatusPage.setName("ServiceStatusPage");
			ivjServiceStatusPage.setLayout(new java.awt.BorderLayout());
			
			JPanel panel = new javax.swing.JPanel();
			panel.setLayout(new FlowLayout(FlowLayout.LEFT));			
			JLabel label = new javax.swing.JLabel("Total Services:");
			label.setForeground(java.awt.Color.red);			
			panel.add(label);
			panel.add(getNumServiceLabel());
			ivjServiceStatusPage.add(panel, "North");
			
			ivjServiceStatusPage.add(getServiceStatusTable().getEnclosingScrollPane(), "Center");
			
			JPanel panel6 = new javax.swing.JPanel();
			panel6.setLayout(new java.awt.FlowLayout());
			panel6.add(getStartServiceButton(), getStartServiceButton().getName());
			panel6.add(getStopServiceButton(), getStopServiceButton().getName());	
	
			ivjServiceStatusPage.add(panel6, "South");
			// user code begin {1}
			// user code end
		} catch (java.lang.Throwable ivjExc) {
			// user code begin {2}
			// user code end
			handleException(ivjExc);
		}
	}
	return ivjServiceStatusPage;
}

/**
 * Return the ServiceStatusTable property value.
 * @return cbit.vcell.messaging.admin.sorttable.JSortTable
 */
private JSortTable getConfigTable() {
	if (ivjConfigTable == null) {
		try {
			ivjConfigTable = new JSortTable();
			ivjConfigTable.setModel(new ServiceStatusTableModel());
			ivjConfigTable.disableUneditableForeground();
		} catch (java.lang.Throwable ivjExc) {
			handleException(ivjExc);
		}
	}
	return ivjConfigTable;
}

/**
 * Return the ServiceStatusTable property value.
 * @return cbit.vcell.messaging.admin.sorttable.JSortTable
 */
/* WARNING: THIS METHOD WILL BE REGENERATED. */
private JSortTable getServiceStatusTable() {
	if (ivjServiceStatusTable == null) {
		try {
			ivjServiceStatusTable = new JSortTable();
			ivjServiceStatusTable.setModel(new ServiceInstanceStatusTableModel());
			ivjServiceStatusTable.disableUneditableForeground();
			//ivjServiceStatusTable.setBounds(0, 0, 200, 200);
			// user code begin {1}
			// user code end
		} catch (java.lang.Throwable ivjExc) {
			// user code begin {2}
			// user code end
			handleException(ivjExc);
		}
	}
	return ivjServiceStatusTable;
}


/**
 * Insert the method's description here.
 * Creation date: (7/19/2004 3:44:01 PM)
 * @return cbit.vcell.messaging.server.RpcsimServerProxy
 */
private RpcSimServerProxy getSimProxy(User user) throws JMSException, DataAccessException, java.rmi.RemoteException {
	if (simProxyHash == null) {
		simProxyHash = new HashMap<User, RpcSimServerProxy>();
	}

	RpcSimServerProxy simProxy = (RpcSimServerProxy)simProxyHash.get(user);

	if (simProxy == null) {
		JmsClientMessaging jmsClientMessaging = new JmsClientMessaging(jmsConn, log);		
		simProxy = new RpcSimServerProxy(user, jmsClientMessaging, log);
		simProxyHash.put(user, simProxy);
	}
	
	return simProxy;
}

/**
 * Return the StartServiceButton property value.
 * @return javax.swing.JButton
 */
/* WARNING: THIS METHOD WILL BE REGENERATED. */
private javax.swing.JButton getStartServiceButton() {
	if (ivjStartServiceButton == null) {
		try {
			ivjStartServiceButton = new javax.swing.JButton();
			ivjStartServiceButton.setName("StartServiceButton");
			ivjStartServiceButton.setText("Start Service");
			ivjStartServiceButton.setEnabled(false);
			// user code begin {1}
			// user code end
		} catch (java.lang.Throwable ivjExc) {
			// user code begin {2}
			// user code end
			handleException(ivjExc);
		}
	}
	return ivjStartServiceButton;
}

/**
 * Return the StopServiceButton property value.
 * @return javax.swing.JButton
 */
/* WARNING: THIS METHOD WILL BE REGENERATED. */
private javax.swing.JButton getStopServiceButton() {
	if (ivjStopServiceButton == null) {
		try {
			ivjStopServiceButton = new javax.swing.JButton();
			ivjStopServiceButton.setName("StopServiceButton");
			ivjStopServiceButton.setText("Stop Service");
			ivjStopServiceButton.setEnabled(false);
			// user code begin {1}
			// user code end
		} catch (java.lang.Throwable ivjExc) {
			// user code begin {2}
			// user code end
			handleException(ivjExc);
		}
	}
	return ivjStopServiceButton;
}

/**
 * Return the JTabbedPane1 property value.
 * @return javax.swing.JTabbedPane
 */
/* WARNING: THIS METHOD WILL BE REGENERATED. */
private javax.swing.JTabbedPane getTabbedPane() {
	if (ivjTabbedPane == null) {
		try {
			ivjTabbedPane = new javax.swing.JTabbedPane();
			ivjTabbedPane.setName("TabbedPane");			
			ivjTabbedPane.insertTab("Configurations", null, getConfigPage(), "services in the database", 0);
			ivjTabbedPane.insertTab("Runtime Services", null, getServiceStatusPage(), "realtime services", 1);
			ivjTabbedPane.insertTab("Active Users", null, getUserConnectionPage(), "Connected users", 2);
			ivjTabbedPane.insertTab("Query", null, getQueryPage(), "query simulation status", 3);
			ivjTabbedPane.insertTab("Broadcast Message", null, getBroadcastPanel(), "send broadcast message", 4);
			// user code begin {1}
			// user code end
		} catch (java.lang.Throwable ivjExc) {
			// user code begin {2}
			// user code end
			handleException(ivjExc);
		}
	}
	return ivjTabbedPane;
}

/**
 * Return the ServicePage property value.
 * @return javax.swing.JPanel
 */
/* WARNING: THIS METHOD WILL BE REGENERATED. */
private javax.swing.JPanel getConfigPage() {
	if (ivjConfigPage == null) {
		try {
			ivjConfigPage = new javax.swing.JPanel();
			ivjConfigPage.setName("ConfigurationPage");
			ivjConfigPage.setLayout(new BorderLayout());
			
			JPanel panel = new JPanel();
			panel.setLayout(new FlowLayout(FlowLayout.LEFT));			
			JLabel label = new JLabel("Total services : ");
			label.setForeground(Color.red);			
			panel.add(label);
			panel.add(getNumConfigsLabel());			
			ivjConfigPage.add(panel, "North");
			
			ivjConfigPage.add(getConfigTable().getEnclosingScrollPane(), "Center");
			
			Box box = Box.createHorizontalBox();
			box.add(Box.createHorizontalGlue());
			box.add(getNewServiceButton());
			box.add(getModifyServiceButton());
			box.add(getDeleteServiceButton());
			box.add(Box.createHorizontalGlue());
			box.add(getRefreshServerManagerButton());
			ivjConfigPage.add(box, "South");
			
			// user code begin {1}
			// user code end
		} catch (java.lang.Throwable ivjExc) {
			// user code begin {2}
			// user code end
			handleException(ivjExc);
		}
	}
	return ivjConfigPage;
}

private JButton getNewServiceButton() {
	if (ivjNewServiceButton == null) {
		ivjNewServiceButton = new JButton("New");
	}
	return ivjNewServiceButton;
}

private JButton getDeleteServiceButton() {
	if (ivjDeleteServiceButton == null) {
		ivjDeleteServiceButton = new JButton("Delete");
		ivjDeleteServiceButton.setEnabled(false);
	}
	return ivjDeleteServiceButton;
}

private JButton getRefreshServerManagerButton() {
	if (ivjRefreshServerManagerButton == null) {
		ivjRefreshServerManagerButton = new JButton("Refresh Server Manager");
		ivjRefreshServerManagerButton.setEnabled(true);
	}
	return ivjRefreshServerManagerButton;
}

private JButton getModifyServiceButton() {
	if (ivjModifyServiceButton == null) {
		ivjModifyServiceButton = new JButton("Modify");
		ivjModifyServiceButton.setEnabled(false);
	}
	return ivjModifyServiceButton;
}

private javax.swing.JLabel getNumConfigsLabel() {
	if (ivjNumConfigsLabel == null) {
		try {
			ivjNumConfigsLabel = new javax.swing.JLabel();
			ivjNumConfigsLabel.setName("NumConfigsLabel");
			ivjNumConfigsLabel.setPreferredSize(new java.awt.Dimension(100, 14));
			ivjNumConfigsLabel.setText("0");
			ivjNumConfigsLabel.setMaximumSize(new java.awt.Dimension(100, 14));
			ivjNumConfigsLabel.setForeground(java.awt.Color.red);
			ivjNumConfigsLabel.setMinimumSize(new java.awt.Dimension(100, 14));
			// user code begin {1}
			// user code end
		} catch (java.lang.Throwable ivjExc) {
			// user code begin {2}
			// user code end
			handleException(ivjExc);
		}
	}
	return ivjNumConfigsLabel;
}


/**
 * Return the UserConnectionPage property value.
 * @return javax.swing.JPanel
 */
/* WARNING: THIS METHOD WILL BE REGENERATED. */
private javax.swing.JPanel getUserConnectionPage() {
	if (ivjUserConnectionPage == null) {
		try {
			ivjUserConnectionPage = new javax.swing.JPanel();
			ivjUserConnectionPage.setName("UserConnectionPage");
			ivjUserConnectionPage.setLayout(new java.awt.BorderLayout());
			
			JPanel panel = new javax.swing.JPanel();
			panel.setLayout(new FlowLayout(FlowLayout.LEFT));			
			JLabel label = new javax.swing.JLabel("Total Active Users:");
			label.setForeground(java.awt.Color.red);			
			panel.add(label);
			panel.add(getNumUserConnectionLabel(), getNumUserConnectionLabel().getName());			
			getUserConnectionPage().add(panel, "North");
			
			getUserConnectionPage().add(getUserConnectionTable().getEnclosingScrollPane(), "Center");
			// user code begin {1}
			// user code end
		} catch (java.lang.Throwable ivjExc) {
			// user code begin {2}
			// user code end
			handleException(ivjExc);
		}
	}
	return ivjUserConnectionPage;
}

/**
 * Return the UserConnectionTable property value.
 * @return cbit.vcell.messaging.admin.sorttable.JSortTable
 */
private JSortTable getUserConnectionTable() {
	if (ivjUserConnectionTable == null) {
		try {
			ivjUserConnectionTable = new JSortTable();
			ivjUserConnectionTable.setModel(new UserConnectionTableModel());
			ivjUserConnectionTable.disableUneditableForeground();
		} catch (java.lang.Throwable ivjExc) {
			handleException(ivjExc);
		}
	}
	return ivjUserConnectionTable;
}


/**
 * Called whenever the part throws an exception.
 * @param exception java.lang.Throwable
 */
private void handleException(java.lang.Throwable exception) {

	/* Uncomment the following lines to print uncaught exceptions to stdout */
	// System.out.println("--------- UNCAUGHT EXCEPTION ---------");
	 exception.printStackTrace(System.out);
}


/**
 * Initializes connections
 * @exception java.lang.Exception The exception description.
 */
/* WARNING: THIS METHOD WILL BE REGENERATED. */
private void initConnections() throws java.lang.Exception {
	// user code begin {1}
	// user code end
	getStopServiceButton().addActionListener(ivjEventHandler);
	getStartServiceButton().addActionListener(ivjEventHandler);
	getTabbedPane().addChangeListener(ivjEventHandler);
	getQueryWaitingCheck().addItemListener(ivjEventHandler);
	getQueryQueuedCheck().addItemListener(ivjEventHandler);
	getQueryFailedCheck().addItemListener(ivjEventHandler);
	getQueryRunningCheck().addItemListener(ivjEventHandler);
	getQueryStoppedCheck().addItemListener(ivjEventHandler);
	getQueryCompletedCheck().addItemListener(ivjEventHandler);
	getQueryAllStatusCheck().addItemListener(ivjEventHandler);
	getQueryGoButton().addActionListener(ivjEventHandler);
	getQueryResetButton().addActionListener(ivjEventHandler);
	getServiceStatusTable().addMouseListener(ivjEventHandler);
	getQueryResultTable().addMouseListener(ivjEventHandler);
	getConfigTable().addMouseListener(ivjEventHandler);
	getQuerySubmitDateCheck().addItemListener(ivjEventHandler);
	getQueryStartDateCheck().addItemListener(ivjEventHandler);
	getQueryEndDateCheck().addItemListener(ivjEventHandler);
	getQueryDispatchedCheck().addItemListener(ivjEventHandler);
	getRefreshButton().addActionListener(ivjEventHandler);
	getExitButton().addActionListener(ivjEventHandler);
	getRemoveFromListButton().addActionListener(ivjEventHandler);
	getSubmitSelectedButton().addActionListener(ivjEventHandler);
	getSendMessageButton().addActionListener(ivjEventHandler);
	getMessageResetButton().addActionListener(ivjEventHandler);
	getNewServiceButton().addActionListener(ivjEventHandler);
	getDeleteServiceButton().addActionListener(ivjEventHandler);
	getModifyServiceButton().addActionListener(ivjEventHandler);
	getRefreshServerManagerButton().addActionListener(ivjEventHandler);
	getStopSelectedButton().addActionListener(ivjEventHandler);
}

/**
 * Initialize the class.
 */
/* WARNING: THIS METHOD WILL BE REGENERATED. */
private void initialize() {
	try {
		// user code begin {1}
		try {
			log = new StdoutSessionLog("Console");
			setTitle("Virtual Cell Management Console -- " + VCellServerID.getSystemServerID());
			reconnect();
			
			try {
				KeyFactory keyFactory = new cbit.sql.OracleKeyFactory();	
				DbDriver.setKeyFactory(keyFactory);
				ConnectionFactory conFactory = new cbit.sql.OraclePoolingConnectionFactory(log);
				adminDbTop = new AdminDBTopLevel(conFactory,log);
			} catch (SQLException e) {
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				e.printStackTrace();
			} catch (InstantiationException e) {
				e.printStackTrace();
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
			}		
		} catch (JMSException ex) {
		}	
		// user code end
		setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
		setSize(1200, 700);
		add(getJFrameContentPane());
		
		statusChecks.add(getQueryWaitingCheck());
		statusChecks.add(getQueryQueuedCheck());
		statusChecks.add(getQueryDispatchedCheck());
		statusChecks.add(getQueryRunningCheck());
		statusChecks.add(getQueryCompletedCheck());
		statusChecks.add(getQueryStoppedCheck());
		statusChecks.add(getQueryFailedCheck());

		DateRenderer dateRenderer = new DateRenderer();
		dateRenderer.disableUneditableForeground();
		getQueryResultTable().setDefaultRenderer(Date.class, dateRenderer);
		getQueryResultTable().setDefaultRenderer(Long.class, dateRenderer);
		getConfigTable().setDefaultRenderer(Date.class, dateRenderer);
		getServiceStatusTable().setDefaultRenderer(Date.class, dateRenderer);
		initConnections();
	} catch (java.lang.Throwable ivjExc) {
		handleException(ivjExc);
	}
	// user code begin {2}	
	// user code end
}

/**
 * main entrypoint - starts the part when it is run as an application
 * @param args java.lang.String[]
 */
public static void main(java.lang.String[] args) {
	try {		
		PropertyLoader.loadProperties();
		
		javax.swing.UIManager.setLookAndFeel(javax.swing.UIManager.getSystemLookAndFeelClassName());
		ServerManageConsole aServerManageConsole = new ServerManageConsole();		

		aServerManageConsole.addWindowListener(new java.awt.event.WindowAdapter() {
			public void windowClosing(java.awt.event.WindowEvent e) {
				System.exit(0);
			};
		});
		java.awt.Insets insets = aServerManageConsole.getInsets();
		aServerManageConsole.setSize(aServerManageConsole.getWidth() + insets.left + insets.right, aServerManageConsole.getHeight() + insets.top + insets.bottom);
		aServerManageConsole.setLocation(200, 200);		
		aServerManageConsole.setVisible(true);
	} catch (Throwable exception) {
		System.err.println("Exception occurred in main() of javax.swing.JFrame");
		exception.printStackTrace(System.out);
	}
}


/**
 * Comment
 */
public void messageResetButton_ActionEvents() {
	getBroadcastMessageTextArea().setText("The Virtual Cell is going to reboot in 5 minutes due to technical requirements. Please save your work and logout." 
		+ " We are sorry for any inconvenience." 
		+ " If you have any questions, please contact the Virtual Cell at VCell_Support@uchc.edu.");
	getBroadcastMessageToTextField().setText("All");
}


/**
 * Insert the method's description here.
 * Creation date: (9/10/2003 2:27:25 PM)
 * @param service cbit.vcell.messaging.admin.VCellService
 */
private void onArrivingService(ServiceInstanceStatus arrivingService) {
	if (arrivingService.getType().equals(ServiceType.SERVERMANAGER)) {
		serviceInstanceStatusList.add(0, arrivingService);
		return;
	}	

	boolean bDefined = false;
	List<ServiceInstanceStatus> tempList = new ArrayList<ServiceInstanceStatus>(serviceInstanceStatusList);
	for (int i = 0; i < tempList.size(); i ++) {
		ServiceInstanceStatus sis = tempList.get(i);
		if (sis.getSpecID().equals(arrivingService.getSpecID())) {
			if (sis.isRunning()) {
				serviceInstanceStatusList.add(arrivingService);			
			} else {
				serviceInstanceStatusList.set(i, arrivingService);				
			}
			bDefined = true;
			break;
		} 
	}
	if (!bDefined) {
		serviceInstanceStatusList.add(arrivingService);		
	}
}


/**
 * onMessage method comment.
 */
public void onControlTopicMessage(Message message) {		
	try {
		log.print("onMessage [" + JmsUtils.toString(message) + "]");	
		String msgType = (String)JmsUtils.parseProperty(message, ManageConstants.MESSAGE_TYPE_PROPERTY, String.class);
		
		if (msgType == null) {
			return;
		}
		
		if (msgType.equals(ManageConstants.MESSAGE_TYPE_REPLYPERFORMANCESTATUS_VALUE) && message instanceof ObjectMessage) {			
			Object obj = ((ObjectMessage)message).getObject();			
			if (obj instanceof ServiceInstanceStatus) {
				final ServiceInstanceStatus serviceInfo = (ServiceInstanceStatus)obj;
				SwingUtilities.invokeLater(new Runnable() {
					public void run() {
						onArrivingService(serviceInfo);
					}
				});
			}
		}		
	} catch (Exception ex) {
		log.exception(ex);
	}	
}

public SimpleJobStatus getReturnedSimulationJobStatus(int selectedRow) {	
	return (SimpleJobStatus)((JobTableModel)getQueryResultTable().getModel()).getValueAt(selectedRow);
}

/**
 * Insert the method's description here.
 * Creation date: (10/24/2001 11:08:09 PM)
 * @param simulation cbit.vcell.solver.Simulation
 */
private void pingAll(int waitingTimeSec) {
	try {
		Message msg = topicSession.createMessage();			
		msg.setStringProperty(ManageConstants.MESSAGE_TYPE_PROPERTY, ManageConstants.MESSAGE_TYPE_ASKPERFORMANCESTATUS_VALUE);		
		log.print("sending ping message [" + JmsUtils.toString(msg) + "]");		
		topicSession.publishMessage(JmsUtils.getTopicDaemonControl(), msg);			
		try {
			Thread.sleep(waitingTimeSec * MessageConstants.SECOND);
		} catch (InterruptedException ex) {
			log.exception(ex);
		}		
	} catch (Exception ex) {
		log.exception(ex);
	}
}


/**
 * Insert the method's description here.
 * Creation date: (9/3/2003 8:00:07 AM)
 */
private void query() {	
	boolean bOtherConditions = false;
	
	getRemoveFromListButton().setEnabled(false);
	getSubmitSelectedButton().setEnabled(false);
	getStopSelectedButton().setEnabled(false);
	StringBuffer conditions = new StringBuffer();
	String text = getQuerySimField().getText();
	if (text != null && text.trim().length() > 0) {
		try {
			bOtherConditions = true;
			int simID = Integer.parseInt(text);
			conditions.append(SimulationJobTable.table.simRef.getQualifiedColName() + "=" + simID);
		} catch (NumberFormatException ex) {
		}
	}

	text = getQueryHostField().getText();
	if (text != null && text.trim().length() > 0) {
		bOtherConditions = true;
		if (conditions.length() > 0) {
			conditions.append(" AND ");
		}
		conditions.append("lower(" + SimulationJobTable.table.computeHost.getQualifiedColName() + ")='" + text.toLowerCase() + "'");
	}

	text = getQueryServerIDField().getText();
	if (text != null && text.trim().length() > 0) {
		bOtherConditions = true;
		if (conditions.length() > 0) {
			conditions.append(" AND ");
		}
		conditions.append("lower(" + SimulationJobTable.table.serverID.getQualifiedColName() + ")='" + text.toLowerCase() + "'");
	}
		
	text = getQueryUserField().getText();
	if (text != null && text.trim().length() > 0) {
		bOtherConditions = true;
		if (conditions.length() > 0) {
			conditions.append(" AND ");
		}
		conditions.append(UserTable.table.userid.getQualifiedColName() + "='" + text + "'");
	}

	StringBuffer status = new StringBuffer();
	int index = 0;	
	if (!getQueryAllStatusCheck().isSelected()) {
		Iterator<JCheckBox> iter = statusChecks.iterator();
		for (; iter.hasNext() ; index ++) {
			JCheckBox box = iter.next();	
			if (box.isSelected()) {
				if (status.length() > 0) {
					status.append(" OR ");
				}					
				status.append(SimulationJobTable.table.schedulerStatus.getQualifiedColName() + "=" + index);		
			}
		}			
	}

	if (status.length() > 0) {
		if (conditions.length() > 0) {
			conditions.append(" AND ");
		}
		conditions.append("(" + status + ")");
	}

	java.text.SimpleDateFormat df = new java.text.SimpleDateFormat("MM/dd/yyyy", java.util.Locale.US);
	
	if (getQuerySubmitDateCheck().isSelected()) {
		bOtherConditions = true;
		String d1 = df.format(getQuerySubmitFromDate().getDate());
		String d2 = df.format(getQuerySubmitToDate().getDate());
		if (conditions.length() > 0) {
			conditions.append(" AND ");
		}
		conditions.append("(" + SimulationJobTable.table.submitDate.getQualifiedColName() 
			+ " BETWEEN to_date('" + d1 + " 00:00:00', 'mm/dd/yyyy HH24:MI:SS') AND to_date('" + d2 + " 23:59:59', 'mm/dd/yyyy HH24:MI:SS'))");		
	}
	
	if (getQueryStartDateCheck().isSelected()) {
		bOtherConditions = true;
		String d1 = df.format(getQueryStartFromDate().getDate());
		String d2 = df.format(getQueryStartToDate().getDate());
		if (conditions.length() > 0) {
			conditions.append(" AND ");
		}
		conditions.append("(" + SimulationJobTable.table.startDate.getQualifiedColName() 
			+ " BETWEEN to_date('" + d1 + "00:00:00', 'mm/dd/yyyy HH24:MI:SS') AND to_date('" + d2 + " 23:59:59', 'mm/dd/yyyy HH24:MI:SS'))");		
	}
		
	if (getQueryEndDateCheck().isSelected()) {
		bOtherConditions = true;
		String d1 = df.format(getQueryEndFromDate().getDate());
		String d2 = df.format(getQueryEndToDate().getDate());
		if (conditions.length() > 0) {
			conditions.append(" AND ");
		}
		conditions.append("(" + SimulationJobTable.table.endDate.getQualifiedColName() 
			+ " BETWEEN to_date('" + d1 + "00:00:00', 'mm/dd/yyyy HH24:MI:SS') AND to_date('" + d2 + " 23:59:59', 'mm/dd/yyyy HH24:MI:SS'))");		
	}
	
	if (getQueryCompletedCheck().isSelected() && !bOtherConditions) {
		int n = javax.swing.JOptionPane.showConfirmDialog(this, "You are gonna get all the completed simulation jobs in the database, which is gonna be huge . Continue?", "Confirm", javax.swing.JOptionPane.YES_NO_OPTION);
		if (n == javax.swing.JOptionPane.NO_OPTION) {
			getNumResultsLabel().setText("0");
			getNumSelectedLabel().setText("0");
			((JobTableModel)getQueryResultTable().getModel()).setData(null);			
			return;
		}
	}
	
	try {
		List<SimpleJobStatus> resultList = adminDbTop.getSimulationJobStatus(conditions.toString(), true);
		getNumResultsLabel().setText("" + resultList.size());
		getNumSelectedLabel().setText("0");
		((JobTableModel)getQueryResultTable().getModel()).setData(resultList);
	} catch (Exception ex) {
		getNumResultsLabel().setText("Query failed, please try again!");
		((JobTableModel)getQueryResultTable().getModel()).setData(null);
	}
}


/**
 * Comment
 */
public void queryAllStatusCheck_ItemStateChanged(ItemEvent itemEvent) {
	updateChecks(itemEvent);
	return;
}


/**
 * Comment
 */
public void queryCompletedCheck_ItemStateChanged(ItemEvent itemEvent) {
	updateChecks(itemEvent);
	return;
}


/**
 * Comment
 */
public void queryDispatchedCheck_ItemStateChanged(java.awt.event.ItemEvent itemEvent) {
	updateChecks(itemEvent);
	return;
}


/**
 * Comment
 */
public void queryEndDateSubmit_ItemStateChanged(java.awt.event.ItemEvent itemEvent) {
	if (itemEvent.getStateChange() == java.awt.event.ItemEvent.SELECTED) {
		getQueryEndFromDate().setEnabled(true);
		getQueryEndToDate().setEnabled(true);
	} else {
		getQueryEndFromDate().setEnabled(false);
		getQueryEndToDate().setEnabled(false);
	}
	return;
}


/**
 * Comment
 */
public void queryFailedCheck_ItemStateChanged(java.awt.event.ItemEvent itemEvent) {
	updateChecks(itemEvent);
	return;
}


/**
 * Comment
 */
public void queryGoButton_ActionPerformed(java.awt.event.ActionEvent actionEvent) {
	query();
	return;
}


/**
 * Comment
 */
public void queryQueuedCheck_ItemStateChanged(java.awt.event.ItemEvent itemEvent) {
	updateChecks(itemEvent);
	return;
}

private void submitSelectedButton_ActionPerformed(ActionEvent e) {
	int srows[] = getQueryResultTable().getSelectedRows();
	if (srows==null || srows.length==0) {
		return;
	}
	final String SUBMIT_JOBS_OPTION = "submit jobs";
	final String CANCEL_OPTION = "cancel";
	String response = DialogUtils.showWarningDialog(this, "Are you sure you want to submit "+srows.length+" simulation job(s)? (see console for progress printed to stdout)", new String[] { SUBMIT_JOBS_OPTION, CANCEL_OPTION }, CANCEL_OPTION);
	if (response.equals(SUBMIT_JOBS_OPTION)){
		for (int i = 0; i < srows.length; i++) {
			int selectedRow = srows[i];
			SimpleJobStatus jobStatus = getReturnedSimulationJobStatus(selectedRow);
			String statusString = "["+ jobStatus.getVCSimulationIdentifier() + ", " + jobStatus.getStatusMessage() + "]";
			if (jobStatus.isDone()) {
				log.print("Submitting job (" + (i+1) + " of " + srows.length + ") : " + statusString);
				resubmitSimulation(jobStatus.getUserID(), jobStatus.getVCSimulationIdentifier().getSimulationKey());
			} else {
				log.print("Submitting job ("+(i+1)+" of "+srows.length+") : " + statusString + ", is still running, skipping...");
			}
		}
	}
}

private void stopSelectedButton_ActionPerformed(ActionEvent e) {
	int srows[] = getQueryResultTable().getSelectedRows();
	if (srows==null || srows.length==0) {
		return;
	}
	final String STOP_JOBS_OPTION = "stop jobs";
	final String CANCEL_OPTION = "cancel";
	String response = DialogUtils.showWarningDialog(this, "Are you sure you want to stop "
			+srows.length+" simulation job(s)? (see console for progress printed to stdout)", 
			new String[] { STOP_JOBS_OPTION, CANCEL_OPTION }, CANCEL_OPTION);
	if (response.equals(STOP_JOBS_OPTION)){
		for (int i = 0; i < srows.length; i++) {
			int selectedRow = srows[i];
			SimpleJobStatus jobStatus = getReturnedSimulationJobStatus(selectedRow);
			String statusString = "["+ jobStatus.getVCSimulationIdentifier() + ", " + jobStatus.getStatusMessage() + "]";
			if (!jobStatus.isDone()) {
				log.print("Stopping job ("+(i+1)+" of "+srows.length+") : "+statusString);	
				stopSimulation(jobStatus.getUserID(), jobStatus.getVCSimulationIdentifier().getSimulationKey());
			} else {
				log.print("***Stopping job ("+(i+1)+" of "+srows.length+") : "+statusString + ", is already finished, skipping");
			}
		}
	}
}

/**
 * Comment
 */
public void queryResetButton_ActionPerformed(java.awt.event.ActionEvent actionEvent) {
	getQuerySimField().setText(null);
	getQueryHostField().setText(null);
	getQueryUserField().setText(null);
	getQueryAllStatusCheck().setSelected(true);
	getQueryStartFromDate().reset();
	getQueryStartToDate().reset();
	getQueryEndFromDate().reset();	
	getQueryEndToDate().reset();
	getQuerySubmitFromDate().reset();
	getQuerySubmitToDate().reset();
	getQuerySubmitDateCheck().setSelected(true);
	getQueryStartDateCheck().setSelected(false);
	getQueryEndDateCheck().setSelected(false);
	return;
}


/**
 * Comment
 */
public void queryResultTable_MouseClicked(java.awt.event.MouseEvent mouseEvent) {
	int srow = getQueryResultTable().getSelectedRow();
	if (srow < 0) {
		getRemoveFromListButton().setEnabled(false);
		getSubmitSelectedButton().setEnabled(false);
		getStopSelectedButton().setEnabled(false);
		return;
	}
	if (mouseEvent.getClickCount() == 1) {
		getRemoveFromListButton().setEnabled(true);
		getSubmitSelectedButton().setEnabled(true);
		getStopSelectedButton().setEnabled(true);
		getNumSelectedLabel().setText("" + getQueryResultTable().getSelectedRowCount());
	} else if (mouseEvent.getClickCount() == 2) {
		SimulationJobStatusDetailDialog dialog = new SimulationJobStatusDetailDialog(this, getQueryResultTable().getRowCount(), srow);
		dialog.setLocationRelativeTo(this);
		dialog.setVisible(true);
	}
}

public void configTable_mouseClicked(java.awt.event.MouseEvent mouseEvent) {
	int srow = getConfigTable().getSelectedRow();
	if (srow < 0) {
		getDeleteServiceButton().setEnabled(false);
		getModifyServiceButton().setEnabled(false);
	}
	
	getDeleteServiceButton().setEnabled(true);
	getModifyServiceButton().setEnabled(true);
	
	if (mouseEvent.getClickCount() == 2) {
		modifyService();
	}
}

/**
 * Comment
 */
public void queryRunningCheck_ItemStateChanged(java.awt.event.ItemEvent itemEvent) {
	updateChecks(itemEvent);
	return;
}


/**
 * Comment
 */
public void queryStartDateCheck_ItemStateChanged(java.awt.event.ItemEvent itemEvent) {
	if (itemEvent.getStateChange() == java.awt.event.ItemEvent.SELECTED) {
		getQueryStartFromDate().setEnabled(true);
		getQueryStartToDate().setEnabled(true);
	} else {
		getQueryStartFromDate().setEnabled(false);
		getQueryStartToDate().setEnabled(false);
	}	
	return;
}

/**
 * Comment
 */
public void queryStoppedCheck_ItemStateChanged(ItemEvent itemEvent) {
	updateChecks(itemEvent);
	return;
}


/**
 * Comment
 */
public void querySubmitDateCheck_ItemStateChanged(java.awt.event.ItemEvent itemEvent) {
	if (itemEvent.getStateChange() == java.awt.event.ItemEvent.SELECTED) {
		getQuerySubmitFromDate().setEnabled(true);
		getQuerySubmitToDate().setEnabled(true);
	} else {
		getQuerySubmitFromDate().setEnabled(false);
		getQuerySubmitToDate().setEnabled(false);
	}
	return;
}


/**
 * Comment
 */
public void queryWaitingCheck_ItemStateChanged(java.awt.event.ItemEvent itemEvent) {
	updateChecks(itemEvent);
	return;
}


/**
 * Comment
 */
private void reconnect() throws JMSException {
	jmsConnFactory = new JmsConnectionFactoryImpl();
	
	jmsConn = jmsConnFactory.createConnection();
	
	topicSession = jmsConn.getAutoSession();
	JmsSession listenSession = jmsConn.getAutoSession();
	String filter = MESSAGE_TYPE_PROPERTY + " NOT IN " 
		+ "('" + MESSAGE_TYPE_IAMALIVE_VALUE + "'" 
		+ ",'" + MESSAGE_TYPE_ISSERVICEALIVE_VALUE + "'" 
		+ ",'" + MESSAGE_TYPE_REFRESHSERVERMANAGER_VALUE + "'"
		+ ",'" + MESSAGE_TYPE_STARTSERVICE_VALUE + "'"
		+ ",'" + MESSAGE_TYPE_STOPSERVICE_VALUE + "'"
		+ ")";
	listenSession.setupTopicListener(JmsUtils.getTopicDaemonControl(), filter, new ControlMessageCollector(this));
	jmsConn.startConnection();
}

private void refresh () {
	int count = getServiceStatusTable().getRowCount();
	boolean bAll = false;
	if (count == 0) {
		bAll = true;
	}
	int tabIndex = getTabbedPane().getSelectedIndex();
	if (tabIndex == 0 || tabIndex == 1) {
		final int waitingTimeSec = 5;
		
		Thread t = new Thread(new Runnable() {
			public void run() {
				getProgressBar().setMaximum(waitingTimeSec);
				getProgressBar().setMinimum(0);
				SwingUtilities.invokeLater(new Runnable() {
					public void run() {
						getProgressBar().setIndeterminate(true);
						getRefreshButton().setEnabled(false);
					}			
				});									
			}
		});
		t.setName("Refresh Thread");
		t.start();
		
		try {
			serviceConfigList = adminDbTop.getAllServiceStatus(true);
		} catch (Exception e) {			
			e.printStackTrace();
			javax.swing.JOptionPane.showMessageDialog(this, e.getMessage(), "Error", javax.swing.JOptionPane.ERROR_MESSAGE);
		}	
		
		showConfigs(serviceConfigList);
		getModifyServiceButton().setEnabled(false);
		getDeleteServiceButton().setEnabled(false);
		
		if (!bAll && tabIndex == 0) {		
			Thread t1 = new Thread(new Runnable() {
				public void run() {
					SwingUtilities.invokeLater(new Runnable() {
						public void run() {
							getProgressBar().setIndeterminate(false);
							getProgressBar().setValue(waitingTimeSec);
							getRefreshButton().setEnabled(true);
						}			
					});					
				}
			});			
			t1.setName("Refresh Thread");
			t1.start();			
		} else {		
			((ServiceInstanceStatusTableModel)getServiceStatusTable().getModel()).setData(null);
			serviceInstanceStatusList.clear();
			
			for (int i = 0; i < serviceConfigList.size(); i ++) {
				ServiceSpec ss = serviceConfigList.get(i).getServiceSpec();
				serviceInstanceStatusList.add(new ServiceInstanceStatus(ss.getServerID(), ss.getType(), ss.getOrdinal(), null, null, false));
			}
			
			showServices(serviceInstanceStatusList);
			Thread pingThread = new Thread(new Runnable() {
				public void run() {			
					pingAll(waitingTimeSec);	
					SwingUtilities.invokeLater(new Runnable() {
						public void run() {
							getProgressBar().setIndeterminate(false);
							getProgressBar().setValue(waitingTimeSec);
							getRefreshButton().setEnabled(true);
						}			
					});					
				}
			});
			pingThread.setName("Refresh Thread");
			pingThread.start();
		}		
	} else if (tabIndex == 2) {
		userList.clear();
		try {
			if (vcellBootstrap == null) {
				vcellBootstrap = (VCellBootstrap) java.rmi.Naming.lookup(getLocalVCellBootstrapUrl());
				vcellServer = vcellBootstrap.getVCellServer(new User("Administrator",new KeyValue("2")), "icnia66");
			}		
			
			ServerInfo serverInfo = vcellServer.getServerInfo();
			User[] users = serverInfo.getConnectedUsers();
			for (int i = 0; i < users.length; i ++) {
				userList.add(new SimpleUserConnection(users[i], new Date()));
			}
		} catch (Exception ex) {
			javax.swing.JOptionPane.showMessageDialog(this, "Exception:" + ex.getMessage(), "Error", javax.swing.JOptionPane.ERROR_MESSAGE);
		}
		showUsers(userList);
	}
		
	return;
}


/**
 * Comment
 */
public void refreshButton_ActionPerformed(ActionEvent actionEvent) {
	refresh();
	return;
}


/**
 * Comment
 */
public void removeFromListButton_ActionPerformed(java.awt.event.ActionEvent actionEvent) {
	int[] indexes = getQueryResultTable().getSelectedRows();
	for (int i = 0; i < indexes.length; i ++) {
		((JobTableModel)getQueryResultTable().getModel()).removeValueAt(indexes[i] - i);
	}
	getNumResultsLabel().setText("" + getQueryResultTable().getRowCount());
	getNumSelectedLabel().setText("0");
	return;
}


/**
 * Insert the method's description here.
 * Creation date: (7/19/2004 3:32:52 PM)
 * @param simKey cbit.sql.KeyValue
 */
public void resubmitSimulation(String userid, KeyValue simKey) {
	try {
		User user = adminDbTop.getUser(userid, true);
		RpcDbServerProxy dbProxy = getDbProxy(user);
		BigString simxml = dbProxy.getSimulationXML(simKey);
		if (simxml == null) {
			javax.swing.JOptionPane.showMessageDialog(this, "Simulation [" + simKey + "] doesn't exit, might have been deleted.", "Error", javax.swing.JOptionPane.ERROR_MESSAGE);
			return;
		}
		Simulation sim = XmlHelper.XMLToSim(simxml.toString());
		if (sim == null) {
			javax.swing.JOptionPane.showMessageDialog(this, "Simulation [" + simKey + "] doesn't exit, might have been deleted.", "Error", javax.swing.JOptionPane.ERROR_MESSAGE);
			return;
		}
		RpcSimServerProxy simProxy = getSimProxy(user);
		simProxy.startSimulation(sim.getSimulationInfo().getAuthoritativeVCSimulationIdentifier());		
	} catch (Exception ex) {
		javax.swing.JOptionPane.showMessageDialog(this, "Resubmitting simulation failed:" + ex.getMessage(), "Error", javax.swing.JOptionPane.ERROR_MESSAGE);
	}
}


/**
 * Comment
 */
public void sendMessageButton_ActionPerformed(java.awt.event.ActionEvent actionEvent) {
	try {
		int n = javax.swing.JOptionPane.showConfirmDialog(this, "You are going to send message to " + getBroadcastMessageToTextField().getText() + ". Continue?", "Confirm", javax.swing.JOptionPane.YES_NO_OPTION);
		if (n == javax.swing.JOptionPane.NO_OPTION) {
			return;
		}	
		
		Message msg = topicSession.createObjectMessage(new BigString(getBroadcastMessageTextArea().getText()));
		String username = getBroadcastMessageToTextField().getText();

		if (username.equalsIgnoreCase("All")) {
			username = "All";
		}
			
		msg.setStringProperty(ManageConstants.MESSAGE_TYPE_PROPERTY, ManageConstants.MESSAGE_TYPE_BROADCASTMESSAGE_VALUE);
		msg.setStringProperty(MessageConstants.USERNAME_PROPERTY, username);
		
		log.print("sending broadcast message [" + JmsUtils.toString(msg) + "]");		
		topicSession.publishMessage(JmsUtils.getTopicClientStatus(), msg);		

	} catch (Exception ex) {
		log.exception(ex);
	}
}


/**
 * Comment
 */
public void serverManageConsole_WindowClosed(java.awt.event.WindowEvent windowEvent) {
	try {
		dispose();
		if (jmsConn != null) {
			jmsConn.close();
		}
	} catch (JMSException ex) {
		log.exception(ex);
	} finally {
 		System.exit(0);	
 	}	
}

/**
 * Comment
 */
public void serverStatusTable_MouseClicked(java.awt.event.MouseEvent mouseEvent) {
	}


/**
 * Comment
 */
public void serviceStatusTable_MouseClicked(java.awt.event.MouseEvent mouseEvent) {
	int selectedCount = getServiceStatusTable().getSelectedRowCount();
	int[] selectedRows = getServiceStatusTable().getSelectedRows();
	if (selectedRows == null || selectedCount < 1) {
		return;
	}

	getStopServiceButton().setEnabled(false);
	getStartServiceButton().setEnabled(false);

	for (int i = 0; i < selectedCount; i ++){	
		int row = selectedRows[i];
		ServiceInstanceStatus serviceInstanceStatus = (ServiceInstanceStatus)((ServiceInstanceStatusTableModel)getServiceStatusTable().getModel()).getValueAt(row);		
		if (serviceInstanceStatus.isRunning()) {
			getStopServiceButton().setEnabled(true);
		} else {
			//getStartServiceButton().setEnabled(true);
		}
	}
}


/**
 * Insert the method's description here.
 * Creation date: (8/31/2005 1:20:48 PM)
 * @return cbit.vcell.messaging.admin.SimpleJobStatus
 */
public void setSelectedReturnedSimulationJobStatus(int selectedRow) {	
	getQueryResultTable().setRowSelectionInterval(selectedRow, selectedRow);
}


/**
 * Method generated to support the promotion of the userConnectionTableModel attribute.
 * @param arg1 cbit.vcell.messaging.admin.sorttable.SortTableModel
 */
public void setUserConnectionTableModel(org.vcell.util.gui.sorttable.SortTableModel arg1) {
	getUserConnectionTable().setModel(arg1);
}

/**
 * Insert the method's description here.
 * Creation date: (2/17/2004 1:21:46 PM)
 * @param serviceList0 java.util.List
 */
private void showServices(final List<ServiceInstanceStatus> serviceList0) {
	SwingUtilities.invokeLater(new Runnable() {
		public void run() {
			((ServiceInstanceStatusTableModel)(getServiceStatusTable().getModel())).setData(serviceList0);
			getNumServiceLabel().setText(serviceList0.size() + "");
		}
	});
}

private void showConfigs(List<ServiceStatus> configList0) {
	((ServiceStatusTableModel)(getConfigTable().getModel())).setData(configList0);
	getNumConfigsLabel().setText(configList0.size() + "");
}

/**
 * Insert the method's description here.
 * Creation date: (2/17/2004 1:21:46 PM)
 * @param serviceList0 java.util.List
 */
private void showUsers(List<SimpleUserConnection> userList0) {
	((UserConnectionTableModel)(getUserConnectionTable().getModel())).setData(userList0);
	getNumUserConnectionLabel().setText(userList0.size() + "");
}

/**
 * Comment
 */
public void startServiceButton_ActionPerformed(java.awt.event.ActionEvent actionEvent) {
	startServices();
	return;
}


/**
 * Insert the method's description here.
 * Creation date: (8/20/2003 1:15:33 PM)
 */
private void startServices() {
//	try
//	} catch (Exception ex) {
//		javax.swing.JOptionPane.showMessageDialog(this, "Failed!!: " + ex.getMessage(), "Bad News", javax.swing.JOptionPane.ERROR_MESSAGE);
//	}
}

/**
 * Comment
 */
public void stopServiceButton_ActionPerformed(java.awt.event.ActionEvent actionEvent) {
	stopServices();
	return;
}

private void refreshServerManager() {
	try {
		int n = javax.swing.JOptionPane.showConfirmDialog(this, "You are going to refresh server manager. Continue?", "Confirm", javax.swing.JOptionPane.YES_NO_OPTION);
		if (n == javax.swing.JOptionPane.NO_OPTION) {
			return;
		}			
		Message msg = topicSession.createMessage();
			
		msg.setStringProperty(ManageConstants.MESSAGE_TYPE_PROPERTY, ManageConstants.MESSAGE_TYPE_REFRESHSERVERMANAGER_VALUE);
			
		log.print("sending refresh server manager message [" + JmsUtils.toString(msg) + "]");		
		topicSession.publishMessage(JmsUtils.getTopicDaemonControl(), msg);			
	} catch (Exception ex) {
		javax.swing.JOptionPane.showMessageDialog(this, "Failed!!: " + ex.getMessage(), "Bad News", javax.swing.JOptionPane.ERROR_MESSAGE);
	}	
}

/**
 * Insert the method's description here.
 * Creation date: (8/20/2003 1:15:33 PM)
 */
private void stopServices() {
	try {
		int selectedCount = getServiceStatusTable().getSelectedRowCount();
		int[] selectedRows = getServiceStatusTable().getSelectedRows();
		if (selectedRows == null || selectedCount < 1) {
			return;
		}

		for (int i = 0; i < selectedCount; i ++){					
			int row = selectedRows[i];
			ServiceInstanceStatus serviceInstanceStatus = (ServiceInstanceStatus)((ServiceInstanceStatusTableModel)getServiceStatusTable().getModel()).getValueAt(row);		
			if (!serviceInstanceStatus.isRunning()) {
				continue;
			}			
			
			sendStopMessage(serviceInstanceStatus.getID());		
		}
		refresh();
		clearServiceStatusTab();
	} catch (Exception ex) {
		javax.swing.JOptionPane.showMessageDialog(this, "Failed!!: " + ex.getMessage(), "Bad News", javax.swing.JOptionPane.ERROR_MESSAGE);
	}	
}

private void sendStopMessage(String serviceInstanceID) throws JMSException {
	Message msg = topicSession.createMessage();
	
	msg.setStringProperty(ManageConstants.MESSAGE_TYPE_PROPERTY, ManageConstants.MESSAGE_TYPE_STOPSERVICE_VALUE);
	msg.setStringProperty(ManageConstants.SERVICE_ID_PROPERTY, serviceInstanceID);
	
	log.print("sending stop service message [" + JmsUtils.toString(msg) + "]");		
	topicSession.publishMessage(JmsUtils.getTopicDaemonControl(), msg);		
}

private void stopService(ServiceSpec ss) {
	try {	
		int count = getServiceStatusTable().getRowCount();
		for (int i = 0; i < count; i ++){					
			ServiceInstanceStatus serviceInstanceStatus = (ServiceInstanceStatus)((ServiceInstanceStatusTableModel)getServiceStatusTable().getModel()).getValueAt(i);
			if (!serviceInstanceStatus.isRunning() || !serviceInstanceStatus.getSpecID().equals(ss.getID())) {
				continue;
			}
			
			sendStopMessage(serviceInstanceStatus.getID());
		}
	} catch (Exception ex) {
		javax.swing.JOptionPane.showMessageDialog(this, "Failed!!: " + ex.getMessage(), "Bad News", javax.swing.JOptionPane.ERROR_MESSAGE);
	}
}


/**
 * Insert the method's description here.
 * Creation date: (7/19/2004 3:32:52 PM)
 * @param simKey cbit.sql.KeyValue
 */
public void stopSimulation(String userid, KeyValue simKey) {
	try {
		User user = adminDbTop.getUser(userid, true);
		RpcDbServerProxy dbProxy = getDbProxy(user);
		BigString simxml = dbProxy.getSimulationXML(simKey);
		if (simxml == null) {
			javax.swing.JOptionPane.showMessageDialog(this, "Simulation [" + simKey + "] doesn't exit, might have been deleted.", "Error", javax.swing.JOptionPane.ERROR_MESSAGE);
			return;
		}
		cbit.vcell.solver.Simulation sim = cbit.vcell.xml.XmlHelper.XMLToSim(simxml.toString());
		if (sim == null) {
			javax.swing.JOptionPane.showMessageDialog(this, "Simulation [" + simKey + "] doesn't exit, might have been deleted.", "Error", javax.swing.JOptionPane.ERROR_MESSAGE);
			return;
		}
		cbit.vcell.messaging.server.RpcSimServerProxy simProxy = getSimProxy(user);
		simProxy.stopSimulation(sim.getSimulationInfo().getAuthoritativeVCSimulationIdentifier());		
	} catch (Exception ex) {
		javax.swing.JOptionPane.showMessageDialog(this, "Resubmitting simulation failed:" + ex.getMessage(), "Error", javax.swing.JOptionPane.ERROR_MESSAGE);
	}
}


/**
 * Comment
 */
public void tabbedPane_ChangeEvents() {    
    switch (getTabbedPane().getSelectedIndex()) {
        case 0 :
        case 1 :
        case 2 :
        	if (!getProgressBar().isIndeterminate()) {
        		getRefreshButton().setEnabled(true);
        	}
            break;
            
        case 3 :
            getRefreshButton().setEnabled(false);
            break;
        case 4:
        	ivjBroadcastMessageTextArea.setText("Dear User,\n\n" 
					+ "VCell is shutting down for maintenance in 5 minutes. Please save your work and logout.\n\n"
					+ "We are sorry for the inconvenience. Please come back in 15 minutes.\n\n\n"
					+ "VCell team\n" 
					+ new Date());	
        	break;
    }

    return;
}


/**
 * Insert the method's description here.
 * Creation date: (8/29/2003 4:57:15 PM)
 */
private void updateChecks(java.awt.event.ItemEvent event) {
	if (event.getSource() == getQueryAllStatusCheck()) {
		if (event.getStateChange() == java.awt.event.ItemEvent.SELECTED) {
			getQueryWaitingCheck().setSelected(true);
			getQueryQueuedCheck().setSelected(true);
			getQueryDispatchedCheck().setSelected(true);
			getQueryRunningCheck().setSelected(true);
			getQueryCompletedCheck().setSelected(true);
			getQueryFailedCheck().setSelected(true);
			getQueryStoppedCheck().setSelected(true);
		}
	} else if (event.getStateChange() == java.awt.event.ItemEvent.DESELECTED) {
		if (getQueryAllStatusCheck().isSelected())
			getQueryAllStatusCheck().setSelected(false);		 
	}
}
}