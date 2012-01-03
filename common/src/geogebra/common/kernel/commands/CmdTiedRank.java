package geogebra.common.kernel.commands;

import geogebra.common.kernel.Kernel;
import geogebra.common.kernel.geos.GeoElement;
import geogebra.common.kernel.geos.GeoList;

/*
 * Rank[ <List> ]
 */
public class CmdTiedRank extends CmdOneListFunction {

	public CmdTiedRank(Kernel kernel) {
		super(kernel);
	}

	final protected GeoElement doCommand(String a, GeoList b)
	{
		return kernelA.TiedRank(a, b);
	}


}
