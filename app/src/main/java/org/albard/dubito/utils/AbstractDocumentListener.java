package org.albard.dubito.utils;

import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

public abstract class AbstractDocumentListener implements DocumentListener {
    @Override
    public void changedUpdate(final DocumentEvent e) {
    }

    @Override
    public void insertUpdate(final DocumentEvent e) {
    }

    @Override
    public void removeUpdate(final DocumentEvent e) {
    }
}
