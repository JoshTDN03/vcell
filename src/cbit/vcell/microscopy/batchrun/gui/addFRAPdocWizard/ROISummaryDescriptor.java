package cbit.vcell.microscopy.batchrun.gui.addFRAPdocWizard;

import java.awt.BorderLayout;
import java.awt.Point;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.ArrayList;
import java.util.Hashtable;

import javax.swing.JPanel;

import org.vcell.wizard.Wizard;
import org.vcell.wizard.WizardPanelDescriptor;

import cbit.vcell.VirtualMicroscopy.ROI;
import cbit.vcell.client.task.AsynchClientTask;
import cbit.vcell.microscopy.DataVerifyInfo;
import cbit.vcell.microscopy.FRAPData;
import cbit.vcell.microscopy.FRAPStudy;
import cbit.vcell.microscopy.FRAPSingleWorkspace;
import cbit.vcell.microscopy.batchrun.FRAPBatchRunWorkspace;
import cbit.vcell.microscopy.gui.FRAPStudyPanel;
import cbit.vcell.microscopy.gui.defineROIwizard.DefineROI_Panel;
import cbit.vcell.microscopy.gui.defineROIwizard.DefineROI_SummaryPanel;
import cbit.vcell.microscopy.gui.loaddatawizard.LoadFRAPData_SummaryPanel;

public class ROISummaryDescriptor extends WizardPanelDescriptor {
	public static final String IDENTIFIER = "BATCHRUN_RoiSummary";
	private JPanel imgPanel = null;
	FRAPBatchRunWorkspace batchRunWorkspace = null;
	
    public ROISummaryDescriptor (JPanel imagePanel) {
        super(IDENTIFIER, new DefineROI_SummaryPanel());
        imgPanel = imagePanel;
        setProgressPopupShown(false); 
        setTaskProgressKnown(false);
    }
    
    public String getNextPanelDescriptorID() {
        return RoiForErrorDescriptor.IDENTIFIER;
    }
    
    public String getBackPanelDescriptorID() {
        return BackgroundROIDescriptor.IDENTIFIER;
    }

    public void aboutToDisplayPanel() 
    {
    	((DefineROI_SummaryPanel)getPanelComponent()).setLoadInfo(getBatchRunWorkspace().getWorkingFrapStudy());
	} 
    
	public ArrayList<AsynchClientTask> preBackProcess()
    {
    	//create AsynchClientTask arraylist
		ArrayList<AsynchClientTask> taskArrayList = new ArrayList<AsynchClientTask>();
		
		final String nextROIStr = FRAPData.VFRAP_ROI_ENUM.ROI_BACKGROUND.name();
		AsynchClientTask setCurrentROITask = new AsynchClientTask("", AsynchClientTask.TASKTYPE_SWING_BLOCKING) 
		{
			public void run(Hashtable<String, Object> hashTable) throws Exception
			{
				//save current ROI and load ROI in the panel it goes next to
				((BatchRunROIImgPanel)imgPanel).setCurrentROI(nextROIStr, false);
			}
		};
		taskArrayList.add(setCurrentROITask);
		return taskArrayList;
    } 
	
	//save the startingIndex before the panel disappears
    public ArrayList<AsynchClientTask> preNextProcess()
    {
    	//create AsynchClientTask arraylist
		ArrayList<AsynchClientTask> taskArrayList = new ArrayList<AsynchClientTask>();
		
		AsynchClientTask verifyLoadedDataTask = new AsynchClientTask("", AsynchClientTask.TASKTYPE_SWING_BLOCKING) 
		{
			public void run(Hashtable<String, Object> hashTable) throws Exception
			{
				String msg = ((DefineROI_SummaryPanel)getPanelComponent()).checkInputValidity();
				if(msg.equals(""))
				{
					int startIndex = ((DefineROI_SummaryPanel)getPanelComponent()).getStartingIndex();
					FRAPStudy fStudy = getBatchRunWorkspace().getWorkingFrapStudy();
					
					//check ROI void/discontinuous location
					Point internalVoidLocation = ROI.findInternalVoid(fStudy.getFrapData().getRoi(FRAPData.VFRAP_ROI_ENUM.ROI_CELL.name()));
					if(internalVoidLocation != null){
						throw new Exception("CELL ROI has unfilled internal void area at image location "+
								"x="+internalVoidLocation.x+",y="+internalVoidLocation.y+"\n"+
								"Use ROI editing tools to completely define the CELL ROI");
					}
					Point[] distinctCellAreaLocations = ROI.checkContinuity(fStudy.getFrapData().getRoi(FRAPData.VFRAP_ROI_ENUM.ROI_CELL.name()));
					if(distinctCellAreaLocations != null){
						throw new Exception("CELL ROI has at least 2 discontinuous areas at image locations \n"+
								"x="+distinctCellAreaLocations[0].x+",y="+distinctCellAreaLocations[0].y+
								" and "+
								"x="+distinctCellAreaLocations[1].x+",y="+distinctCellAreaLocations[1].y+"\n"+
						"Use ROI editing tools to define a single continuous CELL ROI");				
					}
					if(!fStudy.getFrapData().checkROIConstraints(imgPanel))
					{
						fStudy.setStartingIndexForRecovery(startIndex);
						getBatchRunWorkspace().setWorkingFRAPStudy(fStudy);
						//generate ROI rings
						fStudy.refreshDependentROIs();
					}
				}
				else throw new Exception(msg);
			}
		};
		
		taskArrayList.add(verifyLoadedDataTask);
		return taskArrayList;
    }
	
    public FRAPBatchRunWorkspace getBatchRunWorkspace() {
		return batchRunWorkspace;
	}
    
	public void setBatchRunWorkspace(FRAPBatchRunWorkspace batchRunWorkspace) {
		this.batchRunWorkspace = batchRunWorkspace;
	}
}
