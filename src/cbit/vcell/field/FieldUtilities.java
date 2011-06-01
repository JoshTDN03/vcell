/*
 * Copyright (C) 1999-2011 University of Connecticut Health Center
 *
 * Licensed under the MIT License (the "License").
 * You may not use this file except in compliance with the License.
 * You may obtain a copy of the License at:
 *
 *  http://www.opensource.org/licenses/mit-license.php
 */

package cbit.vcell.field;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.Vector;
import java.util.Map.Entry;

import org.vcell.util.document.ExternalDataIdentifier;

import cbit.vcell.math.MathException;
import cbit.vcell.math.MathFunctionDefinitions;
import cbit.vcell.parser.ASTFuncNode;
import cbit.vcell.parser.Expression;
import cbit.vcell.parser.ExpressionException;
import cbit.vcell.parser.FunctionInvocation;
import cbit.vcell.parser.Expression.FunctionFilter;
import cbit.vcell.simdata.VariableType;

public class FieldUtilities {
	
	public static class FieldFunctionFilter implements FunctionFilter {
		public boolean accept(String functionName) {
			return (functionName.equals(ASTFuncNode.getFunctionNames()[ASTFuncNode.FIELD]));
		}
	};


	public static void addFieldFuncArgsAndExpToCollection(Hashtable<FieldFunctionArguments, Vector<Expression>> fieldFuncArgsAndExpHash,Expression expression){
		if(expression == null){
			return;
		}
		FunctionFilter functionFilter = new FieldFunctionFilter();
		FunctionInvocation[] functionInvocations = expression.getFunctionInvocations(functionFilter);
		for (int i=0;i<functionInvocations.length;i++){
			Vector<Expression> expV = null;
			FieldFunctionArguments fieldFunctionArgs = new FieldFunctionArguments(functionInvocations[i]);
			if(fieldFuncArgsAndExpHash.contains(fieldFunctionArgs)){
				expV= fieldFuncArgsAndExpHash.get(fieldFunctionArgs);
			}else{
				expV = new Vector<Expression>();
				fieldFuncArgsAndExpHash.put(fieldFunctionArgs,expV);
				
			}
			if (!expV.contains(expression)){
				expV.add(expression);
			}
		}
	}

	public static FieldFunctionArguments[] getFieldFunctionArguments(Expression expression) {
		if(expression == null){
			return null;
		}
		FunctionFilter functionFilter = new FieldFunctionFilter();
		FunctionInvocation[] functionInvocations = expression.getFunctionInvocations(functionFilter);
		ArrayList<FieldFunctionArguments> fieldFuncArgsList = new ArrayList<FieldFunctionArguments>();
		for (int i=0;i<functionInvocations.length;i++){
			FieldFunctionArguments fieldFunctionArgs = new FieldFunctionArguments(functionInvocations[i]);
			if (!fieldFuncArgsList.contains(fieldFunctionArgs)){
				fieldFuncArgsList.add(fieldFunctionArgs);
			}
		}
		if (fieldFuncArgsList.size()==0){
			return null;
		}else{
			return fieldFuncArgsList.toArray(new FieldFunctionArguments[fieldFuncArgsList.size()]);
		}
	}

	public static Set<FunctionInvocation> getSizeFunctionInvocations(Expression expression) {
		if(expression == null){
			return null;
		}
		FunctionInvocation[] functionInvocations = expression.getFunctionInvocations(new FunctionFilter() {
			
			public boolean accept(String functionName) {
				if (functionName.equals(MathFunctionDefinitions.Function_regionArea_current.getFunctionName())
						|| functionName.equals(MathFunctionDefinitions.Function_regionVolume_current.getFunctionName())) {
					return true;
				}
				return false;
			}
		});
		Set<FunctionInvocation> fiSet = new HashSet<FunctionInvocation>();
		for (FunctionInvocation fi : functionInvocations){
			fiSet.add(fi);			
		}
		return fiSet;
	}

	private static String removeLiteralQuotes(Expression astLiteralExpression) {
		String infix = astLiteralExpression.infix();
		if (infix.startsWith("'") && infix.endsWith("'")) {
			return infix.substring(1, infix.length() - 1);
		}
		return infix;
	}

	public static void substituteFieldFuncNames(
			Hashtable<String, ExternalDataIdentifier> oldFieldFuncArgsNameNewID,
			Hashtable<FieldFunctionArguments, Vector<Expression>> fieldFuncArgsExpHash
			) throws MathException, ExpressionException{
	
		Enumeration<FieldFunctionArguments> keyEnum = fieldFuncArgsExpHash.keys();
		FunctionFilter functionFilter = new FieldFunctionFilter();
		while(keyEnum.hasMoreElements()){
			Vector<Expression> value = fieldFuncArgsExpHash.get(keyEnum.nextElement());
			for(int i=0;i<value.size();i++){
				Expression exp = value.elementAt(i);
				FunctionInvocation[] functionInvocations = exp.getFunctionInvocations(functionFilter);
				for (int j = 0; j < functionInvocations.length; j++) {
					Expression[] arguments = functionInvocations[j].getArguments();
					String oldFieldName = removeLiteralQuotes(arguments[0]);
					if(oldFieldFuncArgsNameNewID.containsKey(oldFieldName)){
						String varName = removeLiteralQuotes(arguments[1]);
						Expression timeExp = arguments[2];
						VariableType varType = VariableType.UNKNOWN;
						if (arguments.length>3){
							String vt = removeLiteralQuotes(arguments[3]);
							varType = VariableType.getVariableTypeFromVariableTypeName(vt);
						}
						String newFieldName = oldFieldFuncArgsNameNewID.get(oldFieldName).getName();
						FieldFunctionArguments newFieldFunctionArguments = new FieldFunctionArguments(newFieldName, varName, timeExp, varType);
						Expression newFunctionExp = new Expression(newFieldFunctionArguments.infix());
						exp.substituteInPlace(functionInvocations[j].getFunctionExpression(), newFunctionExp);
					}
				}
			}
		}
	}


}
