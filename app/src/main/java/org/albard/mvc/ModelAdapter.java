package org.albard.mvc;

import com.jgoodies.binding.beans.BeanAdapter;

public final class ModelAdapter<X extends AbstractModel<X>> extends BeanAdapter<AbstractModel<X>> {
    public ModelAdapter(final AbstractModel<X> bean) {
        super(bean, true);
    }

    public <Y> ModelAdapter<X> addModelPropertyChangeListener(ModelProperty<Y> property,
            ModelPropertyChangeListener<Y> listener) {
        super.addBeanPropertyChangeListener(property.getName(),
                ev -> listener.propertyChange(new ModelPropertyChangeEvent<Y>(ev)));
        return this;
    }
}
