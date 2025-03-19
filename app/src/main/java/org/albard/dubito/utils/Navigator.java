package org.albard.dubito.utils;

import java.awt.CardLayout;
import java.awt.Container;
import java.util.function.Function;

import javax.swing.JComponent;
import javax.swing.JFrame;

/**
 * A class that manages a JComponent layout and permits navigation between
 * screens. Note: When using this class, users should NOT directly manipulate or
 * change the frame layout and/or its contentPane layout
 */
public final class Navigator<X> {
    final Container parent;
    final CardLayout layout;
    private final Function<X, String> keyMapper;

    /**
     * Creates a new Navigator for the given JFrame
     * 
     * @param parent The JFrame to manage
     */
    public Navigator(final Container parent, final Function<X, String> keyMapper) {
        this.parent = parent;
        this.keyMapper = keyMapper;
        this.layout = new CardLayout();
        this.getActualParent().setLayout(this.layout);
    }

    /**
     * Adds a new screen to this navigator
     * 
     * @param screen The screen to add
     * @param key    The key that will identify this screen
     * @return This Navigator instance
     */
    public Navigator<X> addScreen(final JComponent screen, final X key) {
        this.getActualParent().add(screen, this.keyMapper.apply(key));
        return this;
    }

    public Navigator<X> removeScreen(final JComponent screen) {
        this.getActualParent().remove(screen);
        return this;
    }

    public Navigator<X> removeAll() {
        this.getActualParent().removeAll();
        return this;
    }

    /**
     * Navigates to the given screen
     * 
     * @param key The key of the screen to navigate to
     */
    public void navigateTo(final X key) {
        this.layout.show(this.getActualParent(), this.keyMapper.apply(key));
    }

    private Container getActualParent() {
        if (this.parent instanceof JFrame frame) {
            return frame.getContentPane();
        }
        return this.parent;
    }
}