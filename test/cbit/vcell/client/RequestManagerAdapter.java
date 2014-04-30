package cbit.vcell.client;

import java.awt.Component;

import org.vcell.util.DataAccessException;
import org.vcell.util.document.BioModelInfo;
import org.vcell.util.document.MathModelInfo;
import org.vcell.util.document.VCDataIdentifier;
import org.vcell.util.document.VCDocument;
import org.vcell.util.document.VCDocumentInfo;
import org.vcell.util.document.UserLoginInfo.DigestedPassword;
import org.vcell.util.document.VCDocument.DocumentCreationInfo;
import org.vcell.util.importer.PathwayImportPanel.PathwayImportOption;

import cbit.vcell.client.FieldDataWindowManager.DataSymbolCallBack;
import cbit.vcell.client.TopLevelWindowManager.OpenModelInfoHolder;
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
import cbit.xml.merge.TMLPanel;
import cbit.xml.merge.XmlTreeDiff;

public class RequestManagerAdapter implements RequestManager {
	@Override
	public void changeGeometry(DocumentWindowManager requester,	SimulationContext simContext) {}
	@Override
	public boolean closeWindow(String managerID, boolean exitIfLast) { return false; }
	@Override
	public XmlTreeDiff compareWithOther(VCDocumentInfo vcDoc1, VCDocumentInfo vcDoc2) { return null; }
	@Override
	public XmlTreeDiff compareWithSaved(VCDocument document) { return null; }
	@Override
	public void connectAs(String user, DigestedPassword digestedPassword, TopLevelWindowManager requester) {}
	@Override
	public void connectToServer(TopLevelWindowManager requester,ClientServerInfo clientServerInfo) throws Exception {}
	@Override
	public void createMathModelFromApplication(BioModelWindowManager requester, String name, SimulationContext simContext) {}
	@Override
	public void curateDocument(VCDocumentInfo vcDocInfo, int curateType, TopLevelWindowManager requester) {}
	@Override
	public void updateUserRegistration(DocumentWindowManager docWindowManager, boolean bNewUser) { }
	@Override
	public void sendLostPassword(DocumentWindowManager docWindowManager, String userid) {}
	@Override
	public void deleteDocument(VCDocumentInfo documentInfo,TopLevelWindowManager requester) {}
	@Override
	public void exitApplication() {}
	@Override
	public void exportDocument(TopLevelWindowManager manager) {}
	@Override
	public AsynchMessageManager getAsynchMessageManager() { return null; }
	@Override
	public ConnectionStatus getConnectionStatus() { return null; }
	@Override
	public DataManager getDataManager(OutputContext outputContext, VCDataIdentifier vcDataID, boolean isSpatial) throws DataAccessException { return null; }
	@Override
	public DocumentManager getDocumentManager() { return null; }
	@Override
	public MergedDatasetViewerController getMergedDatasetViewerController(OutputContext outputContext, VCDataIdentifier vcdId, boolean expectODEData) throws DataAccessException { return null; }
	@Override
	public OpenModelInfoHolder[] getOpenDesktopDocumentInfos(boolean bIncludeSimulations) throws DataAccessException { return null;	}
	@Override
	public DataViewerController getDataViewerController(OutputContext outputContext, Simulation simulation, int jobIndex) throws DataAccessException { return null;	}
	@Override
	public SimulationStatus getServerSimulationStatus(SimulationInfo simInfo) { return null; }
	@Override
	public UserPreferences getUserPreferences() { return null; }
	@Override
	public boolean isApplet() {	return false; }
	@Override
	public void managerIDchanged(String oldID, String newID) {}
	@Override
	public AsynchClientTask[] newDocument(TopLevelWindowManager requester, DocumentCreationInfo documentCreationInfo) { return null; }
	@Override
	public AsynchClientTask[] createNewGeometryTasks(TopLevelWindowManager requester, DocumentCreationInfo documentCreationInfo, AsynchClientTask[] finalTasks, String okButtonText) { return null; }
	@Override
	public void openDocument(int documentType, DocumentWindowManager requester) {}
	@Override
	public void openDocument(VCDocumentInfo documentInfo, TopLevelWindowManager requester, boolean inNewWindow) {}
	@Override
	public void openPathway(DocumentWindowManager requester, PathwayImportOption pathwayImportOption) {}
	@Override
	public void processComparisonResult(TMLPanel comparePanel, TopLevelWindowManager requester) {}
	@Override
	public void reconnect(TopLevelWindowManager requester) {}
	@Override
	public void revertToSaved(DocumentWindowManager documentWindowManager) {}
	@Override
	public void runSimulation(SimulationInfo simInfo, int numSimulationScanJobs) throws DataAccessException {}
	@Override
	public void runSimulations(ClientSimManager clientSimManager,Simulation[] simulations) {}
	@Override
	public void saveDocument(DocumentWindowManager documentWindowManager, boolean replace) {}
	@Override
	public void saveDocumentAsNew(DocumentWindowManager documentWindowManager) {}
	@Override
	public BioModelInfo selectBioModelInfo(TopLevelWindowManager tfWindowManager) { return null; }
	@Override
	public MathModelInfo selectMathModelInfo(TopLevelWindowManager tfWindowManager) { return null;}
	@Override
	public void showBNGWindow() {}
	@Override
	public void showFieldDataWindow(DataSymbolCallBack dataSymbolCallBack) {}
	@Override
	public void showTestingFrameworkWindow() {}
	@Override
	public void startExport(OutputContext outputContext,TopLevelWindowManager windowManager, ExportSpecs exportSpecs) {}
	@Override
	public void stopSimulations(ClientSimManager clientSimManager,Simulation[] simulations) {}
	@Override
	public void updateStatusNow() {}
	@Override
	public void showComparisonResults(TopLevelWindowManager requester,XmlTreeDiff comparePanel, String baselineDesc,String modifiedDesc) {}
	@Override
	public void accessPermissions(Component requester,VCDocument vcDocument) {}
	@Override
	public boolean isDifferentFromBlank(int documentType,VCDocument vcDocument) {return false;}
};
