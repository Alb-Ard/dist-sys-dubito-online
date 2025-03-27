package org.albard.dubito.utils;

import java.awt.CardLayout;
import java.awt.Container;
import java.util.Set;
import java.util.function.Function;

import javax.swing.JComponent;
import javax.swing.JFrame;

/**
 * A class that manages a JComponent layout and permits navigation between
 * screens. Note: When using this class, users should NOT directly manipulate or
 * change the frame layout and/or its contentPane layout
 */
public final class Navigator<X> {
    private final Container parent;
    private final CardLayout layout;
    private final Function<X, String> keyMapper;

    /**
     * Creates a new Navigator for the given JFrame
     * 
     * @param parent    The JFrame to manage
     * @param keyMapper A mapper from a navigator key to a string.
     */
    public Navigator(final Container parent, final Function<X, String> keyMapper) {
        this.parent = getActualParent(parent);
        this.keyMapper = keyMapper;
        this.layout = new CardLayout();
        this.parent.setLayout(this.layout);
    }

    /**
     * Adds a new screen to this navigator
     * 
     * @param screen The screen to add
     * @param keys   The key that will identify this screen
     * @return This Navigator instance
     */
    public Navigator<X> addScreen(final JComponent screen, final X key) {
        this.parent.add(screen, this.keyMapper.apply(key));
        return this;
    }

    /**
     * Adds a new screen to this navigator
     * 
     * @param screen The screen to add
     * @param keys   The keys that will identify this screen
     * @return This Navigator instance
     */
    public Navigator<X> addScreen(final JComponent screen, final Set<X> keys) {
        keys.forEach(key -> this.addScreen(screen, key));
        return this;
    }

    /**
     * Removes a previously added screen from this Navigator
     * 
     * @param screen The screen to remove
     * @return This Navigator instance
     */
    public Navigator<X> removeScreen(final JComponent screen) {
        this.parent.remove(screen);
        return this;
    }

    /**
     * Removes ALL screen from this Navigator
     * 
     * @return This Navigator instance
     */
    public Navigator<X> removeAll() {
        this.parent.removeAll();
        return this;
    }

    /**
     * Navigates to the given screen
     * 
     * @param key The key of the screen to navigate to
     */
    public void navigateTo(final X key) {
        this.layout.show(this.parent, this.keyMapper.apply(key));
    }

    private static Container getActualParent(final Container baseContainer) {
        return baseContainer instanceof JFrame frame ? frame.getContentPane() : baseContainer;
    }
}