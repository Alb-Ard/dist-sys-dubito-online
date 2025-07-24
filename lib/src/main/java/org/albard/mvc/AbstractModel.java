package org.albard.mvc;

import java.util.function.Consumer;
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

    /**
     * Adds a listener that will be invoked when the given property changes on this
     * model. The listener is also invoked when this method is called.
     * 
     * @param <Y>      The property type
     * @param property The property definition
     * @param listener The listener to invoke
     * @param wrapper  A wrapper that will be used around the listener when invoked.
     *                 May be null. Can be used to force invocation on a separate
     *                 thread (Ex. by passing SwingUtilities::invokeLater)
     * @return This model
     */
    public <Y> AbstractModel<X> addModelPropertyChangeListener(final ModelProperty<Y> property,
            final ModelPropertyChangeListener<Y> listener, final Consumer<Runnable> wrapper) {
        // If requested, wrap the listener
        final ModelPropertyChangeListener<Y> wrappedListener = wrapper != null
                ? (ev) -> wrapper.accept(() -> listener.propertyChange(ev))
                : listener;
        this.selfAdapter.addBeanPropertyChangeListener(property.getName(),
                ev -> wrappedListener.propertyChange(new ModelPropertyChangeEvent<>(ev)));
        @SuppressWarnings("unchecked")
        final Y currentValue = (Y) this.selfAdapter.getValue(property.getName());
        wrappedListener.propertyChange(new ModelPropertyChangeEvent<>(this, property, currentValue, currentValue));
        return this;
    }

    /**
     * Adds a listener that will be invoked when the given property changes on this
     * model. The listener is also invoked when this method is called.
     * 
     * @param <Y>              The property type
     * @param propertyProvider A provider that will return the property definition
     * @param listener         The listener to invoke
     * @param wrapper          A wrapper that will be used around the listener when
     *                         invoked. May be null. Can be used to force invocation
     *                         on a separate thread (Ex. by passing
     *                         SwingUtilities::invokeLater)
     * @return This model
     */
    @SuppressWarnings("unchecked")
    public <Y> AbstractModel<X> addModelPropertyChangeListener(final Function<X, ModelProperty<Y>> propertyProvider,
            final ModelPropertyChangeListener<Y> listener, final Consumer<Runnable> wrapper) {
        return this.addModelPropertyChangeListener(propertyProvider.apply((X) this), listener, wrapper);
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
