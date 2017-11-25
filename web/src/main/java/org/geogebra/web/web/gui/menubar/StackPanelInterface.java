package org.geogebra.web.web.gui.menubar;

import com.google.gwt.event.dom.client.DomEvent.Type;
import com.google.gwt.event.shared.EventHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.annotations.IsSafeHtml;
import com.google.gwt.safehtml.shared.annotations.SuppressIsSafeHtmlCastCheck;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.ui.InsertPanel;
import com.google.gwt.user.client.ui.Widget;

public interface StackPanelInterface extends InsertPanel {
	/**
	 * Adds a new child with the given widget and header.
	 *
	 * @param w
	 *            the widget to be added
	 * @param stackText
	 *            the header text associated with this widget
	 */

	@SuppressIsSafeHtmlCastCheck
	public void add(Widget w, String stackText);

	/**
	   * Adds a new child with the given widget and header, optionally interpreting
	   * the header as HTML.
	   *
	   * @param w the widget to be added
	   * @param stackHtml the header html associated with this widget
	   */
	public void add(Widget w, SafeHtml stackHtml);

	/**
	 * Adds a new child with the given widget and header, optionally
	 * interpreting the header as HTML.
	 *
	 * @param w
	 *            the widget to be added
	 * @param stackText
	 *            the header text associated with this widget
	 * @param asHTML
	 *            <code>true</code> to treat the specified text as HTML
	 */
	public void add(Widget w, @IsSafeHtml String stackText, boolean asHTML);

	/**
	   * Gets the currently selected child index.
	   *
	   * @return selected child
	   */
	public int getSelectedIndex();

	public void onBrowserEvent(Event event);

	public boolean remove(int index);

	/**
	   * Sets the text associated with a child by its index.
	   *
	   * @param index the index of the child whose text is to be set
	   * @param text the text to be associated with it
	   */
	  @SuppressIsSafeHtmlCastCheck
	public void setStackText(int index, String text);

	/**
	   * Sets the html associated with a child by its index.
	   *
	   * @param index the index of the child whose text is to be set
	   * @param html the html to be associated with it
	   */
	public void setStackText(int index, SafeHtml html);

	/**
	   * Sets the text associated with a child by its index.
	   *
	   * @param index the index of the child whose text is to be set
	   * @param text the text to be associated with it
	   * @param asHTML <code>true</code> to treat the specified text as HTML
	   */
	public void setStackText(int index, @IsSafeHtml String text,
			boolean asHTML);
	/**
	   * Shows the widget at the specified child index.
	   *
	   * @param index the index of the child to be shown
	   */
	public void showStack(int index);

	/**
	   * Adds the {@code styleName} on the {@code <tr>} for the header specified by {@code index}.
	   *
	   * @param index the index of the header row to apply to the style to
	   * @param styleName the name of the class to add
	   */
	public void addHeaderStyleName(int index, String styleName);

	/**
	 * Removes the {@code styleName} off the {@code 
	 * <tr>
	 * } for the header specified by {@code index}.
	 *
	 * @param index
	 *            the index of the header row to remove the style from
	 * @param styleName
	 *            the name of the class to remove
	 */
	public void removeHeaderStyleName(int index, String styleName);

	public <H extends EventHandler>
			HandlerRegistration addDomHandler(H handler,
			Type<H> type);

	public void addStyleName(String string);

	public boolean remove(Widget w);
}