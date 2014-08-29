package cbit.vcell.math;


/**
 * boolean getters of {@link MathDescription} that
 * determine which Solver to select
 * @author gweatherby
 *
 */
public interface SolverSelector {

	public abstract boolean isSpatial();

	public abstract boolean isSpatialHybrid();

	public abstract boolean isSpatialStoch();

	public abstract boolean isNonSpatialStoch();
	
	public abstract boolean hasDirichletAtMembrane();
	
	public abstract boolean hasFastSystems();
	
	/**
	 * validator
	 */
	public static class Checker {
		
		/**
		 * return true if SolverSelector state is valid
		 * @param ss to check
		 */
		public static boolean isValid(SolverSelector ss) {
			return validateLogic(ss) == null;
		}
		
		/**
		 * throw exception if not valid
		 * @param ss to check
		 * @throws IllegalStateException
		 */
		public  static void validate(SolverSelector ss) {
			 String msg = validateLogic(ss);
			 if (msg != null) {
				 throw new IllegalStateException(ss + msg);
			 }
		}
		/**
		 * common implementation of {@link #isValid(SolverSelector)} and {@link #validate(SolverSelector)} logic
		 * @param ss
		 * @return null if good, error message if not
		 */
		private static String validateLogic(SolverSelector ss) {
			if (ss.isNonSpatialStoch() && ss.isSpatial()) {
				return ": invalid state: non spatial stochastic may not be spatial";
			}
			if (ss.isSpatialStoch() && !ss.isSpatial()) {
				return ": invalid state: spatial stochastic must be spatial";
			}
			if (ss.isSpatialHybrid() && !ss.isSpatial()) {
				return ": invalid state: spatial hybrid must be spatial";
			}
			return null;
		}
	}
	/**
	 * Facade to provide human readable String 
	 */
	public static class Explain {
		public static String describe(SolverSelector selector) {
			String desc = selector.isSpatial() ? "Spatial " : "Non-spatial";
			if (selector.isSpatialHybrid()) {
				desc += " hybrid";
			}
			if (selector.isSpatialStoch() || selector.isNonSpatialStoch()) {
				desc += " stoch";
			}
			if (selector.hasFastSystems()) {
				desc += " fast";
			}
			if (selector.hasDirichletAtMembrane()) {
				desc += " dirichlet";
			}
			return desc; 
		}
	}
}