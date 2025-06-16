package org.albard.mvc;

import java.util.function.Function;
import java.util.function.Supplier;

import com.jgoodies.binding.beans.Model;

public abstract class AbstractModel<X extends AbstractModel<X>> extends Model {
    private final ModelAdapter<X> selfAdapter;

    public AbstractModel() {
        this.selfAdapter = new ModelAdapter<>(this);
    }

    public ModelAdapter<X> getAdapter() {
        return this.selfAdapter;
    }

    public <Y> AbstractModel<X> addModelPropertyChangeListener(final ModelProperty<Y> property,
            final ModelPropertyChangeListener<Y> listener) {
        this.selfAdapter.addBeanPropertyChangeListener(property.getName(),
                ev -> listener.propertyChange(new ModelPropertyChangeEvent<Y>(ev)));
        return this;
    }

    public <Y> AbstractModel<X> addModelPropertyChangeListener(final Function<X, ModelProperty<Y>> propertyProvider,
            final ModelPropertyChangeListener<Y> listener) {
        return this.addModelPropertyChangeListener(propertyProvider.apply((X) this), listener);
    }

    protected <Y> void firePropertyChange(final ModelProperty<Y> property, final Y oldValue,
            final Supplier<Y> newValueProvider) {
        final Y newValue = newValueProvider.get();
        this.firePropertyChange(property.getName(), oldValue, newValue);
    }

    protected <Y> void firePropertyChange(final ModelProperty<Y> property, final Y oldValue,
            final Supplier<Y> newValueProvider, final boolean checkIdentity) {
        final Y newValue = newValueProvider.get();
        this.firePropertyChange(property.getName(), oldValue, newValue, checkIdentity);
    }

    protected static <Y> ModelProperty<Y> defineProperty(final String name) {
        return ModelProperty.define(name);
    }

    protected static <Y> ModelProperty<Y> defineProperty(final String name, final Class<Y> propertyType) {
        return ModelProperty.define(name, propertyType);
    }
}
