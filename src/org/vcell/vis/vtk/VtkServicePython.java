package org.vcell.vis.vtk;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.vcell.util.Executable2;
import org.vcell.util.ExecutableException;
import org.vcell.util.IExecutable;
import org.vcell.util.PropertyLoader;
import org.vcell.vis.vismesh.thrift.VarData;
import org.vcell.vis.vismesh.thrift.VisMesh;

public class VtkServicePython extends VtkService {
	private static final String PYTHON_MODULE_PATH;
	/**
	 * path to python exe or wrapper
	 */
	private static final String PYTHON_EXE_PATH;
	/**
	 * path to python script
	 */
	private static final String VIS_TOOL;
	//These aren't going to change, so just read once
	static {
		String pm = null;
		String pe = "python";
		String pv = "visTool";
		try {
			pm = PropertyLoader.getProperty(PropertyLoader.VTK_PYTHON_MODULE_PATH, null);
			pe = PropertyLoader.getProperty(PropertyLoader.VTK_PYTHON_EXE_PATH, pe);
			pv = PropertyLoader.getProperty(PropertyLoader.VIS_TOOL, pv);
		}
		catch (Exception e){ //make fail safe
			lg.warn("error setting PYTHON_PATH",e);
		}
		finally {
			PYTHON_MODULE_PATH = pm;
			PYTHON_EXE_PATH = pe;
			VIS_TOOL=pv;
		}
	}

	@Override
	public void writeChomboMembraneVtkGridAndIndexData(VisMesh visMesh, String domainName, File vtkFile, File indexFile) throws IOException {
		writeVtkGridAndIndexData("chombomembrane", visMesh, domainName, vtkFile, indexFile);
	}

	@Override
	public void writeChomboVolumeVtkGridAndIndexData(VisMesh visMesh, String domainName, File vtkFile, File indexFile) throws IOException {
		writeVtkGridAndIndexData("chombovolume", visMesh, domainName, vtkFile, indexFile);
	}

	@Override
	public void writeFiniteVolumeSmoothedVtkGridAndIndexData(VisMesh visMesh, String domainName, File vtkFile, File indexFile) throws IOException {
		writeVtkGridAndIndexData("finitevolume", visMesh, domainName, vtkFile, indexFile);
	}

	private void writeVtkGridAndIndexData(String visMeshType, VisMesh visMesh, String domainName, File vtkFile, File indexFile) throws IOException {
		if (lg.isDebugEnabled()) {
			lg.debug("writeVtkGridAndIndexData (python) for domain "+domainName);
		}
		String baseFilename = vtkFile.getName().replace(".vtu",".visMesh");
		File visMeshFile = new File(vtkFile.getParentFile(), baseFilename);
		VisMeshUtils.writeVisMesh(visMeshFile, visMesh);
		//It's 2015 -- forward slash works for all operating systems
		String[] cmd = new String[] { PYTHON_EXE_PATH,VIS_TOOL+"/vtkService.py",visMeshType,domainName,visMeshFile.getAbsolutePath(),vtkFile.getAbsolutePath(),indexFile.getAbsolutePath() };
		IExecutable exe = prepareExecutable(cmd);
		try {
			exe.start();
		} catch (ExecutableException e) {
			e.printStackTrace();
			throw new RuntimeException("vtkService.py invocation failed: "+e.getMessage(),e);
		}
	}


	@Override
	public void writeDataArrayToNewVtkFile(File emptyMeshFile, String variableVtuName, double[] data, File newMeshFile) throws IOException {
//		VtkGridUtils vtkGridUtils = new VtkGridUtils();
//		vtkGridUtils.writeDataArrayToNewVtkFile(emptyMeshFile, variableVtuName, data, newMeshFile);
		if (lg.isDebugEnabled()) {
			lg.debug("writeDataArrayToNewVtkFile (python) for variable "+variableVtuName);
		}
		String baseFilename = newMeshFile.getName().replace(".vtu","");
		File varDataFile = File.createTempFile(baseFilename+"_"+variableVtuName+"_", ".vardata", newMeshFile.getParentFile());
		VarData varData = new VarData(variableVtuName, Arrays.asList(ArrayUtils.toObject(data)));
		VisMeshUtils.writeVarData(varDataFile, varData);
		String[] cmd = new String[] { PYTHON_EXE_PATH,VIS_TOOL+"/vtkAddData.py",varDataFile.getAbsolutePath(),emptyMeshFile.getAbsolutePath(),newMeshFile.getAbsolutePath() };
		IExecutable exe = prepareExecutable(cmd);
		try {
			exe.start();
		} catch (ExecutableException e) {
			e.printStackTrace();
			throw new RuntimeException("vtkAddData.py invocation failed: "+e.getMessage(),e);
		}
	}

	private IExecutable prepareExecutable(String[] cmd) {
		if (lg.isInfoEnabled()) {
			lg.info("python command string:" + StringUtils.join(cmd));
		}
		Executable2 exe = new Executable2(cmd);
		if (PYTHON_MODULE_PATH != null) {
			exe.addEnvironmentVariable("PYTHONPATH", PYTHON_MODULE_PATH);
		}
		return exe;
	}

}
