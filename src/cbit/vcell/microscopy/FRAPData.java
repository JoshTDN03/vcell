package cbit.vcell.microscopy;

import java.awt.Component;
import java.awt.Rectangle;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.BitSet;

import javax.media.jai.BorderExtender;
import javax.media.jai.KernelJAI;
import javax.media.jai.LookupTableJAI;
import javax.media.jai.PlanarImage;
import javax.media.jai.RenderedOp;
import javax.media.jai.operator.AddConstDescriptor;
import javax.media.jai.operator.AndDescriptor;
import javax.media.jai.operator.BorderDescriptor;
import javax.media.jai.operator.CropDescriptor;
import javax.media.jai.operator.DilateDescriptor;
import javax.media.jai.operator.ErodeDescriptor;
import javax.media.jai.operator.ExtremaDescriptor;
import javax.media.jai.operator.LookupDescriptor;

import loci.formats.AWTImageTools;

import org.vcell.util.Compare;
import org.vcell.util.Extent;
import org.vcell.util.NullSessionLog;
import org.vcell.util.Origin;

import cbit.image.ImageException;
import cbit.rmi.event.DataJobEvent;
import cbit.rmi.event.DataJobListener;
import org.vcell.util.document.KeyValue;
import org.vcell.util.document.TSJobResultsSpaceStats;
import org.vcell.util.document.TimeSeriesJobSpec;
import org.vcell.util.Matchable;
import org.vcell.util.document.VCDataJobID;
import cbit.vcell.VirtualMicroscopy.ImageDataset;
import cbit.vcell.VirtualMicroscopy.ROI;
import cbit.vcell.VirtualMicroscopy.UShortImage;
import cbit.vcell.client.UserMessage;
import cbit.vcell.client.task.ClientTaskStatusSupport;

import org.vcell.util.document.User;
import org.vcell.util.gui.DialogUtils;

import cbit.vcell.simdata.Cachetable;
import cbit.vcell.simdata.DataIdentifier;
import cbit.vcell.simdata.DataSetControllerImpl;
import cbit.vcell.simdata.SimDataConstants;
import cbit.vcell.simdata.VariableType;
import cbit.vcell.solver.VCSimulationDataIdentifier;
import cbit.vcell.solver.VCSimulationIdentifier;
import cbit.vcell.solvers.CartesianMesh;

/**
 * Insert the type's description here.
 * Creation date: (1/24/2007 4:18:01 PM)
 * @author schaff
 * @version $Revision: 1.0 $
 */

public class FRAPData extends AnnotatedImageDataset implements Matchable, VFrap_ROISourceData{

	public static class OriginalGlobalScaleInfo{
		public final int originalGlobalScaledMin;
		public final int originalGlobalScaledMax;
		public final double originalScaleFactor;
		public final double originalOffsetFactor;
		public OriginalGlobalScaleInfo(
			int originalGlobalScaledMin,int originalGlobalScaledMax,
			double originalScaleFactor,double originalOffsetFactor){
			this.originalGlobalScaledMin = originalGlobalScaledMin;
			this.originalGlobalScaledMax = originalGlobalScaledMax;
			this.originalScaleFactor = originalScaleFactor;
			this.originalOffsetFactor = originalOffsetFactor;
		}
	};
	private OriginalGlobalScaleInfo originalGlobalScaleInfo;
	
	public static enum VFRAP_ROI_ENUM {
	ROI_BLEACHED,
	ROI_BACKGROUND,
	ROI_CELL,
	ROI_BLEACHED_RING1,
	ROI_BLEACHED_RING2,
	ROI_BLEACHED_RING3,
	ROI_BLEACHED_RING4,
	ROI_BLEACHED_RING5,
	ROI_BLEACHED_RING6,
	ROI_BLEACHED_RING7,
	ROI_BLEACHED_RING8
//	ROI_NUCLEUS,
//	ROI_EXTRACELLULAR
}

/**
 * FRAPData constructor comment.
 * @param argImageDataset ImageDataset
 * @param argROIs ROI[]
 */
	public FRAPData(ImageDataset argImageDataset, ROI[] argROIs) {
		super(argImageDataset, argROIs);
	}

	public static FRAPData importFRAPDataFromImageDataSet(ImageDataset imageDataSet){
		FRAPData frapData =
			new FRAPData(imageDataSet,
				new String[]{ FRAPData.VFRAP_ROI_ENUM.ROI_BLEACHED.name(),FRAPData.VFRAP_ROI_ENUM.ROI_CELL.name(),FRAPData.VFRAP_ROI_ENUM.ROI_BACKGROUND.name()}
			);
		frapData.setOriginalGlobalScaleInfo(null);
		return frapData;
	}
	public static DataIdentifier[] getDataIdentiferListFromVCellSimulationData(File vcellSimLogFile,int jobIndex) throws Exception{
		return
			getDataSetControllerImplFromVCellSimulationData(vcellSimLogFile).getDataIdentifiers(
				new VCSimulationDataIdentifier(
					getVCSimulationIdentifierFromVCellSimulationData(vcellSimLogFile),jobIndex)
			);
	}
	public static DataSetControllerImpl getDataSetControllerImplFromVCellSimulationData(File vcellSimLogFile) throws Exception{
		File primaryDir = vcellSimLogFile.getParentFile();
		return new DataSetControllerImpl(
			new NullSessionLog(),new Cachetable(Cachetable.minute*10),primaryDir,primaryDir);
	}
	public static User getDotUser(){
		String bogusUserName = ".";
		return new User(bogusUserName,new KeyValue("0"));
	}
	public static VCSimulationIdentifier getVCSimulationIdentifierFromVCellSimulationData(File vcellSimLogFile){
		final String SIMID_PREFIX = "SimID_";
		String simulationKeyS =
			vcellSimLogFile.getName().substring(
					SIMID_PREFIX.length(),
					vcellSimLogFile.getName().indexOf('_', SIMID_PREFIX.length()));
		KeyValue simulationKey = new KeyValue(simulationKeyS);
		return new VCSimulationIdentifier(simulationKey,getDotUser());

	}
	public static FRAPData importFRAPDataFromVCellSimulationData(File vcellSimLogFile,String variableName,
			final ClientTaskStatusSupport progressListener) throws Exception{
		if(!vcellSimLogFile.getName().endsWith(SimDataConstants.LOGFILE_EXTENSION)){
			throw new IllegalArgumentException("VCell simulation logfile with extension "+SimDataConstants.LOGFILE_EXTENSION+" expected.");
		}
		VCSimulationIdentifier vcSimulationIdentifier =
			getVCSimulationIdentifierFromVCellSimulationData(vcellSimLogFile);
		VCSimulationDataIdentifier vcSimulationDataIdentifier = 
			new VCSimulationDataIdentifier(vcSimulationIdentifier,0);
		
		DataSetControllerImpl dataSetControllerImpl =
			getDataSetControllerImplFromVCellSimulationData(vcellSimLogFile);

		final DataJobEvent[] bStatus = new DataJobEvent[] {null};
		DataJobListener dataJobListener =
			new DataJobListener(){
				public void dataJobMessage(DataJobEvent event) {
					bStatus[0] = event;
					if(progressListener != null){
						progressListener.setProgress((int)(event.getProgress()/100.0*.75));
					}
				}
			};
		dataSetControllerImpl.addDataJobListener(dataJobListener);
		
		DataIdentifier[] dataIdentifiers =
			getDataIdentiferListFromVCellSimulationData(vcellSimLogFile, 0);
		DataIdentifier variableNameDataIdentifier = null;
		for (int i = 0; i < dataIdentifiers.length; i++) {
			if(dataIdentifiers[i].getName().equals(variableName)){
				variableNameDataIdentifier = dataIdentifiers[i];
				break;
			}
		}
		if(variableNameDataIdentifier == null){
			throw new IllegalArgumentException("Variable "+variableName+" not found.");
		}
		if(!variableNameDataIdentifier.getVariableType().equals(VariableType.VOLUME)){
			throw new IllegalArgumentException("Variable "+variableName+" is not VOLUME type.");
		}
		double[] times = dataSetControllerImpl.getDataSetTimes(vcSimulationDataIdentifier);
		CartesianMesh cartesianMesh = dataSetControllerImpl.getMesh(vcSimulationDataIdentifier);
		BitSet allBitset = new BitSet(cartesianMesh.getNumVolumeElements());
		allBitset.set(0, cartesianMesh.getNumVolumeElements()-1);
		TimeSeriesJobSpec timeSeriesJobSpec = 
			new TimeSeriesJobSpec(
					new String[]{variableName},
					new BitSet[] {allBitset},
					times[0],1,times[times.length-1],
					true,false,VCDataJobID.createVCDataJobID(getDotUser(), true)
					);
		TSJobResultsSpaceStats tsJobResultsSpaceStats =
			(TSJobResultsSpaceStats)dataSetControllerImpl.getTimeSeriesValues(
				vcSimulationDataIdentifier, timeSeriesJobSpec);
		//wait for job to finish
		while(bStatus[0] == null || bStatus[0].getEventTypeID() != DataJobEvent.DATA_COMPLETE){
			Thread.sleep(100);
			if(bStatus[0].getEventTypeID() == DataJobEvent.DATA_FAILURE){
				throw bStatus[0].getFailedJobException();
			}
		}
		double allTimesMin = tsJobResultsSpaceStats.getMinimums()[0][0];
		double allTimesMax = allTimesMin;
		for (int i = 0; i < times.length; i++) {
			allTimesMin = Math.min(allTimesMin, tsJobResultsSpaceStats.getMinimums()[0][i]);
			allTimesMax = Math.max(allTimesMax, tsJobResultsSpaceStats.getMaximums()[0][i]);
		}
		double SCALE_MAX = Math.pow(2,16)-1;//Scale to 16 bits
		double linearaScaleFactor = SCALE_MAX/allTimesMax;
		System.out.println("alltimesMin="+allTimesMin+" allTimesMax="+allTimesMax);
		UShortImage[] scaledDataImages = new UShortImage[times.length];
		for (int i = 0; i < times.length; i++) {
			double[] rawData =
				dataSetControllerImpl.getSimDataBlock(
						vcSimulationDataIdentifier,
						variableName,
						times[i]).getData();
			short[] scaledDataShort = new short[rawData.length];
//			if(allTimesMax> SCALE_MAX){
			for (int j = 0; j < scaledDataShort.length; j++) {
				int scaledValue = (int)(rawData[j]*linearaScaleFactor);
				scaledDataShort[j]&= 0x0000;
				scaledDataShort[j]|= 0x0000FFFF & scaledValue;
					
			}
//			}
			scaledDataImages[i] =
				new UShortImage(
					scaledDataShort,
					cartesianMesh.getOrigin(),
					cartesianMesh.getExtent(),
					cartesianMesh.getSizeX(),cartesianMesh.getSizeY(),cartesianMesh.getSizeZ());
			if(progressListener != null){progressListener.setProgress((int)(.75+(.25*(double)(i+1)/times.length)));}
		}
		ImageDataset imageDataSet = new ImageDataset(scaledDataImages,times,cartesianMesh.getSizeZ());
		FRAPData frapData = new FRAPData(imageDataSet,
				new String[]{ FRAPData.VFRAP_ROI_ENUM.ROI_BLEACHED.name(),FRAPData.VFRAP_ROI_ENUM.ROI_CELL.name(),FRAPData.VFRAP_ROI_ENUM.ROI_BACKGROUND.name()}
		);
		frapData.setOriginalGlobalScaleInfo(
			new FRAPData.OriginalGlobalScaleInfo(
				(int)(allTimesMin*linearaScaleFactor),
				(int)(allTimesMax*linearaScaleFactor),
				linearaScaleFactor,0));
		
		return frapData;
	}
	/**
	 * Constructor for FRAPData.
	 * @param argImageDataset ImageDataset
	 * @param argROITypes ROI.RoiType[]
	 */
	public FRAPData(ImageDataset argImageDataset, String[] argROINames) {
		super(argImageDataset, argROINames);
	}

/**
 * Method crop.
 * @param rect Rectangle
 * @return FRAPData
 * @throws ImageException
 */
@Override
public FRAPData crop(Rectangle rect) throws ImageException {
	ImageDataset croppedImageDataset = getImageDataset().crop(rect);
	ROI[] rois = getRois();
	ROI[] croppedROIs = new ROI[rois.length];
	ROI currentlyDisplayedROI = getCurrentlyDisplayedROI();
	ROI croppedCurrentROI = null;
	for (int i = 0; i < croppedROIs.length; i++) {
		croppedROIs[i] = rois[i].crop(rect);
		if (currentlyDisplayedROI == rois[i]){
			croppedCurrentROI = croppedROIs[i];
		}
	}
	FRAPData croppedFrapData = new FRAPData(croppedImageDataset,croppedROIs);
	setCurrentlyDisplayedROI(croppedCurrentROI);
	return croppedFrapData;
}

public OriginalGlobalScaleInfo getOriginalGlobalScaleInfo() {
	return originalGlobalScaleInfo;
}

public void setOriginalGlobalScaleInfo(OriginalGlobalScaleInfo originalGlobalScaleInfo) {
	this.originalGlobalScaleInfo = originalGlobalScaleInfo;
}

public double[] getAvgBackGroundIntensity()
{
	return FRAPDataAnalysis.getAverageROIIntensity(this,this.getRoi(FRAPData.VFRAP_ROI_ENUM.ROI_BACKGROUND.name()),null,null);
}

public boolean compareEqual(Matchable obj) 
{
	if (this == obj) {
		return true;
	}
	if (obj != null && obj instanceof FRAPData) 
	{
		FRAPData fData = (FRAPData) obj;
		if (!Compare.isEqualOrNull(getImageDataset(), fData.getImageDataset())){
			return false;
		}
		if (!Compare.isEqualOrNull(getRois(), fData.getRois())){
			return false;
		}
		return true;
	}
	return false;
}

public void chopImages(int startTimeIndex, int endTimeIndex) 
{
	UShortImage[] origImages = getImageDataset().getAllImages();
	double[] origTimeSteps = getImageDataset().getImageTimeStamps();
	int numOfZ = getImageDataset().getSizeZ();
	int numOfC = getImageDataset().getSizeC();
	int tolImgLen = (endTimeIndex - startTimeIndex + 1)*numOfZ*numOfC;
	UShortImage[] newImages = new UShortImage[tolImgLen];
	double[] newTimeSteps = new double[endTimeIndex - startTimeIndex + 1];
	
	System.arraycopy(origImages, startTimeIndex*numOfZ*numOfC, newImages, 0, tolImgLen);
	System.arraycopy(origTimeSteps, startTimeIndex, newTimeSteps, 0, (endTimeIndex - startTimeIndex + 1));
	//shift time to start from 0, it's not necessary 
//	double firstTimePoint = newTimeSteps[0];
//	for(int i = 0; i < newTimeSteps.length; i++)
//	{
//		newTimeSteps[i] = newTimeSteps[i] - firstTimePoint;
//	}
	
	
	ImageDataset imgDataset = new ImageDataset(newImages, newTimeSteps, numOfZ);
	setImageDataset(imgDataset);
}

public void changeImageExtent(double imageSizeX, double imageSizeY) {
	Extent extent = getImageDataset().getExtent();
	double imageSizeZ = extent.getZ();
	getImageDataset().setExtent(new Extent(imageSizeX, imageSizeY, imageSizeZ));
}

private KernelJAI createCircularBinaryKernel(int radius){
	int enclosingBoxSideLength = radius*2+1;
	float[] kernalData = new float[enclosingBoxSideLength*enclosingBoxSideLength];
	Point2D kernalPoint = new Point2D.Float(0f,0f);
	int index = 0;
	for (int y = -radius; y <= radius; y++) {
		for (int x = -radius; x <= radius; x++) {
			if(kernalPoint.distance(x, y) <= radius){
				kernalData[index] = 1.0f;
			}
			index++;
		}
	}
	return new KernelJAI(enclosingBoxSideLength,enclosingBoxSideLength,radius,radius,kernalData);
}
private PlanarImage binarize(UShortImage source)
{
	return binarize(AWTImageTools.makeImage(source.getPixels(), source.getNumX(), source.getNumY(),false));
}

private PlanarImage binarize(BufferedImage source)
{
	PlanarImage planarSource = PlanarImage.wrapRenderedImage(source);
	double[][] minmaxArr = (double[][])ExtremaDescriptor.create(planarSource, null, 1, 1, false, 1,null).getProperty("extrema");
	short[] lookupData = new short[(int)0x010000];
	lookupData[(int)minmaxArr[1][0]] = 1;
	LookupTableJAI lookupTable = new LookupTableJAI(lookupData,true);
	planarSource = LookupDescriptor.create(planarSource, lookupTable, null).createInstance();
	return planarSource;		
}

private UShortImage convertToUShortImage(PlanarImage source,Origin origin,Extent extent) throws ImageException
{
	short[] shortData = new short[source.getWidth() * source.getHeight()];
	source.getData().getDataElements(0, 0, source.getWidth(),source.getHeight(), shortData);
	return new UShortImage(shortData,origin,extent,source.getWidth(),source.getHeight(),1);	
}

private UShortImage erodeDilate(UShortImage source,KernelJAI dilateErodeKernel,UShortImage mask,boolean bErode) throws ImageException{
	PlanarImage completedImage = null;
	PlanarImage operatedImage = null;
	PlanarImage planarSource = binarize(source);
	Integer borderPad = dilateErodeKernel.getWidth()/2;
	planarSource = 
		BorderDescriptor.create(planarSource,
			borderPad, borderPad, borderPad, borderPad,
			BorderExtender.createInstance(BorderExtender.BORDER_ZERO), null).createInstance();
	if(bErode){
		planarSource = AddConstDescriptor.create(planarSource, new double[] {1.0}, null).createInstance();
    	RenderedOp erodeOP = ErodeDescriptor.create(planarSource, dilateErodeKernel, null);
    	operatedImage = erodeOP.createInstance();
		
	}else{
    	RenderedOp dilationOP = DilateDescriptor.create(planarSource, dilateErodeKernel, null);
    	operatedImage = dilationOP.createInstance();
	}
	operatedImage =
		CropDescriptor.create(operatedImage,
			new Float(0), new Float(0),
			new Float(source.getNumX()), new Float(source.getNumY()), null);
	operatedImage = binarize(operatedImage.getAsBufferedImage());
	if (mask != null) {
		RenderedOp andDescriptor = AndDescriptor.create(operatedImage,binarize(mask), null);
		completedImage = andDescriptor.createInstance();
	}else{
		completedImage = operatedImage;
	}
	return convertToUShortImage(completedImage, source.getOrigin(),source.getExtent());
}

private UShortImage fastDilate(UShortImage dilateSource,int radius,UShortImage mask) throws ImageException{
	short[] sourcePixels = dilateSource.getPixels();
	short[] targetPixels = dilateSource.getPixels().clone();
	KernelJAI dilateKernel = createCircularBinaryKernel(radius);
	float[] kernelData = dilateKernel.getKernelData();
	BitSet kernelBitSet = new BitSet(kernelData.length);
	for (int i = 0; i < kernelData.length; i++) {
		if(kernelData[i] == 1.0f){
			kernelBitSet.set(i);
		}
	}
	boolean bNeedsFill = false;
	for (int y = 0; y < dilateSource.getNumY(); y++) {
		int yOffset = y*dilateSource.getNumX();
		int yMinus = yOffset-dilateSource.getNumX();
		int yPlus = yOffset+dilateSource.getNumX();
		for (int x = 0; x < dilateSource.getNumX(); x++) {
			bNeedsFill = false;
			if(sourcePixels[x+yOffset] != 0){
				if(x-1 >= 0 && sourcePixels[(x-1)+yOffset] == 0){
					bNeedsFill = true;
				}else
				if(y-1 >= 0 && sourcePixels[x+yMinus] == 0){
					bNeedsFill = true;
				}else
				if(x+1 < dilateSource.getNumX() && sourcePixels[(x+1)+yOffset] == 0){
					bNeedsFill = true;
				}else
				if(y+1 < dilateSource.getNumY() && sourcePixels[x+yPlus] == 0){
					bNeedsFill = true;
				}
				if(bNeedsFill){
					int masterKernelIndex = 0;
					for (int y2 = y-radius; y2 <= y+radius; y2++) {
						if(y2>= 0 && y2 <dilateSource.getNumY()){
							int kernelIndex = masterKernelIndex;
							int targetYIndex = y2*dilateSource.getNumX();
							for (int x2 = x-radius; x2 <= x+radius; x2++) {
								if(kernelBitSet.get(kernelIndex) &&
									x2>= 0 && x2 <dilateSource.getNumX()){
									targetPixels[targetYIndex+x2] = 1;
								}
								kernelIndex++;
							}
						}
						masterKernelIndex+= dilateKernel.getWidth();
					}
				}
			}
		}
	}
	UShortImage resultImage =
		new UShortImage(targetPixels,
				dilateSource.getOrigin(),
				dilateSource.getExtent(),
				dilateSource.getNumX(),
				dilateSource.getNumY(),
				dilateSource.getNumZ());
	resultImage.and(mask);
	return resultImage;
}

public boolean checkROIConstraints(Component componentToDisplayMsg) throws Exception{
	short[] cellPixels = getRoi(FRAPData.VFRAP_ROI_ENUM.ROI_CELL.name()).getPixelsXYZ();
	short[] bleachPixels = getRoi(FRAPData.VFRAP_ROI_ENUM.ROI_BLEACHED.name()).getPixelsXYZ();
	short[] backgroundPixels = getRoi(FRAPData.VFRAP_ROI_ENUM.ROI_BACKGROUND.name()).getPixelsXYZ();
	boolean bFixedBleach = false;
	boolean bFixedBackground = false;
	for (int i = 0; i < cellPixels.length; i++) {
		if(cellPixels[i] == 0 && bleachPixels[i] != 0){
			bFixedBleach = true;
			bleachPixels[i] = 0;
		}
		if(cellPixels[i] != 0 && backgroundPixels[i] != 0){
			bFixedBackground = true;
			backgroundPixels[i] = 0;
		}
	}
	if(bFixedBackground || bFixedBleach){
		final String FIX_AUTO = "Fix Automatically";
		final String NO_THANKS = "No, Thanks";
		String result = DialogUtils.showWarningDialog(componentToDisplayMsg,
				(bFixedBleach?"Bleach ROI extends beyond Cell ROI":"")+
				(bFixedBackground &&bFixedBleach?" and" :"")+
				(bFixedBackground?"Background ROI overlaps Cell ROI":"")+
				".  Ensure that the Bleach ROI is completely inside the Cell ROI and the Background ROI is completely outside the Cell ROI.\nDo you want Virtual Frap to fix it automatically?",
				new String[] {FIX_AUTO,NO_THANKS}, FIX_AUTO);
		if(result != null && result.equals(FIX_AUTO)){
			if(bFixedBleach){
				UShortImage ushortImage =
					new UShortImage(bleachPixels,
						getCurrentlyDisplayedROI().getRoiImages()[0].getOrigin(),
						getCurrentlyDisplayedROI().getRoiImages()[0].getExtent(),
						getCurrentlyDisplayedROI().getISize().getX(),
						getCurrentlyDisplayedROI().getISize().getY(),
						getCurrentlyDisplayedROI().getISize().getZ());
				ROI newBleachROI = new ROI(ushortImage,FRAPData.VFRAP_ROI_ENUM.ROI_BLEACHED.name());
				addReplaceRoi(newBleachROI);
			}
			if(bFixedBackground){
				UShortImage ushortImage =
					new UShortImage(backgroundPixels,
						getCurrentlyDisplayedROI().getRoiImages()[0].getOrigin(),
						getCurrentlyDisplayedROI().getRoiImages()[0].getExtent(),
						getCurrentlyDisplayedROI().getISize().getX(),
						getCurrentlyDisplayedROI().getISize().getY(),
						getCurrentlyDisplayedROI().getISize().getZ());
				ROI newBackgroundROI = new ROI(ushortImage,FRAPData.VFRAP_ROI_ENUM.ROI_BACKGROUND.name());
				addReplaceRoi(newBackgroundROI);
			}
		}
		return true;
	}
	return false;
}

protected void refreshDependentROIs()
{
	UShortImage cellROI_2D = null;
	UShortImage bleachedROI_2D = null;
	UShortImage dilatedROI_2D_1 = null;
	UShortImage dilatedROI_2D_2 = null;
	UShortImage dilatedROI_2D_3 = null;
	UShortImage dilatedROI_2D_4 = null;
	UShortImage dilatedROI_2D_5 = null;
	UShortImage erodedROI_2D_0 = null;
	UShortImage erodedROI_2D_1 = null;
	UShortImage erodedROI_2D_2 = null;

	try {
		cellROI_2D =
			convertToUShortImage(binarize(getRoi(FRAPData.VFRAP_ROI_ENUM.ROI_CELL.name()).getRoiImages()[0]),
				getRoi(FRAPData.VFRAP_ROI_ENUM.ROI_CELL.name()).getRoiImages()[0].getOrigin(),
				getRoi(FRAPData.VFRAP_ROI_ENUM.ROI_CELL.name()).getRoiImages()[0].getExtent());
		bleachedROI_2D =
			convertToUShortImage(
					AndDescriptor.create(binarize(getRoi(FRAPData.VFRAP_ROI_ENUM.ROI_BLEACHED.name()).getRoiImages()[0]),
						binarize(cellROI_2D), null).createInstance(),
					getRoi(FRAPData.VFRAP_ROI_ENUM.ROI_BLEACHED.name()).getRoiImages()[0].getOrigin(),
					getRoi(FRAPData.VFRAP_ROI_ENUM.ROI_BLEACHED.name()).getRoiImages()[0].getExtent());
//		writeUShortFile(cellROI_2D, new File("D:\\developer\\eclipse\\workspace\\cellROI_2D.bmp"));
//		writeUShortFile(bleachedROI_2D, new File("D:\\developer\\eclipse\\workspace\\bleachedROI_2D.bmp"));

		dilatedROI_2D_1 =
			fastDilate(bleachedROI_2D, 4, cellROI_2D);
//			erodeDilate(bleachedROI_2D, createCircularBinaryKernel(8), cellROI_2D,false);

		dilatedROI_2D_2 = 
			fastDilate(bleachedROI_2D, 10, cellROI_2D);
//			erodeDilate(bleachedROI_2D, createCircularBinaryKernel(16), cellROI_2D,false);

    	dilatedROI_2D_3 = 
    		fastDilate(bleachedROI_2D, 18, cellROI_2D);
//			erodeDilate(bleachedROI_2D, createCircularBinaryKernel(24), cellROI_2D,false);

    	dilatedROI_2D_4 = 
    		fastDilate(bleachedROI_2D, 28, cellROI_2D);
//			erodeDilate(bleachedROI_2D, createCircularBinaryKernel(32), cellROI_2D,false);

    	dilatedROI_2D_5 = 
    		fastDilate(bleachedROI_2D, 40, cellROI_2D);
//			erodeDilate(bleachedROI_2D, createCircularBinaryKernel(40), cellROI_2D,false);
		
		erodedROI_2D_0 = new UShortImage(bleachedROI_2D);
		
		// The erode always causes problems if eroding without checking the bleached length and hight.
		// we have to check the min length of the bleahed area to make sure erode within the length.
		Rectangle bleachRect = bleachedROI_2D.getNonzeroBoundingBox();
		int minLen = Math.min(bleachRect.height, bleachRect.width);
		if((minLen/2.0) < 5)
		{
			erodedROI_2D_1 = erodeDilate(bleachedROI_2D, createCircularBinaryKernel(1), bleachedROI_2D,true);
			erodedROI_2D_2 = erodeDilate(bleachedROI_2D, createCircularBinaryKernel(2), bleachedROI_2D,true);
		}
		else
		{
			erodedROI_2D_1 = erodeDilate(bleachedROI_2D, createCircularBinaryKernel(2), bleachedROI_2D,true);
			erodedROI_2D_2 = erodeDilate(bleachedROI_2D, createCircularBinaryKernel(5), bleachedROI_2D,true);
		}			
		
		UShortImage reverseErodeROI_2D_1 = new UShortImage(erodedROI_2D_1);
		reverseErodeROI_2D_1.reverse();
		erodedROI_2D_0.and(reverseErodeROI_2D_1);
		
		UShortImage reverseErodeROI_2D_2 = new UShortImage(erodedROI_2D_2);
		reverseErodeROI_2D_2.reverse();
		erodedROI_2D_1.and(reverseErodeROI_2D_2);
		
		UShortImage reverseDilateROI_2D_4 = new UShortImage(dilatedROI_2D_4);
		reverseDilateROI_2D_4.reverse();
		dilatedROI_2D_5.and(reverseDilateROI_2D_4);

		UShortImage reverseDilateROI_2D_3 = new UShortImage(dilatedROI_2D_3);
//		writeUShortFile(dilatedROI_2D_3, new File("D:\\developer\\eclipse\\workspace\\dilatedROI_2D_3.bmp"));
		reverseDilateROI_2D_3.reverse();
//		writeUShortFile(reverseDilateROI_2D_3, new File("D:\\developer\\eclipse\\workspace\\dilatedROI_2D_3_reverse.bmp"));
//		writeUShortFile(dilatedROI_2D_4, new File("D:\\developer\\eclipse\\workspace\\dilatedROI_2D_4.bmp"));
		dilatedROI_2D_4.and(reverseDilateROI_2D_3);
//		writeUShortFile(dilatedROI_2D_4, new File("D:\\developer\\eclipse\\workspace\\dilatedROI_2D_4_anded.bmp"));

		UShortImage reverseDilateROI_2D_2 = new UShortImage(dilatedROI_2D_2);
		reverseDilateROI_2D_2.reverse();
		dilatedROI_2D_3.and(reverseDilateROI_2D_2);

		UShortImage reverseDilateROI_2D_1 = new UShortImage(dilatedROI_2D_1);
		reverseDilateROI_2D_1.reverse();
		dilatedROI_2D_2.and(reverseDilateROI_2D_1);

		UShortImage reverseBleach_2D = new UShortImage(bleachedROI_2D);
		reverseBleach_2D.reverse();
		dilatedROI_2D_1.and(reverseBleach_2D);

	}catch (ImageException e){
		e.printStackTrace(System.out);
	}
	ROI roiBleachedRing1_2D = getRoi(FRAPData.VFRAP_ROI_ENUM.ROI_BLEACHED_RING1.name());
	if (roiBleachedRing1_2D==null){
		roiBleachedRing1_2D = new ROI(erodedROI_2D_2,FRAPData.VFRAP_ROI_ENUM.ROI_BLEACHED_RING1.name());
		addReplaceRoi(roiBleachedRing1_2D);
	}else{
		System.arraycopy(erodedROI_2D_2.getPixels(), 0, roiBleachedRing1_2D.getRoiImages()[0].getPixels(), 0, erodedROI_2D_2.getPixels().length);
	}
	ROI roiBleachedRing2_2D = getRoi(FRAPData.VFRAP_ROI_ENUM.ROI_BLEACHED_RING2.name());
	if (roiBleachedRing2_2D==null){
		roiBleachedRing2_2D = new ROI(erodedROI_2D_1,FRAPData.VFRAP_ROI_ENUM.ROI_BLEACHED_RING2.name());
		addReplaceRoi(roiBleachedRing2_2D);
	}else{
		System.arraycopy(erodedROI_2D_1.getPixels(), 0, roiBleachedRing2_2D.getRoiImages()[0].getPixels(), 0, erodedROI_2D_1.getPixels().length);
	}
	ROI roiBleachedRing3_2D = getRoi(FRAPData.VFRAP_ROI_ENUM.ROI_BLEACHED_RING3.name());
	if (roiBleachedRing3_2D==null){
		roiBleachedRing3_2D = new ROI(erodedROI_2D_0,FRAPData.VFRAP_ROI_ENUM.ROI_BLEACHED_RING3.name());
		addReplaceRoi(roiBleachedRing3_2D);
	}else{
		System.arraycopy(erodedROI_2D_0.getPixels(), 0, roiBleachedRing3_2D.getRoiImages()[0].getPixels(), 0, erodedROI_2D_0.getPixels().length);
	}
	ROI roiBleachedRing4_2D = getRoi(FRAPData.VFRAP_ROI_ENUM.ROI_BLEACHED_RING4.name());
	if (roiBleachedRing4_2D==null){
		roiBleachedRing4_2D = new ROI(dilatedROI_2D_1,FRAPData.VFRAP_ROI_ENUM.ROI_BLEACHED_RING4.name());
		addReplaceRoi(roiBleachedRing4_2D);
	}else{
		System.arraycopy(dilatedROI_2D_1.getPixels(), 0, roiBleachedRing4_2D.getRoiImages()[0].getPixels(), 0, dilatedROI_2D_1.getPixels().length);

	}
	ROI roiBleachedRing5_2D = getRoi(FRAPData.VFRAP_ROI_ENUM.ROI_BLEACHED_RING5.name());
	if (roiBleachedRing5_2D==null){
		roiBleachedRing5_2D = new ROI(dilatedROI_2D_2,FRAPData.VFRAP_ROI_ENUM.ROI_BLEACHED_RING5.name());
		addReplaceRoi(roiBleachedRing5_2D);
	}else{
		System.arraycopy(dilatedROI_2D_2.getPixels(), 0, roiBleachedRing5_2D.getRoiImages()[0].getPixels(), 0, dilatedROI_2D_2.getPixels().length);
	}
	ROI roiBleachedRing6_2D = getRoi(FRAPData.VFRAP_ROI_ENUM.ROI_BLEACHED_RING6.name());
	if (roiBleachedRing6_2D==null){
		roiBleachedRing6_2D = new ROI(dilatedROI_2D_3,FRAPData.VFRAP_ROI_ENUM.ROI_BLEACHED_RING6.name());
		addReplaceRoi(roiBleachedRing6_2D);
	}else{
		System.arraycopy(dilatedROI_2D_3.getPixels(), 0, roiBleachedRing6_2D.getRoiImages()[0].getPixels(), 0, dilatedROI_2D_3.getPixels().length);
	}
	ROI roiBleachedRing7_2D = getRoi(FRAPData.VFRAP_ROI_ENUM.ROI_BLEACHED_RING7.name());
	if (roiBleachedRing7_2D==null){
		roiBleachedRing7_2D = new ROI(dilatedROI_2D_4,FRAPData.VFRAP_ROI_ENUM.ROI_BLEACHED_RING7.name());
		addReplaceRoi(roiBleachedRing7_2D);
	}else{
		System.arraycopy(dilatedROI_2D_4.getPixels(), 0, roiBleachedRing7_2D.getRoiImages()[0].getPixels(), 0, dilatedROI_2D_4.getPixels().length);
	}
	ROI roiBleachedRing8_2D = getRoi(FRAPData.VFRAP_ROI_ENUM.ROI_BLEACHED_RING8.name());
	if (roiBleachedRing8_2D==null){
		roiBleachedRing8_2D = new ROI(dilatedROI_2D_5,FRAPData.VFRAP_ROI_ENUM.ROI_BLEACHED_RING8.name());
		addReplaceRoi(roiBleachedRing8_2D);
	}else{
		System.arraycopy(dilatedROI_2D_5.getPixels(), 0, roiBleachedRing8_2D.getRoiImages()[0].getPixels(), 0, dilatedROI_2D_5.getPixels().length);
	}
}

}