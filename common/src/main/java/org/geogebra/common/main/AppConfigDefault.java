package org.geogebra.common.main;

import org.geogebra.common.io.layout.DockPanelData;

public class AppConfigDefault implements AppConfig {

	@Override
	public void adjust(DockPanelData dp) {
		// do nothing
	}

	@Override
	public String getAVTitle() {
		return "Algebra";
	}

	@Override
	public int getLineDisplayStyle() {
		return -1;
	}

	@Override
	public String getAppTitle() {
		return "math_apps";
	}

	/**
	 * @param appName
	 *            app name
	 * @return whether app name is one of the unbundled apps
	 */
	public static boolean isUnbundledOrWhiteboard(String appName) {
		return "graphing".equals(appName) || "geometry".equals(appName)
				|| "whiteboard".equals(appName) || "3d".equals(appName);
	}

	@Override
	public String getTutorialKey() {
		return "TutorialClassic";
	}

	@Override
	public boolean showKeyboardHelpButton() {
		return true;
	}

	@Override
	public boolean showObjectSettingsFromAV() {
		return true;
	}

	@Override
	public boolean isSimpleMaterialPicker() {
		return false;
	}

	@Override
	public boolean hasPreviewPoints() {
		return false;
	}

}
