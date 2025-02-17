package org.albard.dubito.lobby.app.demoViewer.models;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.function.Consumer;

public abstract class AbstractModel {
    private final PropertyChangeSupport propertyChangeSupport;

    public AbstractModel() {
        this.propertyChangeSupport = new PropertyChangeSupport(this);
    }

    public void addPropertyChangeListener(final PropertyChangeListener listener) {
        this.propertyChangeSupport.addPropertyChangeListener(listener);
    }

    public void addPropertyChangeListener(final String propertyName, final PropertyChangeListener listener) {
        this.propertyChangeSupport.addPropertyChangeListener(propertyName, listener);
    }

    public void removePropertyChangeListener(final PropertyChangeListener listener) {
        this.propertyChangeSupport.removePropertyChangeListener(listener);
    }

    protected <X> void firePropertyChange(final String propertyName, final X oldValue, final X newValue) {
        this.propertyChangeSupport.firePropertyChange(propertyName, oldValue, newValue);
    }

    protected <X> void firePropertyChange(final String propertyName, final X oldValue, final X newValue,
            final Consumer<X> setter) {
        setter.accept(newValue);
        this.firePropertyChange(propertyName, oldValue, newValue);
    }
}