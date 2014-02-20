import sys, os
sys.path.append("D:/Developer/VTK6.0.0/build/Wrapping/Python")
sys.path.append("D:/Developer/VTK6.0.0/build/Wrapping/Python")

import vtk
import vtk.qt4
import vtk.qt4.QVTKRenderWindowInteractor
print("vtk imported without an exception")

from visContextAbstract import *


class visContextVTK(visContextAbstract):

    def __init__(self):
        visContextAbstract.__init__(self)
        self._var = ""
        self._frame = None
        self._widget = None
        self._dataset = None
        self._plot = None
        assert isinstance(self._widget,vtk.qt4.QVTKRenderWindowInteractor.QVTKRenderWindowInteractor) or self._widget == None
        assert isinstance(self._dataset,Dataset) or self._dataset == None
        assert isinstance(self._plot,Plot) or self._dataset == None
 
        self._variable = None

        self._currentOperator = None  # None, "Slice", "Clip"
        self._currentPlot = None      # None, ("Pseudocolor", varName)
        self._operatorEnabled = False
        self._operatorAxis = 0 # 0=x, 1=y, 2=z
        self._operatorPercent = 50
        self._operatorProject2d = False

    def getRenderWindow(self,parent):
        if self._frame == None:
            self._parent = parent
            self._frame = QtGui.QFrame(parent)
            self._frame.setObjectName("vtkRenderWindowFrame1")
            self._frame.setMinimumSize(799, 800)
            self._hbox = QtGui.QHBoxLayout(self._frame)
            self._frame.setLayout(self._hbox)
            self._widget = vtk.qt4.QVTKRenderWindowInteractor.QVTKRenderWindowInteractor(self._frame)
            self._hbox.addWidget(self._widget)
            self._widget.Initialize()
            self._widget.Start()

            renderer = vtk.vtkRenderer()
            renderer.SetBackground(0,0,0); #  Background color white
            renWin = self._widget.GetRenderWindow()
            renWin.AddRenderer(renderer)
            self._widget.repaint()

        return self._frame

    def getDataset(self):
        return self._dataset

    def open(self,filename):
        print("in visContextVTK.open(): begin")
        self._var = None
        filename = str(filename)
        print("filename is "+filename)
        self._dataset = Dataset(filename)
        self._plot = PseudocolorPlot(self._dataset,None)
        actor = self._plot.getActor()
       # Add the actor to the scene

        renderer = vtk.vtkRenderer()
        renderer.AddActor(actor);
        renderer.SetBackground(1,0,0); #  Background color white

        self.renWin=self._widget.GetRenderWindow()
        self.renWin.AddRenderer(renderer)

    def getVariableNames(self):
        if self._dataset != None:
            return self._dataset.getVariableNames();
        raise Exception("no variables ... no dataset")

    def setVariable(self,var):
        print("in visContextVTK.setVariable("+str(var)+"): begin")
        self._plot.setVariable(var)
        actor = self._plot.getActor()
       # Add the actor to the scene

        renderer = vtk.vtkRenderer()
        renderer.AddActor(actor);
        renderer.SetBackground(1,0,0); #  Background color white

        self.renWin=self._widget.GetRenderWindow()
        self.renWin.AddRenderer(renderer)
        self._widget.repaint()

    def quit(self):
        pass # nothing special to close

    def setOperatorEnabled(self, bEnable):
        assert isinstance(bEnable,bool)
        self._operatorEnabled = bEnable
        self._updateDisplay()

    def getOperatorEnabled(self):
        return self._operatorEnabled

    def setOperatorAxis(self, axis):
        assert (axis in (0,1,2))
        self._operatorAxis = axis;
        self._updateDisplay()
       
    def getOperatorAxis(self):
        return self._operatorAxis
    
    def setOperatorPercent(self, percent):
        assert ((percent >=0) and (percent <=100))
        self._operatorPercent = percent
        self._updateDisplay()

    def getOperatorPercent(self):
        return self._operatorPercent

    def setOperatorProject2d(self, bProject2d):
        assert isinstance(bProject2d, bool)
        self._operatorProject2d = bProject2d
        self._updateDisplay()

    def getOperatorProject2d(self):
        return self._operatorProject2d

    def setTimeIndex(self, index):
        nStates = visit.TimeSliderGetNStates()      
        print(index)
        visit.SetTimeSliderState(index) 
        visit.DrawPlots()

    def getNumberOfTimePoints(self):
        if (self._dataset == None):
            return 0
        return self._dataset.getNumberOfTimePoints()    
    
    def getTimes(self):
        if (self._dataset == None):
            return []
        return self._dataset.getTimes()


class Dataset(object):
    def __init__(self, filename):
        self._filename = filename
        self._ugrid = None
        self._currentTimeIndex = None
        self._currentTimeIndex = 0
        assert isinstance(self._currentTimeIndex,int) or self._currentTimeIndex == None
        assert isinstance(self._ugrid,vtk.vtkUnstructuredGrid) or self._ugrid == None

    def _getGrid(self):
        if self._ugrid == None:
            reader = vtk.vtkXMLUnstructuredGridReader()
            reader.SetFileName(str(self._filename))
            reader.Update()
            self._ugrid = reader.GetOutput()
        assert isinstance(self._ugrid,vtk.vtkUnstructuredGrid)
        return self._ugrid

    def getFilename(self):
        return self._filename

    def getVariableNames(self):
        cellData = self._getGrid().GetCellData()
        return [cellData.GetArrayName(i) for i in range(cellData.GetNumberOfArrays())]

    def getNumberOfTimePoints(self):
        return 1

    def getNumberOfTimePoints(self):
        return [0.0, ]

class Plot(object):
    def __init__(self,dataset):
        assert isinstance(dataset,Dataset)
        self._dataset = dataset
        self._actor = vtk.vtkActor()
        self._lut = None
        assert isinstance(self._lut,vtk.vtkLookupTable) or self._lut == None

    def getDataset(self):
        return self._dataset

    def _getLUT(self,low,high):
        lut = vtk.vtkLookupTable()
        lut.SetNumberOfTableValues(256)
        lut.SetHueRange(0.0,0.6)
        lut.SetValueRange(1,1)
        lut.SetAlphaRange(1,1)
        lut.SetRange(low,high)
        lut.Build()    
        return lut

class PseudocolorPlot(Plot):
    def __init__(self,dataset,var):
        super(PseudocolorPlot,self).__init__(dataset)
        self._var = var

    def getVar(self):
        return self._var

    def setVariable(self,var):
        self._var = str(var)

    def getActor(self):
        uGrid = self._dataset._getGrid()       
        numCells = uGrid.GetNumberOfCells()
        cellData = uGrid.GetCellData()
        (gridMinX,gridMaxX,gridMinY,gridMaxY,gridMinZ,gridMaxZ) = uGrid.GetBounds()
 
        if self._var == None:
            self._var = cellData.GetArrayName(0)
        cellScalars1 = cellData.GetArray(self._var)
        if cellScalars1 != None:
            (dataMin1,dataMax1) = cellScalars1.GetRange()
        else:
            dataMin1=0
            dataMax1=1

        #plane = vtk.vtkPlane()
        #plane.SetNormal(0,-1,0)
        #plane.SetOrigin(30,30,30)

        #clip = vtk.vtkClipDataSet()
        #clip.SetClipFunction(plane)
        #clip.GenerateClipScalarsOn()
        ##clippedGrid.SetValue(0.5)

        mapper1 = vtk.vtkDataSetMapper()
        if vtk.vtkVersion.GetVTKMajorVersion() == 6: 
        #    clip.SetInputConnection(uGrid)
        #    g = vtk.vtkDataSetSurfaceFilter()
        #    g.SetInputConnection(clip.GetOutputPort())
        #    mapper1 = vtk.vtkPolyDataMapper()
        #    mapper1.SetInputConnection(g.GetOutputPort())
        #    #mapper1.SetInputData(clip.getOutput())
            mapper1.SetInputData(uGrid)  # vtk 6.0
        else:
        #    clip.SetInput(uGrid)
        #    #clip.InsideOutOn()
        #    clip.Update()
        #    mapper1.SetInput(clip.GetOutput())      # vtk 5.0
            mapper1.SetInput(uGrid)      # vtk 5.0

        mapper1.SetLookupTable(self._getLUT(dataMin1,dataMax1))
        mapper1.SetScalarRange(dataMin1,dataMax1)
        mapper1.SelectColorArray(self._var)
        mapper1.ScalarVisibilityOn()
        mapper1.SetScalarModeToUseCellFieldData()
       # mapper1.SetColorModeToMapScalars()
        mapper1.Update()

        self._actor = vtk.vtkActor();
        self._actor.SetMapper(mapper1);
        #actor1.GetProperty().SetRepresentationToWireframe();
        #actor1.GetProperty().EdgeVisibilityOn();
        self._actor.GetProperty().SetEdgeColor(0,0,0);
        #actor1.GetProperty().SetLineWidth(1.5);

        return self._actor
 