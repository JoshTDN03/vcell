package cbit.vcell.solver;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Map;
import java.util.Set;

import cbit.vcell.math.MathFunctionDefinitions;
import cbit.vcell.math.VariableType.VariableDomain;
import cbit.vcell.parser.ASTFuncNode.FunctionType;
import cbit.vcell.parser.Expression;
import cbit.vcell.parser.Expression.FunctionFilter;
import cbit.vcell.parser.ExpressionException;
import cbit.vcell.parser.FunctionInvocation;
import cbit.vcell.resource.ResourceUtil;

public class SolverUtilities {

	private static Map<SolverExecutable,File[]>  loaded = new Hashtable<SolverExecutable,File[]>( );

	public static Expression substituteSizeFunctions(Expression origExp, VariableDomain variableDomain) throws ExpressionException {
		Expression exp = new Expression(origExp);
		Set<FunctionInvocation> fiSet = SolverUtilities.getSizeFunctionInvocations(exp);
		for(FunctionInvocation fi : fiSet) {
			String functionName = fi.getFunctionName();
			// replace vcRegionArea('domain') and vcRegionVolume('domain') with vcRegionArea or vcRegionVolume or vcRegionVolume_domain
			// the decision is based on variable domain
			if (variableDomain.equals(VariableDomain.VARIABLEDOMAIN_VOLUME)) {
				exp.substituteInPlace(fi.getFunctionExpression(), new Expression(functionName));
			} else if (variableDomain.equals(VariableDomain.VARIABLEDOMAIN_MEMBRANE)) {
				if (functionName.equals(MathFunctionDefinitions.Function_regionArea_current.getFunctionName())) {
					exp.substituteInPlace(fi.getFunctionExpression(), new Expression(functionName));
				} else {
					String domainName = fi.getArguments()[0].infix();
					// remove single quote
					domainName = domainName.substring(1, domainName.length() - 1);
					exp.substituteInPlace(fi.getFunctionExpression(), new Expression(functionName + "_" + domainName));
				}
			}
		}
		return exp;
	}

	public static Set<FunctionInvocation> getSizeFunctionInvocations(Expression expression) {
		if(expression == null){
			return null;
		}
		FunctionInvocation[] functionInvocations = expression.getFunctionInvocations(new FunctionFilter() {
			
			@Override
			public boolean accept(String functionName, FunctionType functionType) {
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
	
	public static boolean isPowerOf2(int n) {
		return n != 0 && ((n & (n-1)) == 0);
	}

	/**
	 * Ensure solvers extracted from resources and registered as property
	 * @param cf
	 * @return array of exes used by provided solver
	 * @throws IOException, {@link UnsupportedOperationException} if no exe for this solver
	 */
	public static File[] getExes(SolverDescription sd) throws IOException {
		SolverExecutable se = sd.getSolverExecutable(); 
		if (se != null) {
			if (loaded.containsKey(se)) {
				return loaded.get(se);
			}
			SolverExecutable.NameInfo nameInfos[] = se.getNameInfo(); 
			File files[] = new File[nameInfos.length];
			for (int i = 0; i < nameInfos.length; ++i) {
				SolverExecutable.NameInfo ni = nameInfos[i];
				File exe = ResourceUtil.loadSolverExecutable(ni.exeName, sd.versionedLibrary);
				System.getProperties().put(ni.propertyName,exe.getAbsolutePath());
				files[i] = exe; 
			}
			loaded.put(se, files);
			return files;
		}
		throw new UnsupportedOperationException("SolverDescription " + sd + " has no executable");
	}

	/**
	 * calls {@link #getExes(SolverConfig)} if solver requires executables,
	 * no-op otherwise
	 */
	public static void prepareSolverExecutable(SolverDescription solverDescription) throws IOException {
		if (solverDescription.getSolverExecutable() != null) {
			getExes(solverDescription);
		}
	}

}
