package cbit.vcell.client.task;
import java.util.Hashtable;
import java.util.Vector;

import org.vcell.util.BeanUtils;
import org.vcell.util.UserCancelException;
import org.vcell.util.document.VCDocument;
import org.vcell.util.document.Version;
import org.vcell.util.document.VersionFlag;

import cbit.vcell.biomodel.BioModel;
import cbit.vcell.client.DocumentWindowManager;
import cbit.vcell.client.PopupGenerator;
import cbit.vcell.client.UserMessage;
import cbit.vcell.clientdb.DocumentManager;
import cbit.vcell.mathmodel.MathModel;
import cbit.vcell.solver.Simulation;
import cbit.vcell.solver.SimulationInfo;
import cbit.vcell.solver.ode.gui.SimulationStatus;

/**
 * Insert the type's description here.
 * Creation date: (5/31/2004 6:03:16 PM)
 * @author: Ion Moraru
 */
public class CheckBeforeDelete extends AsynchClientTask {
	
	public CheckBeforeDelete() {
		super("Checking for successfull save and possible lost results", TASKTYPE_NONSWING_BLOCKING);
	}


/**
 * Insert the method's description here.
 * Creation date: (6/1/2004 3:44:03 PM)
 * @return cbit.vcell.solver.SolverResultSetInfo[]
 * @param bioModel cbit.vcell.biomodel.BioModel
 */
private Simulation[] checkLostResults(BioModel oldBioModel, BioModel newlySavedBioModel, cbit.vcell.clientdb.DocumentManager documentManager, Simulation submittedSimulations[]) throws Exception {
	//
	// before deleting old version, prompt user if old simulation results will not be availlable in new edition
	//
	Vector<Simulation> lostResultsSimulationList = new Vector<Simulation>();
	Simulation oldSimulations[] = oldBioModel.getSimulations();
	for (int i = 0; i < oldSimulations.length; i++){
		Simulation oldSimulation = oldSimulations[i];
		SimulationStatus simStatus = null;
		SimulationInfo oldSimInfo = oldSimulation.getSimulationInfo();
		if (oldSimInfo!=null){
			//
			// we need to ask for previous sim results (here we need possible translation to ask for parent's results).
			//
			simStatus = documentManager.getServerSimulationStatus(oldSimInfo.getAuthoritativeVCSimulationIdentifier());
		}
		if (simStatus != null && simStatus.getHasData()) {
			//
			// results exist in old version (the BioModel to be deleted) for SimulationInfo "oldSimInfo"
			// Users should be warned when they are going to loose any simulation results in any unexpected way.
			//
			// WARN if the lost data is because new simulation is not mathematically equivalent to old edition
			//       (different MathDescription key)
			//
			// IGNORE if the lost data is from edits of a Simulation only (same MathDescription)
			//       (same MathDescription key, different Simulation key)
			//
			// IGNORE if Simulation has been deleted 
			//       (Simulation not found in current BioModel)
			//
			// IGNORE if Simulation has been submitted for running 
			//
			boolean bDataInNewEdition = false;
			Simulation newSimulations[] = newlySavedBioModel.getSimulations();
			Simulation correspondingSimulation = null;
			for (int j = 0; j < newSimulations.length; j++){
				if (newSimulations[j].getName().equals(oldSimulation.getName())){
					correspondingSimulation = newSimulations[j];
					if (correspondingSimulation.getKey().equals(oldSimulation.getKey())){
						//
						// exactly same simulation (same key), so no lost data
						//
						bDataInNewEdition = true;
					}else if (correspondingSimulation.getSimulationVersion().getParentSimulationReference()!=null){
						//
						// new simulation changed but points to same results
						//
						bDataInNewEdition = true;
					}
					break;
				}
			}
			if (!bDataInNewEdition && correspondingSimulation!=null){
				//
				// result set (for "rsInfo") will be lost, should we ignore this fact?
				//
				boolean bIgnore = false;

				//
				// ignore if only Simulation has been edited (same MathDescription)
				//
				if (correspondingSimulation.getMathDescription().getKey().equals(oldSimulation.getMathDescription().getKey())){
					bIgnore = true;
				}

				//
				// ignore if new simulation has been submitted for running (who cares if the old data is lost)
				//
				for (int j = 0; submittedSimulations!=null && j < submittedSimulations.length; j++){
					if (correspondingSimulation.getName().equals(submittedSimulations[j].getName())){
						bIgnore = true;
					}
				}

				//
				// if don't ignore this data loss, then add results set to list of warnings
				//
				if (!bIgnore) {
					lostResultsSimulationList.add(correspondingSimulation);
				}
			}
		}
	}
	return (Simulation[])BeanUtils.getArray(lostResultsSimulationList, Simulation.class);
}


/**
 * Insert the method's description here.
 * Creation date: (6/1/2004 3:44:03 PM)
 * @return cbit.vcell.solver.SolverResultSetInfo[]
 * @param mathmodel cbit.vcell.mathmodel.Mathmodel
 */
private Simulation[] checkLostResults(MathModel oldMathmodel, MathModel newlySavedMathmodel, cbit.vcell.clientdb.DocumentManager documentManager, Simulation submittedSimulations[]) throws Exception {
	//
	// before deleting old version, prompt user if old simulation results will not be availlable in new edition
	//
	Vector<Simulation> lostResultsSimulationList = new Vector<Simulation>();
	Simulation oldSimulations[] = oldMathmodel.getSimulations();
	for (int i = 0; i < oldSimulations.length; i++){
		Simulation oldSimulation = oldSimulations[i];
//		SolverResultSetInfo rsInfo = null;
		SimulationStatus simStatus = null;
		SimulationInfo oldSimInfo = oldSimulation.getSimulationInfo();
		if (oldSimInfo!=null){
			//
			// we need to ask for previous sim results (here we need possible translation to ask for parent's results).
			//
			simStatus = documentManager.getServerSimulationStatus(oldSimInfo.getAuthoritativeVCSimulationIdentifier());
		}
		if (simStatus != null && simStatus.getHasData()) {
			//
			// results exist in old version (the Mathmodel to be deleted) for SimulationInfo "oldSimInfo"
			// Users should be warned when they are going to loose any simulation results in any unexpected way.
			//
			// WARN if the lost data is because new simulation is not mathematically equivalent to old edition
			//       (different MathDescription key)
			//
			// IGNORE if the lost data is from edits of a Simulation only (same MathDescription)
			//       (same MathDescription key, different Simulation key)
			//
			// IGNORE if Simulation has been deleted 
			//       (Simulation not found in current Mathmodel)
			//
			// IGNORE if Simulation has been submitted for running 
			//
			boolean bDataInNewEdition = false;
			Simulation newSimulations[] = newlySavedMathmodel.getSimulations();
			Simulation correspondingSimulation = null;
			for (int j = 0; j < newSimulations.length; j++){
				if (newSimulations[j].getName().equals(oldSimulation.getName())){
					correspondingSimulation = newSimulations[j];
					if (correspondingSimulation.getKey().equals(oldSimulation.getKey())){
						//
						// exactly same simulation (same key), so no lost data
						//
						bDataInNewEdition = true;
					}else if (correspondingSimulation.getSimulationVersion().getParentSimulationReference()!=null){
						//
						// new simulation changed but points to same results
						//
						bDataInNewEdition = true;
					}
					break;
				}
			}
			if (!bDataInNewEdition && correspondingSimulation!=null){
				//
				// result set (for "rsInfo") will be lost, should we ignore this fact?
				//
				boolean bIgnore = false;

				//
				// ignore if only Simulation has been edited (same MathDescription)
				//
				if (correspondingSimulation.getMathDescription().getKey().equals(oldSimulation.getMathDescription().getKey())){
					bIgnore = true;
				}

				//
				// ignore if new simulation has been submitted for running (who cares if the old data is lost)
				//
				for (int j = 0; submittedSimulations!=null && j < submittedSimulations.length; j++){
					if (correspondingSimulation.getName().equals(submittedSimulations[j].getName())){
						bIgnore = true;
					}
				}

				//
				// if don't ignore this data loss, then add results set to list of warnings
				//
				if (!bIgnore) {
					lostResultsSimulationList.add(correspondingSimulation);
				}
			}
		}
	}
	return (Simulation[])BeanUtils.getArray(lostResultsSimulationList, Simulation.class);
}

/**
 * Insert the method's description here.
 * Creation date: (5/31/2004 6:04:14 PM)
 * @param hashTable java.util.Hashtable
 * @param clientWorker cbit.vcell.desktop.controls.ClientWorker
 */
public void run(Hashtable<String, Object> hashTable) throws Exception {
	DocumentWindowManager documentWindowManager = (DocumentWindowManager)hashTable.get("documentWindowManager");
//	JFrame currentDocumentWindow = (JFrame)hashTable.get("currentDocumentWindow");
	VCDocument currentDocument = documentWindowManager.getVCDocument();
	if (! hashTable.containsKey("savedDocument")) {
		throw new RuntimeException("CheckBeforeDelete task called although SaveDocument task did not complete - should have aborted!!");
	}

	//Don't delete ARCHIVE or PUBLISH  documents
	if(!currentDocument.getVersion().getFlag().compareEqual(VersionFlag.Current)){
		hashTable.put("conditionalSkip", new String[] {DeleteOldDocument.class.getName()});
		return;
	}

	
	DocumentManager documentManager = (DocumentManager)hashTable.get("documentManager");
	Simulation simulations[] = (Simulation[])hashTable.get("simulations");
	VCDocument savedDocument = (VCDocument)hashTable.get("savedDocument");
	// just to make sure, verify that we actually did save a new edition
	Version oldVersion = currentDocument.getVersion();
	Version newVersion = savedDocument.getVersion();
	if (newVersion.getVersionKey().compareEqual(oldVersion.getVersionKey())) {
		//throw new DataAccessException("CheckBeforeDelete task called but saved document has same version with current document");
		hashTable.put("conditionalSkip", new String[] {DeleteOldDocument.class.getName()});
		return;
	}
	// we saved a new one, now check for lost simulation data and warn the user
	Simulation[] simulationsWithLostResults = null;
	switch (currentDocument.getDocumentType()) {
		case VCDocument.BIOMODEL_DOC: {
			simulationsWithLostResults = checkLostResults((BioModel)currentDocument, (BioModel)savedDocument, documentManager, simulations);
			break;
		}
		case VCDocument.MATHMODEL_DOC: {
			simulationsWithLostResults = checkLostResults((MathModel)currentDocument, (MathModel)savedDocument, documentManager, simulations);
			break;
		}
		case VCDocument.GEOMETRY_DOC: {
			return; // nothing to check for in this case
		}
	}
	boolean bLost = simulationsWithLostResults != null && simulationsWithLostResults.length > 0;
	if (bLost) {
		String choice = PopupGenerator.showWarningDialog(documentWindowManager, documentWindowManager.getUserPreferences(), UserMessage.question_LostResults, null);
		if (choice.equals(UserMessage.OPTION_SAVE_AS_NEW_EDITION)){
			// user canceled deletion
			throw UserCancelException.CANCEL_DELETE_OLD;
		}
	}
}

}