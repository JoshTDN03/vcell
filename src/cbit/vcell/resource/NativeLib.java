package cbit.vcell.resource;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import cbit.vcell.util.NativeLoader;

/**
 * Known / named collections of library
 * @author gweatherby
 */
public enum NativeLib {
	VTK("vtk"),
	NATIVE_SOLVERS("NativeSolvers"),
	SBML("sbmlj"),
	COPASI("vcellCopasiOptDriver"),
	COPASI_JAVA("CopasiJava");
	
	private final String libName;
	private boolean loaded = false;
	private static final Logger lg = Logger.getLogger(NativeLib.class);

	private NativeLib(String libName) {
		this.libName = libName;
	}

	/**
	 *  commence load process but don't wait for results
	 */
	public void initLoad( ) {
		NativeLoader.load(libName);
	}

	public void load( ) { 
		if (loaded) {
			return;
		}
		Future<Boolean> r = NativeLoader.load(libName);
		try {
			r.get( );
		} catch (InterruptedException | ExecutionException e) {
				if (lg.isEnabledFor(Level.WARN)) {
					lg.warn("Can't load " + libName,e);
				}
			throw new RuntimeException("Can't load " + toString(), e);
		}
		loaded = true;
	}
	
	/**
	 * find whether underlying thread is complete
	 * @return
	 */
	public boolean isDone( ) {
		return NativeLoader.load(libName).isDone();
	}

}
