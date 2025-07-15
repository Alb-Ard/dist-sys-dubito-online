package org.albard.mvc;

import java.beans.PropertyChangeEvent;

public final class ModelPropertyChangeEvent<X> extends PropertyChangeEvent {
    public ModelPropertyChangeEvent(final Object source, ModelProperty<X> property, X oldValue, X newValue) {
        super(source, property.getName(), oldValue, newValue);
    }

    public ModelPropertyChangeEvent(final PropertyChangeEvent from) {
        super(from.getSource(), from.getPropertyName(), from.getOldValue(), from.getNewValue());
    }

    @SuppressWarnings("unchecked")
    public X getNewTypedValue() {
        return (X) this.getNewValue();
    }

    @SuppressWarnings("unchecked")
    public X getOldTypedValue() {
        return (X) this.getOldValue();
    }
}