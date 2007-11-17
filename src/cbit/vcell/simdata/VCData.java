package cbit.vcell.simdata;
/*�
 * (C) Copyright University of Connecticut Health Center 2001.
 * All rights reserved.
�*/
import java.io.IOException;
import java.util.Arrays;

import cbit.vcell.math.AnnotatedFunction;
import cbit.vcell.math.MathException;
import cbit.vcell.parser.SymbolTableEntry;
import cbit.vcell.server.DataAccessException;
import cbit.vcell.server.VCDataIdentifier;
import cbit.vcell.simdata.DataSetControllerImpl.ProgressListener;
import cbit.vcell.solvers.CartesianMesh;
/**
 * This type was created in VisualAge.
 */
public abstract class VCData implements SimDataConstants {

/**
 * SimResults constructor comment.
 */
protected VCData() {
}

public abstract SymbolTableEntry getEntry(String identifier);

/**
 * Insert the method's description here.
 * Creation date: (10/11/00 1:28:51 PM)
 * @param function cbit.vcell.math.Function
 */
public abstract void addFunction(AnnotatedFunction function,boolean bReplace) throws DataAccessException;


/**
 * Insert the method's description here.
 * Creation date: (1/19/00 11:52:22 AM)
 * @return long
 * @param dataType int
 * @param timepoint double
 * @exception cbit.vcell.server.DataAccessException The exception description.
 */
public abstract long getDataBlockTimeStamp(int dataType, double timepoint) throws DataAccessException;


/**
 * This method was created in VisualAge.
 * @return double[]
 */
public abstract double[] getDataTimes() throws DataAccessException;


/**
 * Insert the method's description here.
 * Creation date: (10/11/00 5:16:06 PM)
 * @return cbit.vcell.math.Function
 * @param name java.lang.String
 */
public abstract AnnotatedFunction getFunction(String identifier);


/**
 * Insert the method's description here.
 * Creation date: (10/11/00 5:16:06 PM)
 * @return cbit.vcell.math.Function
 * @param name java.lang.String
 */
public abstract AnnotatedFunction[] getFunctions();


/**
 * This method was created in VisualAge.
 * @return boolean
 */
public abstract boolean getIsODEData() throws DataAccessException;


/**
 * This method was created in VisualAge.
 * @return cbit.vcell.solvers.CartesianMesh
 */
public abstract CartesianMesh getMesh() throws DataAccessException, MathException;


/**
 * Insert the method's description here.
 * Creation date: (1/14/00 2:28:47 PM)
 * @return cbit.vcell.simdata.ODEDataBlock
 */
public abstract ODEDataBlock getODEDataBlock() throws DataAccessException;


/**
 * This method was created in VisualAge.
 * @return cbit.vcell.simdata.ParticleDataBlock
 * @param double time
 */
public abstract ParticleDataBlock getParticleDataBlock(double time) throws DataAccessException, IOException;


/**
 * This method was created in VisualAge.
 * @return boolean
 */
public abstract boolean getParticleDataExists() throws DataAccessException;


/**
 * This method was created in VisualAge.
 * @return cbit.vcell.simdata.SimResultsInfo
 */
public abstract VCDataIdentifier getResultsInfoObject();


/**
 * This method was created in VisualAge.
 * @return cbit.vcell.simdata.DataBlock
 * @param user cbit.vcell.server.User
 * @param simID java.lang.String
 */
public abstract SimDataBlock getSimDataBlock(String varName, double time) throws DataAccessException, IOException;

abstract double[][][] getSimDataTimeSeries0(
		String varNames[],
		int[][] indexes,
		boolean[] wantsThisTime,
		DataSetControllerImpl.SpatialStatsInfo spatialStatsInfo,
		ProgressListener progressListener) 
throws DataAccessException,IOException;

/**
 * This method was created in VisualAge.
 * @return long
 */
public abstract long getSizeInBytes();


/**
 * This method was created in VisualAge.
 * @return java.lang.String[]
 */
public abstract DataIdentifier[] getVarAndFunctionDataIdentifiers() throws IOException, DataAccessException;


/**
 * This method was created in VisualAge.
 * @return cbit.vcell.solvers.CartesianMesh
 */
abstract int[] getVolumeSize() throws IOException, DataAccessException;


/**
 * Insert the method's description here.
 * Creation date: (10/11/00 1:28:51 PM)
 * @param function cbit.vcell.math.Function
 */
public abstract void removeFunction(AnnotatedFunction function) throws DataAccessException;

/**
 * This method was created in VisualAge.
 * @return cbit.vcell.simdata.DataBlock
 * @param user cbit.vcell.server.User
 * @param simID java.lang.String
 */
public final double[][][] getSimDataTimeSeries(String varNames[],int[][] indexes,boolean[] wantsThisTime, ProgressListener progressListener) throws DataAccessException,IOException{

	return getSimDataTimeSeries0(varNames,indexes,wantsThisTime,null,progressListener);
}

/**
 * This method was created in VisualAge.
 * @return cbit.vcell.simdata.DataBlock
 * @param user cbit.vcell.server.User
 * @param simID java.lang.String
 */
public final double[][][] getSimDataTimeSeries(
		String varNames[],
		int[][] indexes,
		boolean[] wantsThisTime,
		DataSetControllerImpl.SpatialStatsInfo spatialStatsInfo,
		ProgressListener progressListener) throws DataAccessException,IOException{

	return getSimDataTimeSeries0(varNames,indexes,wantsThisTime,spatialStatsInfo, progressListener);

}

/**
 * Insert the method's description here.
 * Creation date: (3/20/2006 11:37:48 PM)
 * @return double[]
 * @param rawVals double[]
 */
double[] calcSpaceStats(double[] rawVals,int varIndex,DataSetControllerImpl.SpatialStatsInfo spatialStatsInfo) {
	
    double min = Double.POSITIVE_INFINITY;
    double max = Double.NEGATIVE_INFINITY;
    double mean = 0;
    double wmean = 0;
    double sum = 0;
    double wsum = 0;
    double val;
    for(int j=0;j<rawVals.length;j+= 1){
	    val = rawVals[j];
	    if(val < min){min=val;}
	    if(val > max){max=val;}
	    sum+= val;
	    if(spatialStatsInfo.bWeightsValid){wsum+= val*spatialStatsInfo.spaceWeight[varIndex][j];}
    }
    mean = sum/rawVals.length;
    if(spatialStatsInfo.bWeightsValid){wmean = wsum/spatialStatsInfo.totalSpace[varIndex];}

    return new double[] {min,max,mean,wmean,sum,wsum};
}


/**
 * This method was created in VisualAge.
 * @return cbit.vcell.simdata.DataBlock
 * @param user cbit.vcell.server.User
 * @param simID java.lang.String
 */
public synchronized double[][] getSimDataLineScan(String[] varNames,int[][] indexes,double desiredTime,ProgressListener progressListener) throws DataAccessException,IOException{

	// Setup parameters for SimDataReader
	double[] dataTimes = getDataTimes();
	boolean[] wantsThisTime = new boolean[dataTimes.length];
	Arrays.fill(wantsThisTime,false);
	for(int i=0;i<dataTimes.length;i+= 1){
		if(dataTimes[i] == desiredTime){
			wantsThisTime[i] = true;
			break;
		}
	}

	double[][][] timeResults = getSimDataTimeSeries(varNames,indexes,wantsThisTime,progressListener);
	double[][] results = new double[varNames.length][];
	for(int i=0;i<varNames.length;i+= 1){
		results[i] = new double[indexes[i].length];
		for( int j=0;j<indexes[i].length;j+= 1){
			results[i][j] = timeResults[0][i][j];
		}
	}
	return results;
}

}