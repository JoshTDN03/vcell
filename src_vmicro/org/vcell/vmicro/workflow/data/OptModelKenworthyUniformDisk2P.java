package org.vcell.vmicro.workflow.data;

import cbit.vcell.opt.Parameter;
import cern.jet.math.Bessel;

public class OptModelKenworthyUniformDisk2P extends OptModel {

	private static final String NAME = "uniform disk bleach area fluorescence with bleach while monitoring";
	private final static int INDEX_DIFFUSION_RATE = 0;
	private final static int INDEX_BLEACH_AMPLITUDE = 1;
	
	private final static String[] MODEL_PARAMETER_NAMES = {
		"DiffusionRate",
		"BleachAmplitude",
	};
	
	private final double bleachingRadius;


	public OptModelKenworthyUniformDisk2P(double bleachingRadius) {
		super(NAME, new Parameter[] {
				new Parameter(MODEL_PARAMETER_NAMES[INDEX_DIFFUSION_RATE], 0.1, 200, 1.0, 1.0),
				new Parameter(MODEL_PARAMETER_NAMES[INDEX_BLEACH_AMPLITUDE], 0.01, 1, 1.0, 0.5),
			});
		this.bleachingRadius = bleachingRadius;
	}

	private double getValueFromParameters(double diffusion, double bleachAmplitude, double bleachWhileMonitoringRate, double t)
	{
		if (diffusion<=-1e-5){
			throw new RuntimeException("diffusion must be non-negative, diff = "+diffusion);
		}
		double tau = bleachingRadius*bleachingRadius/(4*diffusion);
		double arg = 2*tau/t;
		//
		// F(t) = exp(-2*tau/t)*(I0(2*tau/t) + I1(2*tau/t))
		//
		double I0e_value = Bessel.i0e(arg);  // exp(-x)*I0(x)
		double I1e_value = Bessel.i1e(arg);  // exp(-x)*I1(x)
		double result = Math.exp(-t*bleachWhileMonitoringRate) * ((1-bleachAmplitude) + bleachAmplitude*(I0e_value + I1e_value));
		
		return result;
	}

	/**
	 * returns the expected fluorescence under the bleaching area for a uniform disk
	 */
	@Override
	public double[][] getSolution(double[] newParams, double[] solutionTimePoints) {

		double diffusionRate;
		double bleachAmplitude; // 0 to 1 (1 means complete bleaching)

		if (newParams.length==1){
			if (isFixedParameter(MODEL_PARAMETER_NAMES[INDEX_DIFFUSION_RATE])){
				diffusionRate = getFixedParameterValue();
				bleachAmplitude = newParams[0];
			}else if (isFixedParameter(MODEL_PARAMETER_NAMES[INDEX_BLEACH_AMPLITUDE])){
				diffusionRate = newParams[0];
				bleachAmplitude = getFixedParameterValue();
			}else{
				throw new RuntimeException("unexpected fixed parameter");
			}
		}else{
			diffusionRate = newParams[INDEX_DIFFUSION_RATE];
			bleachAmplitude = newParams[INDEX_BLEACH_AMPLITUDE];
		}
		
		double[][] solutionData = new double[1][solutionTimePoints.length];
		
		for (int j = 0; j < solutionTimePoints.length; j++) {
			double value = getValueFromParameters(diffusionRate, bleachAmplitude, 0, solutionTimePoints[j]);
			solutionData[0][j] = value;
		}
		return solutionData;
	}

	@Override
	public double getPenalty(double[] parameters2) {
		return 0;
	}

}
