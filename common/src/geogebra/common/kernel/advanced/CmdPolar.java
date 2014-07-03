package geogebra.common.kernel.advanced;

import geogebra.common.kernel.Kernel;
import geogebra.common.kernel.algos.AlgoPolarLine;
import geogebra.common.kernel.arithmetic.Command;
import geogebra.common.kernel.commands.CommandProcessor;
import geogebra.common.kernel.geos.GeoElement;
import geogebra.common.kernel.kernelND.GeoConicND;
import geogebra.common.kernel.kernelND.GeoPointND;
import geogebra.common.main.MyError;

/**
 * Polar[ <GeoPoint>, <GeoConic> ]
 */
public class CmdPolar extends CommandProcessor {

	/**
	 * Create new command processor
	 * 
	 * @param kernel
	 *            kernel
	 */
	public CmdPolar(Kernel kernel) {
		super(kernel);
	}

	@Override
	final public GeoElement[] process(Command c) throws MyError {
		int n = c.getArgumentNumber();
		boolean[] ok = new boolean[n];
		GeoElement[] arg;

		switch (n) {
		case 2:
			arg = resArgs(c);

			// polar line to point relative to conic
			if ((ok[0] = (arg[0].isGeoPoint()))
					&& (ok[1] = (arg[1].isGeoConic()))) {
				GeoElement[] ret = { PolarLine(c.getLabel(),
						(GeoPointND) arg[0], (GeoConicND) arg[1]) };
				return ret;
			}
			throw argErr(app, c.getName(), getBadArg(ok,arg));

		default:
			throw argNumErr(app, c.getName(), n);
		}
	}
	
	/**
	 * polar line to P relativ to c
	 */
	protected GeoElement PolarLine(String label, GeoPointND P, GeoConicND c) {
		AlgoPolarLine algo = new AlgoPolarLine(cons, label, c, P);
		return (GeoElement) algo.getLine();
	}


}
