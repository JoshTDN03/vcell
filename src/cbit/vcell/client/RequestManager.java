package cbit.vcell.client;
import java.awt.Component;

import org.vcell.util.DataAccessException;
import org.vcell.util.document.BioModelInfo;
import org.vcell.util.document.MathModelInfo;
import org.vcell.util.document.VCDataIdentifier;
import org.vcell.util.document.VCDocument;
import org.vcell.util.document.VCDocumentInfo;

import cbit.vcell.client.FieldDataWindowManager.SimInfoHolder;
import cbit.vcell.client.data.OutputContext;
import cbit.vcell.client.server.AsynchMessageManager;
import cbit.vcell.client.server.ClientServerInfo;
import cbit.vcell.client.server.ConnectionStatus;
import cbit.vcell.client.server.DataManager;
import cbit.vcell.client.server.DataViewerController;
import cbit.vcell.client.server.MergedDatasetViewerController;
import cbit.vcell.client.server.UserPreferences;
import cbit.vcell.client.task.AsynchClientTask;
import cbit.vcell.clientdb.DocumentManager;
import cbit.vcell.export.server.ExportSpecs;
import cbit.vcell.mapping.SimulationContext;
import cbit.vcell.solver.Simulation;
import cbit.vcell.solver.SimulationInfo;
import cbit.vcell.solver.ode.gui.SimulationStatus;
import cbit.vcell.visit.VisitSession;
import cbit.xml.merge.TMLPanel;
import cbit.xml.merge.XmlTreeDiff;
/**
 * Insert the type's description here.
 * Creation date: (5/21/2004 2:36:40 AM)
 * @author: Ion Moraru
 */
public interface RequestManager {
/**
 * Insert the method's description here.
 * Creation date: (5/27/2004 2:12:25 AM)
 * @param clientServerInfo cbit.vcell.client.server.ClientServerInfo
 */
void changeGeometry(DocumentWindowManager requester, SimulationContext simContext);

/**
 * Insert the method's description here.
 * Creation date: (5/21/2004 2:51:33 AM)
 * @param windowID java.lang.String
 */
boolean closeWindow(String managerID, boolean exitIfLast);


/**
 * Insert the method's description here.
 * Creation date: (5/27/2004 2:59:24 PM)
 * @param vcDocument cbit.vcell.document.VCDocument
 */
XmlTreeDiff compareWithOther(VCDocumentInfo vcDoc1, VCDocumentInfo vcDoc2);


/**
 * Insert the method's description here.
 * Creation date: (5/27/2004 2:59:24 PM)
 * @param vcDocument cbit.vcell.document.VCDocument
 */
XmlTreeDiff compareWithSaved(VCDocument document);


/**
 * Insert the method's description here.
 * Creation date: (5/27/2004 2:12:25 AM)
 * @param clientServerInfo cbit.vcell.client.server.ClientServerInfo
 */
void connectAs(String user, String password, TopLevelWindowManager requester);


/**
 * Insert the method's description here.
 * Creation date: (5/27/2004 2:12:25 AM)
 * @param clientServerInfo cbit.vcell.client.server.ClientServerInfo
 */
void connectToServer(TopLevelWindowManager requester, ClientServerInfo clientServerInfo) throws Exception;


/**
 * Insert the method's description here.
 * Creation date: (5/21/2004 2:37:55 AM)
 * @param documentType int
 */
void createMathModelFromApplication(BioModelWindowManager requester, String name, SimulationContext simContext);


/**
 * Insert the method's description here.
 * Creation date: (5/29/2006 11:16:29 AM)
 */
void curateDocument(VCDocumentInfo vcDocInfo, int curateType, final TopLevelWindowManager requester);

void updateUserRegistration(DocumentWindowManager docWindowManager, boolean bNewUser);
void sendLostPassword(DocumentWindowManager docWindowManager, String userid);

/**
 * Insert the method's description here.
 * Creation date: (5/21/2004 2:38:26 AM)
 * @param documentInfo cbit.vcell.document.VCDocumentInfo
 */
void deleteDocument(VCDocumentInfo documentInfo, TopLevelWindowManager requester);


/**
 * Insert the method's description here.
 * Creation date: (5/21/2004 2:53:15 AM)
 */
void exitApplication();


/**
 * Comment
 */
void exportDocument(TopLevelWindowManager manager);

/**
 * Insert the method's description here.
 * Creation date: (6/9/2004 3:20:26 PM)
 * @return cbit.vcell.client.AsynchMessageManager
 */
AsynchMessageManager getAsynchMessageManager();

ConnectionStatus getConnectionStatus();

/**
 * Insert the method's description here.
 * Creation date: (6/11/2004 6:40:26 AM)
 * @return cbit.vcell.desktop.controls.DataManager
 * @param vcDataIdentifier cbit.vcell.server.VCDataIdentifier
 */
DataManager getDataManager(OutputContext outputContext, VCDataIdentifier vcDataID, boolean isSpatial) throws DataAccessException;


/**
 * Insert the method's description here.
 * Creation date: (5/21/2004 9:56:48 AM)
 * @return cbit.vcell.clientdb.DocumentManager
 */
DocumentManager getDocumentManager();


/**
 * Insert the method's description here.
 * Creation date: (6/11/2004 6:40:26 AM)
 * @return cbit.vcell.desktop.controls.DataManager
 * @param vcDataIdentifier cbit.vcell.server.VCDataIdentifier
 */
MergedDatasetViewerController getMergedDatasetViewerController(OutputContext outputContext, VCDataIdentifier vcdId, boolean expectODEData) throws DataAccessException;

SimInfoHolder[] getOpenDesktopDocumentInfos() throws DataAccessException;

/**
 * Insert the method's description here.
 * Creation date: (6/11/2004 6:40:26 AM)
 * @return cbit.vcell.desktop.controls.DataManager
 * @param vcDataIdentifier cbit.vcell.server.VCDataIdentifier
 */
DataViewerController getDataViewerController(OutputContext outputContext, Simulation simulation, int jobIndex) throws DataAccessException;


/**
 * Insert the method's description here.
 * Creation date: (11/16/2004 7:55:08 AM)
 */
SimulationStatus getServerSimulationStatus(SimulationInfo simInfo);

/**
 * Insert the method's description here.
 * Creation date: (5/28/2004 5:54:11 PM)
 * @return cbit.vcell.client.UserPreferences
 */
UserPreferences getUserPreferences();

/**
 * Insert the method's description here.
 * Creation date: (8/26/2005 3:16:18 PM)
 * @return boolean
 */
boolean isApplet();


/**
 * Insert the method's description here.
 * Creation date: (5/21/2004 2:38:26 AM)
 * @param documentInfo cbit.vcell.document.VCDocumentInfo
 */
void managerIDchanged(String oldID, String newID);


/**
 * Insert the method's description here.
 * Creation date: (5/21/2004 2:37:55 AM)
 * @param documentType int
 */
AsynchClientTask[] newDocument(TopLevelWindowManager requester, VCDocument.DocumentCreationInfo documentCreationInfo);

AsynchClientTask[] createNewGeometryTasks(final TopLevelWindowManager requester,
		final VCDocument.DocumentCreationInfo documentCreationInfo,AsynchClientTask[] finalTasks,String okButtonText);
/**
 * Insert the method's description here.
 * Creation date: (5/21/2004 2:38:26 AM)
 * @param documentInfo cbit.vcell.document.VCDocumentInfo
 */
void openDocument(int documentType, DocumentWindowManager requester);


/**
 * Insert the method's description here.
 * Creation date: (5/21/2004 2:38:26 AM)
 * @param documentInfo cbit.vcell.document.VCDocumentInfo
 */
void openDocument(VCDocumentInfo documentInfo, TopLevelWindowManager requester, boolean inNewWindow);


	public void processComparisonResult(TMLPanel comparePanel, TopLevelWindowManager requester);


/**
 * Insert the method's description here.
 * Creation date: (5/27/2004 2:12:25 AM)
 * @param clientServerInfo cbit.vcell.client.server.ClientServerInfo
 */
void reconnect(TopLevelWindowManager requester);


/**
 * Insert the method's description here.
 * Creation date: (5/27/2004 2:59:24 PM)
 * @param vcDocument cbit.vcell.document.VCDocument
 */
void revertToSaved(DocumentWindowManager documentWindowManager);


/**
 * Insert the method's description here.
 * Creation date: (6/2/2004 2:23:41 AM)
 * @param documentWindowManager cbit.vcell.client.DocumentWindowManager
 * @param simulation cbit.vcell.solver.Simulation
 */
void runSimulation(SimulationInfo simInfo) throws DataAccessException;


/**
 * Insert the method's description here.
 * Creation date: (6/2/2004 2:23:41 AM)
 * @param documentWindowManager cbit.vcell.client.DocumentWindowManager
 * @param simulation cbit.vcell.solver.Simulation
 */
void runSimulations(ClientSimManager clientSimManager, Simulation[] simulations);


/**
 * Insert the method's description here.
 * Creation date: (5/27/2004 2:58:20 PM)
 * @param vcDocument cbit.vcell.document.VCDocument
 * @param replace boolean
 */
void saveDocument(DocumentWindowManager documentWindowManager, boolean replace);


/**
 * Insert the method's description here.
 * Creation date: (5/27/2004 2:59:24 PM)
 * @param vcDocument cbit.vcell.document.VCDocument
 */
void saveDocumentAsNew(DocumentWindowManager documentWindowManager);


/**
 * Insert the method's description here.
 * Creation date: (5/21/2004 4:18:53 AM)
 */
BioModelInfo selectBioModelInfo(TopLevelWindowManager tfWindowManager);


/**
 * Insert the method's description here.
 * Creation date: (5/21/2004 4:18:53 AM)
 */
MathModelInfo selectMathModelInfo(TopLevelWindowManager tfWindowManager);


/**
 * Insert the method's description here.
 * Creation date: (5/21/2004 4:18:53 AM)
 */
void showBNGWindow();

void showFieldDataWindow(FieldDataWindowManager.DataSymbolCallBack dataSymbolCallBack);

/**
 * Insert the method's description here.
 * Creation date: (5/21/2004 4:18:53 AM)
 */
void showDatabaseWindow();


/**
 * Insert the method's description here.
 * Creation date: (5/21/2004 4:18:53 AM)
 */
void showTestingFrameworkWindow();


/**
 * Insert the method's description here.
 * Creation date: (6/1/2004 9:58:46 PM)
 */
public void startExport(
		OutputContext outputContext,TopLevelWindowManager windowManager, ExportSpecs exportSpecs);


/**
 * Insert the method's description here.
 * Creation date: (6/2/2004 2:23:41 AM)
 * @param documentWindowManager cbit.vcell.client.DocumentWindowManager
 * @param simulation cbit.vcell.solver.Simulation
 */
void stopSimulations(ClientSimManager clientSimManager, Simulation[] simulations);


/**
 * Insert the method's description here.
 * Creation date: (5/24/2004 12:09:57 PM)
 */
void updateStatusNow();


void showComparisonResults(TopLevelWindowManager requester, XmlTreeDiff comparePanel, String baselineDesc, String modifiedDesc);

void accessPermissions(Component requester, VCDocument vcDocument);

boolean isDifferentFromBlank(int documentType, VCDocument vcDocument);

public VisitSession createNewVisitSession(String visitBinPath) throws DataAccessException;

}