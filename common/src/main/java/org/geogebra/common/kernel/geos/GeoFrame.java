package org.geogebra.common.kernel.geos;

import org.geogebra.common.kernel.kernelND.GeoElementND;

public interface GeoFrame extends GeoElementND {

	void setReady();

	boolean isReady();
}
