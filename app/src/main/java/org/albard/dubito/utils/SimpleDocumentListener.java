package org.albard.dubito.utils;

import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.BadLocationException;

public abstract class SimpleDocumentListener implements DocumentListener {
    @Override
    public final void changedUpdate(final DocumentEvent e) {
        try {
            this.updated(e.getDocument().getText(0, e.getLength()));
        } catch (final BadLocationException ex) {
        }
    }

    @Override
    public final void insertUpdate(final DocumentEvent e) {
        try {
            this.updated(e.getDocument().getText(0, e.getLength()));
        } catch (final BadLocationException ex) {
        }
    }

    @Override
    public final void removeUpdate(final DocumentEvent e) {
        try {
            this.updated(e.getDocument().getText(0, e.getLength()));
        } catch (final BadLocationException ex) {
        }
    }

    protected abstract void updated(final String newText);
}
