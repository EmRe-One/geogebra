package org.geogebra.web.full.gui.openfileview;

import java.util.ArrayList;
import java.util.List;

import org.geogebra.common.main.Feature;
import org.geogebra.common.main.OpenFileListener;
import org.geogebra.common.move.ggtapi.models.Chapter;
import org.geogebra.common.move.ggtapi.models.Material;
import org.geogebra.common.move.ggtapi.models.Material.Provider;
import org.geogebra.common.move.ggtapi.requests.MaterialCallbackI;
import org.geogebra.common.util.debug.Log;
import org.geogebra.web.full.css.MaterialDesignResources;
import org.geogebra.web.full.gui.MyHeaderPanel;
import org.geogebra.web.full.gui.dialog.DialogManagerW;
import org.geogebra.web.full.main.BrowserDevice.FileOpenButton;
import org.geogebra.web.html5.gui.FastClickHandler;
import org.geogebra.web.html5.gui.util.NoDragImage;
import org.geogebra.web.html5.gui.util.StandardButton;
import org.geogebra.web.html5.gui.view.browser.BrowseViewI;
import org.geogebra.web.html5.gui.view.browser.MaterialListElementI;
import org.geogebra.web.html5.main.AppW;
import org.geogebra.web.shared.ggtapi.models.GeoGebraTubeAPIW;
import org.geogebra.web.shared.ggtapi.models.MaterialCallback;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.Widget;

/**
 * View for browsing materials
 * 
 * @author Alicia
 *
 */
public class OpenFileView extends MyHeaderPanel
		implements BrowseViewI, OpenFileListener {
	/**
	 * application
	 */
	protected AppW app;
	// header
	private FlowPanel headerPanel;
	private StandardButton backBtn;
	private Label headerCaption;

	// content panel
	private FlowPanel contentPanel;
	// button panel
	private HorizontalPanel buttonPanel;
	private StandardButton newFileBtn;
	private FileOpenButton openFileBtn;

	// dropdown
	private ListBox sortDropDown;

	// material panel
	private FlowPanel materialPanel;
	private MaterialCallbackI ggtMaterialsCB;
	private MaterialCallbackI userMaterialsCB;

	private boolean materialListEmpty = true;

	/**
	 * @param app
	 *            application
	 * @param openFileButton
	 *            button to open file picker
	 */
	public OpenFileView(AppW app, FileOpenButton openFileButton) {
		this.app = app;
		this.openFileBtn = openFileButton;
		initGUI();
	}

	private void initGUI() {
		this.setStyleName("openFileView");
		this.userMaterialsCB = getUserMaterialsCB();
		this.ggtMaterialsCB = getGgtMaterialsCB();
		initHeader();
		initContentPanel();
		initButtonPanel();
		initSortDropdown();
		initMaterialPanel();
	}

	/**
	 * adds content if available, notification otherwise
	 */
	protected void addContent() {
		if (materialListEmpty) {
			showEmptyListNotification();
			setExtendedButtonStyle();
			contentPanel.add(buttonPanel);
		} else {
			contentPanel.add(buttonPanel);
			contentPanel.add(sortDropDown);
			contentPanel.add(materialPanel);
		}
	}

	private void initHeader() {
		headerPanel = new FlowPanel();
		headerPanel.setStyleName("openFileViewHeader");

		backBtn = new StandardButton(
				MaterialDesignResources.INSTANCE.mow_back_arrow(),
				null, 24,
				app);
		backBtn.setStyleName("headerBackButton");
		backBtn.addFastClickHandler(new FastClickHandler() {

			@Override
			public void onClick(Widget source) {
				close();
			}
		});
		headerPanel.add(backBtn);

		headerCaption = new Label(
				localize("mow.openFileViewTitle"));
		headerCaption.setStyleName("headerCaption");
		headerPanel.add(headerCaption);

		this.setHeaderWidget(headerPanel);
	}

	private void initContentPanel() {
		contentPanel = new FlowPanel();
		contentPanel.setStyleName("fileViewContentPanel");
		this.setContentWidget(contentPanel);
	}

	private void initButtonPanel() {
		buttonPanel = new HorizontalPanel();
		buttonPanel.setStyleName("fileViewButtonPanel");

		newFileBtn = new StandardButton(
				MaterialDesignResources.INSTANCE.add_black(),
				localize("mow.newFile"), 18, app);
		newFileBtn.setStyleName("containedButton");
		newFileBtn.addFastClickHandler(new FastClickHandler() {

			@Override
			public void onClick(Widget source) {
				newFile();
			}
		});
		buttonPanel.add(newFileBtn);

		openFileBtn.setImageAndText(
				MaterialDesignResources.INSTANCE.mow_pdf_open_folder()
						.getSafeUri().asString(),
				localize("mow.openFile"));
		openFileBtn.addStyleName("buttonMargin");
		buttonPanel.add(openFileBtn);
	}

	private void initSortDropdown() {
		sortDropDown = new ListBox();
		sortDropDown.setMultipleSelect(false);
		sortDropDown.addItem(localize("SortBy"));
		sortDropDown.getElement().getFirstChildElement()
				.setAttribute("disabled", "disabled");
		sortDropDown.addItem(localize("sort_author")); // index 1
		sortDropDown.addItem(localize("sort_title")); // index 2
		sortDropDown.addItem(localize("sort_date_created")); // index 3
		sortDropDown.addItem(localize("sort_last_modified")); // index 4
		sortDropDown.setSelectedIndex(4);
		sortDropDown.addChangeHandler(new ChangeHandler() {

			@Override
			public void onChange(ChangeEvent event) {
				// TODO sort material cards according to selected sort mode
			}
		});
	}

	private void initMaterialPanel() {
		materialPanel = new FlowPanel();
		materialPanel.addStyleName("materialPanel");
		// materialPanel.add(new MaterialCard(null, app));
	}

	private String localize(String id) {
		return app.getLocalization().getMenu(id);
	}

	/**
	 * start a new file
	 */
	protected void newFile() {
		Runnable newConstruction = new Runnable() {

			@Override
			public void run() {
				app.setWaitCursor();
				app.fileNew();
				app.setDefaultCursor();

				if (!app.isUnbundledOrWhiteboard()) {
					app.showPerspectivesPopup();
				}
				if (app.has(Feature.MOW_MULTI_PAGE)
						&& app.getPageController() != null) {
					app.getPageController().resetPageControl();
				}
			}
		};
		((DialogManagerW) getApp().getDialogManager()).getSaveDialog()
				.showIfNeeded(newConstruction);
		close();
	}

	/**
	 * @param fileToHandle
	 *            JS file object
	 * @param callback
	 *            callback after file is open
	 */
	public void openFile(final JavaScriptObject fileToHandle,
			final JavaScriptObject callback) {
		if (app.getLAF().supportsLocalSave()) {
			app.getFileManager().setFileProvider(Provider.LOCAL);
		}
		app.openFile(fileToHandle, callback);
		close();
	}

	private void showEmptyListNotification() {
		FlowPanel imagePanel = new FlowPanel();
		imagePanel.setStyleName("emptyMaterialListInfo");
		Image image = new NoDragImage(
				MaterialDesignResources.INSTANCE.more_vert_mebis(), 112, 112);

		Label caption = new Label(localize("emptyMaterialList.caption.mow"));
		caption.setStyleName("caption");
		Label info = new Label(localize("emptyMaterialList.info.mow"));
		info.setStyleName("info");

		imagePanel.add(image);
		imagePanel.add(caption);
		imagePanel.add(info);

		contentPanel.add(imagePanel);
	}

	private void setExtendedButtonStyle() {
		newFileBtn.setStyleName("extendedFAB");
		openFileBtn.setStyleName("extendedFAB");
		openFileBtn.addStyleName("buttonMargin");
		buttonPanel.addStyleName("center");
	}

	@Override
	public AppW getApp() {
		return app;
	}

	@Override
	public void resizeTo(int width, int height) {
		// TODO Auto-generated method stub
	}

	@Override
	public void setMaterialsDefaultStyle() {
		// TODO Auto-generated method stub
	}

	@Override
	public void loadAllMaterials() {
		if (this.app.getLoginOperation().isLoggedIn()) {
			((GeoGebraTubeAPIW) app.getLoginOperation().getGeoGebraTubeAPI())
					.getUsersMaterials(this.userMaterialsCB);
		} else {
			((GeoGebraTubeAPIW) app.getLoginOperation().getGeoGebraTubeAPI())
					.getFeaturedMaterials(this.ggtMaterialsCB);
		}
	}

	@Override
	public void clearMaterials() {
		materialPanel.clear();
	}

	@Override
	public void disableMaterials() {
		// TODO Auto-generated method stub
	}

	@Override
	public void onSearchResults(List<Material> response,
			ArrayList<Chapter> chapters) {
		// TODO Auto-generated method stub
	}

	@Override
	public void displaySearchResults(String query) {
		// TODO Auto-generated method stub
	}

	@Override
	public void refreshMaterial(Material material, boolean isLocal) {
		// TODO Auto-generated method stub
	}

	@Override
	public void rememberSelected(MaterialListElementI materialElement) {
		// TODO Auto-generated method stub
	}

	@Override
	public void setLabels() {
		headerCaption.setText(
				app.getLocalization().getMenu("mow.openFileViewTitle"));
	}

	@Override
	public void addMaterial(Material material) {
		materialPanel.add(new MaterialCard(material, app));
	}

	@Override
	public void removeMaterial(Material material) {
		// TODO Auto-generated method stub
	}

	@Override
	public boolean onOpenFile() {
		// TODO
		return false;
	}

	private MaterialCallback getUserMaterialsCB() {
		return new MaterialCallback() {

			@Override
			public void onLoaded(final List<Material> parseResponse,
					ArrayList<Chapter> meta) {
				addUsersMaterials(parseResponse);
				addContent();
			}
		};
	}

	/**
	 * Adds the given {@link Material materials}.
	 * 
	 * @param matList
	 *            List of materials
	 */
	public void addUsersMaterials(final List<Material> matList) {
		if (matList.size() > 0) {
			materialListEmpty = false;
		}
		for (int i = matList.size() - 1; i >= 0; i--) {
			addMaterial(matList.get(i));
		}
	}

	private MaterialCallback getGgtMaterialsCB() {
		return new MaterialCallback() {
			@Override
			public void onError(final Throwable exception) {
				exception.printStackTrace();
				Log.debug(exception.getMessage());
			}

			@Override
			public void onLoaded(final List<Material> response,
					ArrayList<Chapter> meta) {
				addGGTMaterials(response, meta);
				addContent();
			}
		};
	}

	/**
	 * adds the new materials (matList) - GeoGebraTube only
	 * 
	 * @param matList
	 *            List of materials
	 * @param chapters
	 *            list of book chapters
	 */
	public final void addGGTMaterials(final List<Material> matList,
			final ArrayList<Chapter> chapters) {
		if (matList.size() > 0) {
			materialListEmpty = false;
		}
		if (chapters == null || chapters.size() < 2) {
			for (final Material mat : matList) {
				addMaterial(mat);
			}
		}
	}
}
