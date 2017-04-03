package org.geogebra.keyboard.base;

import org.geogebra.keyboard.base.linear.LinearKeyboard;
import org.geogebra.keyboard.base.listener.KeyboardObserver;

/**
 * An internal keyboard controller.
 */
public interface Keyboard {

    /**
     * Returns the keyboard model. Controllers should use this
     * to build and refresh the keyboard view.
     *
     * @return the model
     */
    LinearKeyboard getModel();

    /**
     * Register a keyboard observer to refresh the view.
     *
     * @param observer keyboard observer
     */
    void registerKeyboardObserver(KeyboardObserver observer);

    /**
     * Toggle the accent on/off.
     *
     * @param accent one of {@link ButtonConstants} with the prefix <i>ACCENT</i>.
     */
    void toggleAccent(String accent);

    /**
     * Disable the caps lock.
     */
    void disableCapsLock();

    /**
     * Toggle the caps lock on/off.
     */
    void toggleCapsLock();
}
