package cbit.vcell.microscopy.gui.estparamwizard;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.border.EtchedBorder;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;

import org.vcell.util.gui.DialogUtils;

import cbit.vcell.microscopy.ConfidenceInterval;
import cbit.vcell.microscopy.FRAPModel;
import cbit.vcell.microscopy.FRAPOptData;
import cbit.vcell.microscopy.FRAPSingleWorkspace;
import cbit.vcell.microscopy.ProfileData;
import cbit.vcell.microscopy.ProfileSummaryData;
import cbit.vcell.microscopy.gui.AdvancedTablePanel;
import cbit.vcell.microscopy.gui.VirtualFrapLoader;
import cbit.vcell.microscopy.gui.groupableTableHeader.ColumnGroup;
import cbit.vcell.microscopy.gui.groupableTableHeader.GroupableTableColumnModel;
import cbit.vcell.microscopy.gui.groupableTableHeader.GroupableTableHeader;

/**
 * Species information panel
 */
public class AnalysisResultsTablePanel extends AdvancedTablePanel implements ActionListener, PropertyChangeListener
{
    private AnalysisTableModel anaTableModel;
    private AnalysisResultsPanel parent;
    private JLabel modelLable;
    private HyperLinkLabel hypDetail;
    private JScrollPane scrTable;
    private JPanel confidenceButtonPanel;
    private JRadioButton confidence80RadioButton = null;
    private JRadioButton confidence90RadioButton = null;
    private JRadioButton confidence95RadioButton = null;
    private JRadioButton confidence99RadioButton = null;
    private FRAPSingleWorkspace frapWorkspace;

    public AnalysisResultsTablePanel(AnalysisResultsPanel arg_parent/*may need to pass in frapstudy as parameter*/) 
    {
    	super();
    	this.parent = arg_parent;
        final GridBagLayout gridBagLayout = new GridBagLayout();
        gridBagLayout.columnWidths = new int[] {0,0};
        setLayout(gridBagLayout);
        setBorder(new EmptyBorder(5, 0, 10, 0));

        JLabel lblConfHeading = new JLabel("Models");
        lblConfHeading.setFont(new Font("Tahoma", Font.BOLD, 11));
        hypDetail = new HyperLinkLabel("Less Details", new HyperLinkListener(), 0);
        hypDetail.setHorizontalAlignment(JLabel.RIGHT);
        
        modelLable = new JLabel("3 Models");
        modelLable.setOpaque(true);
        modelLable.setBackground(new Color(166, 166, 255));
        modelLable.setBorder(BorderFactory.createEmptyBorder(1,5,1,1));

        GridBagConstraints gc1 = new GridBagConstraints();
        gc1.gridx = 0;
        gc1.gridy = 0;
        gc1.weightx = 1.0;
        gc1.anchor = GridBagConstraints.WEST;
        gc1.fill = GridBagConstraints.HORIZONTAL;
        add(lblConfHeading, gc1);

        GridBagConstraints gc2 = new GridBagConstraints();
        gc2.fill = GridBagConstraints.HORIZONTAL;
        gc2.gridx = 1;
        gc2.gridy = 0;
        gc2.anchor = GridBagConstraints.EAST;
        add(hypDetail, gc2);

        //create and add confidenceButtonPanel
        GridBagConstraints gc3 = new GridBagConstraints();
//        gc3.anchor = GridBagConstraints.WEST;
        gc3.gridx = 0;
        gc3.gridy = 3; 
        gc3.gridwidth = 2;
        gc3.weightx = 1.0;
        gc3.fill = GridBagConstraints.HORIZONTAL;
        add(getConfidenceButtonPanel(), gc3);
        
        GridBagConstraints gc4 = new GridBagConstraints();
        gc4.anchor = GridBagConstraints.WEST;
        gc4.gridx = 0;
        gc4.gridy = 1; 
        gc4.gridwidth = 2;
        gc4.weightx = 1.0;
        gc4.fill = GridBagConstraints.BOTH;
        add(modelLable, gc4);
        
        
        //create table model
        anaTableModel = new AnalysisTableModel();
        //by default, expend this table
        if (table == null) setupTable();
        GridBagConstraints gc = new GridBagConstraints();
        gc.gridx = 0;
        gc.gridy = 1;
        gc.gridwidth = 2;
        gc.weightx = 1.0;
        gc.fill = GridBagConstraints.HORIZONTAL;
        //By default, if you don't place a table in a scroll pane, the table  header  is not  shown. You need to explicitly display it.
        add(table.getTableHeader(), gc); 
        gc.weighty = 1.0;
        gc.gridy = 2;
        gc.fill = GridBagConstraints.BOTH;
        add(table, gc);
        setDetail(true);
   }

    private JPanel getConfidenceButtonPanel()
    {
    	if(confidenceButtonPanel == null)
    	{
    		confidenceButtonPanel = new JPanel();
    		confidence80RadioButton = new JRadioButton("80% Confidence Level     ", true);
    		confidence80RadioButton.addActionListener(this);
    		confidence90RadioButton = new JRadioButton("90% Confidence Level     ");
    		confidence90RadioButton.addActionListener(this);
    		confidence95RadioButton = new JRadioButton("95% Confidence Level     ");
    		confidence95RadioButton.addActionListener(this);
    		confidence99RadioButton = new JRadioButton("99% Confidence Level     ");
    		confidence99RadioButton.addActionListener(this);
    		
    		ButtonGroup bg = new ButtonGroup();
    		bg.add(confidence80RadioButton);
    		bg.add(confidence90RadioButton);
    		bg.add(confidence95RadioButton);
    		bg.add(confidence99RadioButton);
    		
    		confidenceButtonPanel.add(confidence80RadioButton);
    		confidenceButtonPanel.add(confidence90RadioButton);
    		confidenceButtonPanel.add(confidence95RadioButton);
    		confidenceButtonPanel.add(confidence99RadioButton);
    	}
    	return confidenceButtonPanel;
    }

    private class HyperLinkListener implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            boolean isDetail = hypDetail.getText().equals("Details");
            setDetail(isDetail);
            hypDetail.setText(isDetail? "Less Details" : "Details");
        }

    }

    public void setDetail(boolean isDetail) {
    	if (isDetail) {
            modelLable.setVisible(false);
            if(table != null)
            {
            	table.getTableHeader().setVisible(true);
            	table.setVisible(true);
            }
            getConfidenceButtonPanel().setVisible(true);
        }
        else {
            if (table != null) {
            	table.getTableHeader().setVisible(false);
            	table.setVisible(false);
            }
            if(frapWorkspace != null)
            {
            	modelLable.setText(frapWorkspace.getWorkingFrapStudy().getSelectedModels().size() + " Models");
            }
            modelLable.setVisible(true);
            getConfidenceButtonPanel().setVisible(false);
        }
        parent.repaint();
    }

    private void setupTable() {
        table = new StyleTable();
        table.setCellSelectionEnabled(true);
        table.setAutoCreateColumnsFromModel(false);
        table.setModel(anaTableModel);
        table.setColumnModel(new GroupableTableColumnModel());
        table.setTableHeader(new GroupableTableHeader((GroupableTableColumnModel)table.getColumnModel()));
        
        AnalysisTableRenderer tableCellRenderer = new AnalysisTableRenderer(4);// 4 digits double precision
        TableColumn[] columns = new TableColumn[AnalysisTableModel.NUM_COLUMNS];
        for (int i = 0; i < anaTableModel.getColumnCount(); i++) {
            columns[i] = new TableColumn(i, 0, tableCellRenderer, null);
            table.addColumn(columns[i]);
        }
        
        GroupableTableColumnModel cm = (GroupableTableColumnModel)table.getColumnModel();
        ColumnGroup group_df1 = new ColumnGroup(new GroupableTableCellRenderer(),"Diffusion with one diffusing component");
        group_df1.add(cm.getColumn(AnalysisTableModel.COLUMN_DIFF_ONE_PARAMETER_VAL));
        group_df1.add(cm.getColumn(AnalysisTableModel.COLUMN_DIFF_ONE_CI));
        group_df1.add(cm.getColumn(AnalysisTableModel.COLUMN_DIFF_ONE_CI_PLOT));
        ColumnGroup group_df2 = new ColumnGroup(new GroupableTableCellRenderer(), "Diffusion with two diffusing components");
        group_df2.add(cm.getColumn(AnalysisTableModel.COLUMN_DIFF_TWO_PARAMETER_VAL));
        group_df2.add(cm.getColumn(AnalysisTableModel.COLUMN_DIFF_TWO_CI));
        group_df2.add(cm.getColumn(AnalysisTableModel.COLUMN_DIFF_TWO_CI_PLOT));
        
        cm.addColumnGroup(group_df1);
        cm.addColumnGroup(group_df2);
        
        //set special editor for confidence interval plot columns
        AnalysisTableEditor ciPlotTableEditor = new AnalysisTableEditor(table);
        ciPlotTableEditor.addPropertyChangeListener(this);
        TableColumn diffOneCIPlotCol = table.getColumnModel().getColumn(AnalysisTableModel.COLUMN_DIFF_ONE_CI_PLOT);
        diffOneCIPlotCol.setCellEditor(ciPlotTableEditor);
        diffOneCIPlotCol.setPreferredWidth(68);
        diffOneCIPlotCol.setMaxWidth(68);
        TableColumn diffTwoCIPlotCol = table.getColumnModel().getColumn(AnalysisTableModel.COLUMN_DIFF_TWO_CI_PLOT);
        diffTwoCIPlotCol.setCellEditor(ciPlotTableEditor);
        diffTwoCIPlotCol.setPreferredWidth(68);
        diffTwoCIPlotCol.setMaxWidth(68);
        
        table.addMouseListener(evtHandler);
        scrTable = new JScrollPane(table);
        scrTable.setAutoscrolls(true);
    }

	public void actionPerformed(ActionEvent evt) {
		ProfileSummaryData[][] allProfileSumData = frapWorkspace.getWorkingFrapStudy().getFrapOptData().getAllProfileSummaryData();
		FRAPModel[] frapModels = frapWorkspace.getWorkingFrapStudy().getModels();
		int confidenceIdx = FRAPOptData.IDX_DELTA_ALPHA_80;
		boolean diffOneModelSignificance = true;
		boolean diffTwoModelSignificance = true;
		
		if(confidence80RadioButton.isSelected())
		{
			anaTableModel.setCurrentConfidenceLevelIndex(FRAPOptData.IDX_DELTA_ALPHA_80);
			confidenceIdx = FRAPOptData.IDX_DELTA_ALPHA_80;
		}
		else if(confidence90RadioButton.isSelected())
		{
			anaTableModel.setCurrentConfidenceLevelIndex(FRAPOptData.IDX_DELTA_ALPHA_90);
			confidenceIdx = FRAPOptData.IDX_DELTA_ALPHA_90;
		}
		else if(confidence95RadioButton.isSelected())
		{
			anaTableModel.setCurrentConfidenceLevelIndex(FRAPOptData.IDX_DELTA_ALPHA_95);
			confidenceIdx = FRAPOptData.IDX_DELTA_ALPHA_95;
		}
		else if(confidence99RadioButton.isSelected())
		{
			anaTableModel.setCurrentConfidenceLevelIndex(FRAPOptData.IDX_DELTA_ALPHA_99);
			confidenceIdx = FRAPOptData.IDX_DELTA_ALPHA_99;
		}
		//adjust best model button
		if(frapModels[FRAPModel.IDX_MODEL_DIFF_ONE_COMPONENT]!= null && frapModels[FRAPModel.IDX_MODEL_DIFF_ONE_COMPONENT].getModelParameters() != null &&
		   allProfileSumData != null && allProfileSumData[FRAPModel.IDX_MODEL_DIFF_ONE_COMPONENT]!= null && 
		   allProfileSumData[FRAPModel.IDX_MODEL_DIFF_ONE_COMPONENT][0] != null)
		{
			for(int i=0; i<FRAPModel.NUM_MODEL_PARAMETERS_ONE_DIFF; i++)
			{
				ConfidenceInterval[] intervals = allProfileSumData[FRAPModel.IDX_MODEL_DIFF_ONE_COMPONENT][i].getConfidenceIntervals();
				if(intervals[confidenceIdx].getUpperBound() == frapModels[FRAPModel.IDX_MODEL_DIFF_ONE_COMPONENT].getModelParameters()[i].getUpperBound() && 
				   intervals[confidenceIdx].getLowerBound() == frapModels[FRAPModel.IDX_MODEL_DIFF_ONE_COMPONENT].getModelParameters()[i].getLowerBound())
				{
					diffOneModelSignificance = false;
					break;
				}
			}
		}
		if(frapModels[FRAPModel.IDX_MODEL_DIFF_TWO_COMPONENTS] != null && frapModels[FRAPModel.IDX_MODEL_DIFF_TWO_COMPONENTS].getModelParameters() != null &&
		   allProfileSumData != null && allProfileSumData[FRAPModel.IDX_MODEL_DIFF_TWO_COMPONENTS]!= null && 
		   allProfileSumData[FRAPModel.IDX_MODEL_DIFF_TWO_COMPONENTS][0] != null)
		{
			for(int i=0; i<FRAPModel.NUM_MODEL_PARAMETERS_TWO_DIFF; i++)
			{
				ConfidenceInterval[] intervals = allProfileSumData[FRAPModel.IDX_MODEL_DIFF_TWO_COMPONENTS][i].getConfidenceIntervals();
				if(intervals[confidenceIdx].getUpperBound() == frapModels[FRAPModel.IDX_MODEL_DIFF_TWO_COMPONENTS].getModelParameters()[i].getUpperBound() && 
				   intervals[confidenceIdx].getLowerBound() == frapModels[FRAPModel.IDX_MODEL_DIFF_TWO_COMPONENTS].getModelParameters()[i].getLowerBound())
				{
					diffTwoModelSignificance = false;
					break;
				}
			}
		}
		if((diffOneModelSignificance && !diffTwoModelSignificance) || (! diffOneModelSignificance && diffTwoModelSignificance))
		{
			if(diffOneModelSignificance)
			{
				firePropertyChange(FRAPSingleWorkspace.PROPERTY_CHANGE_BEST_MODEL_WITH_SIGNIFICANCE, new Integer(-1), new Integer(FRAPModel.IDX_MODEL_DIFF_ONE_COMPONENT));
			}
			else if(diffTwoModelSignificance)
			{
				firePropertyChange(FRAPSingleWorkspace.PROPERTY_CHANGE_BEST_MODEL_WITH_SIGNIFICANCE, new Integer(-1), new Integer(FRAPModel.IDX_MODEL_DIFF_TWO_COMPONENTS));
			}
		}
	}

	public void propertyChange(PropertyChangeEvent evt) 
	{
		if(evt.getPropertyName().equals(FRAPSingleWorkspace.PROPERTY_CHANGE_CONFIDENCEINTERVAL_DETAILS))
		{
			if(((Integer)evt.getNewValue()).intValue() == AnalysisTableModel.COLUMN_DIFF_ONE_CI_PLOT && getTable().getSelectedRow() < FRAPModel.NUM_MODEL_PARAMETERS_ONE_DIFF)
			{
				if(frapWorkspace.getWorkingFrapStudy() != null && frapWorkspace.getWorkingFrapStudy().getProfileData_oneDiffComponent()!= null)
				{
					int paramIdx = getTable().getSelectedRow();
					ProfileData[] profileData = frapWorkspace.getWorkingFrapStudy().getProfileData_oneDiffComponent();
					//put plotpanes of different parameters' profile likelihoods into a base panel
					JPanel basePanel= new JPanel();
			    	basePanel.setLayout(new BoxLayout(basePanel, BoxLayout.Y_AXIS));

			    	ConfidenceIntervalPlotPanel plotPanel = new ConfidenceIntervalPlotPanel();
					plotPanel.setProfileSummaryData(frapWorkspace.getWorkingFrapStudy().getFrapOptData().getSummaryFromProfileData(profileData[paramIdx]));
					plotPanel.setBorder(new EtchedBorder());
					String paramName = "";
					if(profileData[paramIdx].getProfileDataElements().size() > 0)
					{
						paramName = profileData[paramIdx].getProfileDataElements().get(0).getParamName();
					}
					ProfileDataPanel profileDataPanel = new ProfileDataPanel(plotPanel, paramName);
					profileDataPanel.setProfileDataPlotDetailsVisible(true);
					basePanel.add(profileDataPanel);
					
					JScrollPane scrollPane = new JScrollPane(basePanel);
			    	scrollPane.setAutoscrolls(true);
			    	scrollPane.setPreferredSize(new Dimension(600, 600));
			    	scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
			    	scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
			    	//show plots in a dialog
			    	DialogUtils.showComponentCloseDialog(parent, scrollPane, "Profile Likelihood of Parameters");
				}
				else
				{
					DialogUtils.showErrorDialog(parent, "Confidence Intervals haven't been evaluated.");
				}
			}
			else if(((Integer)evt.getNewValue()).intValue() == AnalysisTableModel.COLUMN_DIFF_TWO_CI_PLOT && getTable().getSelectedRow() < FRAPModel.NUM_MODEL_PARAMETERS_TWO_DIFF)
			{
				if(frapWorkspace.getWorkingFrapStudy() != null && frapWorkspace.getWorkingFrapStudy().getProfileData_twoDiffComponents()!= null)
				{
					int paramIdx = getTable().getSelectedRow();
					ProfileData[] profileData = frapWorkspace.getWorkingFrapStudy().getProfileData_twoDiffComponents();
					//put plotpanes of different parameters' profile likelihoods into a base panel
					JPanel basePanel= new JPanel();
			    	basePanel.setLayout(new BoxLayout(basePanel, BoxLayout.Y_AXIS));
					
					ConfidenceIntervalPlotPanel plotPanel = new ConfidenceIntervalPlotPanel();
					plotPanel.setProfileSummaryData(frapWorkspace.getWorkingFrapStudy().getFrapOptData().getSummaryFromProfileData(profileData[paramIdx]));
					plotPanel.setBorder(new EtchedBorder());
					String paramName = "";
					if(profileData[paramIdx].getProfileDataElements().size() > 0)
					{
						paramName = profileData[paramIdx].getProfileDataElements().get(0).getParamName();
					}
					ProfileDataPanel profileDataPanel = new ProfileDataPanel(plotPanel, paramName);
					profileDataPanel.setProfileDataPlotDetailsVisible(true);
					basePanel.add(profileDataPanel);
					
					JScrollPane scrollPane = new JScrollPane(basePanel);
			    	scrollPane.setAutoscrolls(true);
			    	scrollPane.setPreferredSize(new Dimension(620, 600));
			    	scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
			    	scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
			    	//show plots in a dialog
			    	DialogUtils.showComponentCloseDialog(parent, scrollPane, "Profile Likelihood of Parameters");
				}
				else
				{
					DialogUtils.showErrorDialog(parent, "Confidence Intervals haven't been evaluated.");
				}
			}
		}
		
	}
	
	public void setFrapWorkspace(FRAPSingleWorkspace frapWorkspace)
	{
    	this.frapWorkspace = frapWorkspace;
		anaTableModel.setFrapWorkspace(frapWorkspace);
	}
	
	class GroupableTableCellRenderer extends DefaultTableCellRenderer {
	    public Component getTableCellRendererComponent(JTable table, Object value,
	    boolean selected, boolean focused, int row, int column) {
	        JTableHeader header = table.getTableHeader();
	        if (header != null) {
	            setForeground(Color.black);
	            setBackground(new Color(166, 166, 255));
	            setHorizontalAlignment(SwingConstants.CENTER);
	            setBorder(new LineBorder(Color.white,1));
	        }
	        setText(value != null ? value.toString() : " ");
	        setForeground(Color.black);
	        setBackground(new Color(166, 166, 255));
	        return this;
	    }
		public GroupableTableCellRenderer() {
			super();
		}
	}

	public int getSelectedConfidenceIndex() {
		if(confidence80RadioButton.isSelected())
		{
			return FRAPOptData.IDX_DELTA_ALPHA_80;
		}
		else if(confidence90RadioButton.isSelected())
		{
			return FRAPOptData.IDX_DELTA_ALPHA_90;
		}
		else if(confidence95RadioButton.isSelected())
		{
			return FRAPOptData.IDX_DELTA_ALPHA_95;
		}
		else if(confidence99RadioButton.isSelected())
		{
			return FRAPOptData.IDX_DELTA_ALPHA_99;
		}
		return -1;
	}
}

	
	
