package org.vcell.vis.vtk;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import org.vcell.vis.vismesh.VisDataset.VisDomain;
import org.vcell.vis.vismesh.VisIrregularPolyhedron;
import org.vcell.vis.vismesh.VisIrregularPolyhedron.PolyhedronFace;
import org.vcell.vis.vismesh.VisMesh;
import org.vcell.vis.vismesh.VisMeshData;
import org.vcell.vis.vismesh.VisPoint;
import org.vcell.vis.vismesh.VisPolygon;
import org.vcell.vis.vismesh.VisPolyhedron;
import org.vcell.vis.vismesh.VisTetrahedron;
import org.vcell.vis.vismesh.VisVoxel;

import vtk.vtkCell;
import vtk.vtkCellData;
import vtk.vtkDelaunay3D;
import vtk.vtkDoubleArray;
import vtk.vtkFieldData;
import vtk.vtkFloatArray;
import vtk.vtkGeometryFilter;
import vtk.vtkIdList;
import vtk.vtkIdTypeArray;
import vtk.vtkLine;
import vtk.vtkNativeLibrary;
import vtk.vtkObjectBase;
import vtk.vtkPointData;
import vtk.vtkPoints;
import vtk.vtkPolyData;
import vtk.vtkPolygon;
import vtk.vtkPolyhedron;
import vtk.vtkQuad;
import vtk.vtkTetra;
import vtk.vtkTriangle;
import vtk.vtkUnstructuredGrid;
import vtk.vtkUnstructuredGridGeometryFilter;
import vtk.vtkUnstructuredGridReader;
import vtk.vtkUnstructuredGridWriter;
import vtk.vtkVoxel;
import vtk.vtkWindowedSincPolyDataFilter;
import vtk.vtkXMLFileReadTester;
import vtk.vtkXMLUnstructuredGridReader;
import vtk.vtkXMLUnstructuredGridWriter;
//import vtk.vtkXdmfWriter;

public class VtkGridUtils {
	
	// Load VTK library and print which library was not properly loaded
	static {
		try {
			// note, have to set PATH environment variable to include these libraries before execution (or -Dvtk.lib.dir=../nativelibs/win64 may work on command line).
			vtkNativeLibrary.LoadAllNativeLibraries();
		}catch (Exception e){
			e.printStackTrace(System.out);
		}
		vtkNativeLibrary.DisableOutputWindow(null);
		vtkObjectBase.JAVA_OBJECT_MANAGER.getAutoGarbageCollector().SetAutoGarbageCollection(true);
	}

	public vtkUnstructuredGrid getVtkGrid(VisDomain visDomain) throws IOException{
		
		vtkPoints vtkpoints = new vtkPoints();
		VisMesh vMesh = visDomain.getVisMesh();
		for (VisPoint visPoint : vMesh.getPoints()) {
		    vtkpoints.InsertNextPoint(visPoint.x,visPoint.y,visPoint.z);
		}

		vtkUnstructuredGrid vtkgrid = new vtkUnstructuredGrid();
		vtkgrid.Allocate(vMesh.getPoints().size(), vMesh.getPoints().size());
		vtkgrid.SetPoints(vtkpoints);

		int quadType = new vtkQuad().GetCellType();
		int lineType = new vtkLine().GetCellType();
		int polygonType = new vtkPolygon().GetCellType();
		int triangleType = new vtkTriangle().GetCellType();
		int polyhedronType = new vtkPolyhedron().GetCellType();
		int voxelType = new vtkVoxel().GetCellType();
		int tetraType = new vtkTetra().GetCellType();

		for (VisPolygon visPolygon : vMesh.getPolygons()) {
		    vtkIdList pts = new vtkIdList();
		    int[] polygonPoints = visPolygon.getPointIndices();
		    for (int p : polygonPoints){
		        pts.InsertNextId(p);
		    }

		    int numPoints = polygonPoints.length;
		    if (numPoints == 4){
		        vtkgrid.InsertNextCell(quadType,pts);
		    }else if (numPoints == 3){
		        vtkgrid.InsertNextCell(triangleType,pts);
		    }else{
		        vtkgrid.InsertNextCell(polygonType,pts);
		    }
		}
		boolean bInitializedFaces = false;
		for (VisPolyhedron visPolyhedron : vMesh.getPolyhedra()) {
			if (visPolyhedron instanceof VisVoxel){
				VisVoxel voxel = (VisVoxel)visPolyhedron;
			    vtkIdList pts = new vtkIdList();
			    int[] polyhedronPoints = voxel.getPointIndices();
			    for (int p : polyhedronPoints){
			        pts.InsertNextId(p);
			    }
		        vtkgrid.InsertNextCell(voxelType,pts);
			}else if (visPolyhedron instanceof VisTetrahedron){
				VisTetrahedron visTet = (VisTetrahedron)visPolyhedron;
			    vtkIdList pts = new vtkIdList();
			    int[] tetPoints = visTet.getPointIndices();
			    for (int p : tetPoints){
			        pts.InsertNextId(p);
			    }
		        vtkgrid.InsertNextCell(tetraType,pts);
			}else if (visPolyhedron instanceof VisIrregularPolyhedron){
				VisIrregularPolyhedron irregPolyhedron = (VisIrregularPolyhedron)visPolyhedron;
			    vtkIdList faceStreamList = new vtkIdList();
			    int[] faceStream = irregPolyhedron.getVtkFaceStream();
			    for (int p : faceStream){
			        faceStreamList.InsertNextId(p);
			    }
			    if (!bInitializedFaces && vtkgrid.GetNumberOfCells()>0){
				    vtkgrid.InitializeFacesRepresentation(vtkgrid.GetNumberOfCells());
				}
				bInitializedFaces = true;
				vtkgrid.InsertNextCell(polyhedronType, faceStreamList);
			}else{
				throw new RuntimeException("unsupported polyhedron type: "+visPolyhedron.getClass().getName());
			}
		}
		
		vtkgrid.BuildLinks();
		
		int numCells = vtkgrid.GetCells().GetNumberOfCells();

		vtkCellData cellData = vtkgrid.GetCellData();
		VisMeshData meshData = visDomain.getVisMeshData();
		for (String varName : meshData.getVarNames()){
			double[] data = meshData.getData(varName);
		    vtkDoubleArray cellScalars1 = new vtkDoubleArray();
		    cellScalars1.SetNumberOfComponents(1);
		    cellScalars1.SetNumberOfTuples(numCells);
		    cellScalars1.SetName(varName);
//		    System.out.println("saving var "+varName);
		    cellScalars1.SetJavaArray(data);
			cellData.AddArray(cellScalars1);
		}
		
		vtkFieldData fieldData = vtkgrid.GetFieldData();
		vtkFloatArray timeValue = new vtkFloatArray();
		timeValue.SetNumberOfComponents(1);
		timeValue.SetNumberOfTuples(1);
		timeValue.SetName("TIME");
	    float time = (float)visDomain.getVisMeshData().getTime();
	    float timeArray[] = new float[] { time };
//		System.out.println("setting time "+time);
		timeValue.SetJavaArray(timeArray);
		fieldData.AddArray(timeValue);
		
//		vtkgrid.Squeeze();

		return vtkgrid;
	}

	public void writeLegacy(vtkUnstructuredGrid vtkgrid, String filename, boolean bASCII){
		vtkUnstructuredGridWriter writer = new vtkUnstructuredGridWriter();
		if (bASCII){
			writer.SetFileTypeToASCII();
		}else{
			writer.SetFileTypeToBinary();
		}
		
		writer.SetInputDataObject(vtkgrid);
		writer.SetFileName(filename);
		writer.Update();
		long length = new File(filename).length();
		System.out.println("saved to legacy file: "+filename+" with "+((bASCII)?"ASCII":"Binary")+" data encoding, length="+length+" bytes");
	}
	
	public void writeXML(vtkUnstructuredGrid vtkgrid, String filename, boolean bASCII){
		vtkXMLUnstructuredGridWriter writer = new vtkXMLUnstructuredGridWriter();
		if (bASCII){
			writer.SetDataModeToAscii();
		}else{
			writer.SetCompressorTypeToNone();
			writer.SetDataModeToBinary();
		}
//		vtkEnSightWriter writer2 = new vtkEnSightWriter();
//		writer2.SetInputDataObject(id0, id1)
		
		writer.SetInputDataObject(vtkgrid);
		writer.SetFileName(filename);
		writer.Update();
		long length = new File(filename).length();
		System.out.println("saved to XML file: "+filename+" with "+((bASCII)?"ASCII":"Binary")+" data encoding, length="+length+" bytes");
	}
	
//	public void writeXDMF(vtkUnstructuredGrid vtkgrid, String filename){
//		vtkXdmfWriter writer = new vtkXdmfWriter();
//		writer.SetInputDataObject(vtkgrid);
//		writer.SetFileName(filename);
//		writer.Update();
//		long length = new File(filename).length();
//		System.out.println("saved to XDMF file: "+filename+", length="+length+" bytes");
//	}
//	
	public vtkUnstructuredGrid read(String filename){
		File file = new File(filename);
		if (!file.exists()){
			throw new RuntimeException("unstructured mesh file "+filename+" not found");
		}
		vtkXMLFileReadTester tester = new vtkXMLFileReadTester();
		tester.SetFileName(filename);
		vtkUnstructuredGrid vtkgrid = null;
		
		if (tester.TestReadFile() == 1){
			vtkXMLUnstructuredGridReader reader = new vtkXMLUnstructuredGridReader();
			reader.SetFileName(filename);
			reader.Update();
			vtkgrid = reader.GetOutput();
			System.out.println("read from XML file "+filename+", of type "+tester.GetFileDataType());
		}else{
			vtkUnstructuredGridReader reader = new vtkUnstructuredGridReader();
			reader.SetFileName(filename);
			reader.Update();
			vtkgrid = reader.GetOutput();
			System.out.println("read from legacy file "+filename);
		}
		//vtkgrid.BuildLinks();
		return vtkgrid;
	}

	public void write(vtkUnstructuredGrid vtkgrid, String filename) {
		writeXML(vtkgrid,filename,false);		// default
		//writeXDMF(vtkgrid, filename);
//		writeXML(vtkgrid,filename,true);
//		writeLegacy(vtkgrid,filename,false);
//		writeLegacy(vtkgrid,filename,true);
	}

	public static VisTetrahedron[] createTetrahedra(VisIrregularPolyhedron clippedPolyhedron, VisMesh visMesh){
			vtkPolyData vtkpolydata = new vtkPolyData();
			vtkPoints vtkpoints = new vtkPoints();
			int polygonType = new vtkPolygon().GetCellType();
			int[] uniquePointIndices = clippedPolyhedron.getPointIndices();
			for (int point : uniquePointIndices){
				VisPoint visPoint = visMesh.getPoints().get(point);
				vtkpoints.InsertNextPoint(visPoint.x, visPoint.y, visPoint.z);
			}
			vtkpolydata.Allocate(100, 100);
			vtkpolydata.SetPoints(vtkpoints);
			
			for (PolyhedronFace face : clippedPolyhedron.getFaces()){
				vtkIdList faceIdList = new vtkIdList();
				for (int visPointIndex : face.getVertices()){
					int vtkpointid = -1;
					for (int i=0;i<uniquePointIndices.length;i++){
						if (uniquePointIndices[i] == visPointIndex){
							vtkpointid = i;
						}
					}
					faceIdList.InsertNextId(vtkpointid);
				}
				vtkpolydata.InsertNextCell(polygonType, faceIdList);
			}
			
			vtkDelaunay3D delaunayFilter = new vtkDelaunay3D();
			delaunayFilter.SetInputData(vtkpolydata);
			delaunayFilter.Update();
			delaunayFilter.SetAlpha(0.1);
			vtkUnstructuredGrid vtkgrid2 = delaunayFilter.GetOutput();
			ArrayList<VisTetrahedron> visTets = new ArrayList<VisTetrahedron>();
			int numTets = vtkgrid2.GetNumberOfCells();
			if (numTets<1){
				System.out.println("found no tets");
			}
	//		System.out.println("numFaces = "+vtkpolydata.GetNumberOfCells()+", numTets = "+numTets);
			for (int cellIndex=0; cellIndex<numTets; cellIndex++){
				vtkCell cell = vtkgrid2.GetCell(cellIndex);
				if (cell instanceof vtkTetra){
					vtkTetra vtkTet = (vtkTetra)cell;
					vtkIdList tetPointIds = vtkTet.GetPointIds();
					//
					// translate from vtkgrid pointids to visMesh point ids
					//
					int numPoints = tetPointIds.GetNumberOfIds();
					int[] visPointIds = new int[numPoints];
					for (int p=0; p<numPoints; p++){
						visPointIds[p] = uniquePointIndices[tetPointIds.GetId(p)];
					}
					VisTetrahedron visTet = new VisTetrahedron(visPointIds, clippedPolyhedron.getLevel(), clippedPolyhedron.getBoxNumber(), clippedPolyhedron.getBoxIndex(), clippedPolyhedron.getFraction(), clippedPolyhedron.getRegionIndex());
					visTets.add(visTet);
				}else{
					System.out.println("ChomboMeshMapping.createTetrahedra(): expecting a tet, found a "+cell.GetClassName());
				}
			}
			return visTets.toArray(new VisTetrahedron[0]);
		}

	public static vtkUnstructuredGrid smoothUnstructuredGridSurface(vtkUnstructuredGrid grid){
		
		vtkUnstructuredGridGeometryFilter ugGeometryFilter = new vtkUnstructuredGridGeometryFilter();
		ugGeometryFilter.PassThroughPointIdsOn();
		ugGeometryFilter.MergingOff();
		ugGeometryFilter.SetInputData(grid);
		ugGeometryFilter.Update();
		vtkUnstructuredGrid surfaceUnstructuredGrid = ugGeometryFilter.GetOutput();
		
		{
		vtkCellData cellData = surfaceUnstructuredGrid.GetCellData();
		int numCellArrays = cellData.GetNumberOfArrays();
		for (int i=0;i<numCellArrays;i++){
			String cellArrayName = cellData.GetArrayName(i);
			System.out.println("CellArray("+i+") \""+cellArrayName+"\"");
		}
		vtkPointData pointData = surfaceUnstructuredGrid.GetPointData();
		int numPointArrays = pointData.GetNumberOfArrays();
		for (int i=0;i<numPointArrays;i++){
			String pointArrayName = pointData.GetArrayName(i);
			System.out.println("PointArray("+i+") \""+pointArrayName+"\"");
		}
		}
		
		vtkGeometryFilter geometryFilter = new vtkGeometryFilter();
		geometryFilter.SetInputData(surfaceUnstructuredGrid);
		geometryFilter.Update();
		vtkPolyData polyData = geometryFilter.GetOutput();
		
		vtkWindowedSincPolyDataFilter filter = new vtkWindowedSincPolyDataFilter();
		filter.SetInputData(polyData);
		filter.SetNumberOfIterations(15);
		filter.BoundarySmoothingOff();
		filter.FeatureEdgeSmoothingOff();
		filter.SetFeatureAngle(120.0);
		filter.SetPassBand(0.001);
		filter.NonManifoldSmoothingOff();
		filter.NormalizeCoordinatesOn();
		filter.Update();
		
		vtkPolyData smoothedPolydata = filter.GetOutput();
		
		vtkPoints smoothedPoints = smoothedPolydata.GetPoints();
		String originalPointsIdsName = ugGeometryFilter.GetOriginalPointIdsName();
		
		vtkPointData smoothedPointData = smoothedPolydata.GetPointData();
		vtkIdTypeArray pointIdsArray = (vtkIdTypeArray)smoothedPointData.GetArray(originalPointsIdsName);
		int pointsIdsArraySize = pointIdsArray.GetSize();
		vtkPoints origPoints = grid.GetPoints();
		for (int i=0;i<pointsIdsArraySize;i++){
			int pointId = pointIdsArray.GetValue(i);
			double[] smoothedPoint = smoothedPoints.GetPoint(i);
			origPoints.SetPoint(pointId, smoothedPoint);
		}
	
		return grid;
	}

}
