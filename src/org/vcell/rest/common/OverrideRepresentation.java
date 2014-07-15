package org.vcell.rest.common;

import cbit.vcell.parser.ExpressionException;
import cbit.vcell.solver.ConstantArraySpec;
import cbit.vcell.solver.MathOverrides;

public class OverrideRepresentation {

	public final static String OVERRIDE_TYPE_Variable = "Variable";
	public final static String OVERRIDE_TYPE_Single = "Single";
	public final static String OVERRIDE_TYPE_List = "List";
	public final static String OVERRIDE_TYPE_LinearInterval = "LinearInterval";
	public final static String OVERRIDE_TYPE_LogInterval = "LogInterval";

	public final String name;
	public final String type;
	public final String expression;
	public final double[] values;
	public final int cardinality;
	
	public OverrideRepresentation(String name, String type, int cardinality, double[] values, String expression) {
		this.name = name;
		this.type = type;
		this.cardinality = cardinality;
		this.values = values;
		this.expression = expression;
	}
	
	public OverrideRepresentation(MathOverrides.Element element) throws ExpressionException{
		this.name = element.getName();
		
		//
		// single value (overridden to another real number ... no scan)
		//
		if (element.getActualValue()!=null){
			this.cardinality = 1;
			if (element.getActualValue().isNumeric()){
				this.type = OVERRIDE_TYPE_Single;
				this.values = new double[] { element.getActualValue().evaluateConstant() };
				this.expression = null;
			}else{
				this.type = OVERRIDE_TYPE_Variable;
				this.values = null;
				this.expression = element.getActualValue().infix();
			}
		//
		// multiple values
		//
		}else if (element.getSpec()!=null){
			this.expression = null;
			ConstantArraySpec arraySpec = element.getSpec();
			this.cardinality = arraySpec.getNumValues();
			switch (arraySpec.getType()){
			//
			// explicit list of overridden values
			//
			case ConstantArraySpec.TYPE_LIST:{
				this.type = OVERRIDE_TYPE_List;
				this.values = new double[this.cardinality];
				for (int i=0;i<cardinality;i++){
					values[i] = arraySpec.getConstants()[i].getExpression().evaluateConstant();
				}
				break;
			}
			//
			// specified number of values within a range (either linear or log spacing)
			//
			case ConstantArraySpec.TYPE_INTERVAL:{
				if (arraySpec.isLogInterval()){
					this.type = OVERRIDE_TYPE_LogInterval;
				}else{
					this.type = OVERRIDE_TYPE_LinearInterval;
				}
				this.values = new double[] { arraySpec.getMinValue(), arraySpec.getMaxValue() };
				break;
			}
			default:{
				throw new RuntimeException("unexpected constant array spec type "+arraySpec.getType());
			}
			}
		}else{
			throw new RuntimeException("expecting either an actualValue or a constant array spec in math override for "+element.getName());
		}
	}
	
	public String getSerialization(){
		if (type.equals(OVERRIDE_TYPE_List)){
			return String.valueOf(values[0]);
		}
		if (type.equals(OVERRIDE_TYPE_Variable)){
			return expression;
		}
		if (type.equals(OVERRIDE_TYPE_List)){
			StringBuffer buffer = new StringBuffer();
			buffer.append("[");
			for (double val : values){
				buffer.append(val+" ");
			}
			buffer.append("]");
		}
		if (type.equals(OVERRIDE_TYPE_LinearInterval)){
			return "linearInterval(length="+cardinality+",min="+values[0]+",max="+values[1]+")";
		}
		if (type.equals(OVERRIDE_TYPE_LogInterval)){
			return "logInterval(length="+cardinality+",min="+values[0]+",max="+values[1]+")";
		}
		return "unknownType";
	}
	
	public String getName() {
		return name;
	}

	public String getType() {
		return type;
	}

	public double[] getValues() {
		return values;
	}

	public int getCardinality() {
		return cardinality;
	}

	public String getExpression() {
		return expression;
	}
}
