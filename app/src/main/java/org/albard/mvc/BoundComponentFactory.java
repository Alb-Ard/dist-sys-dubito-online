package org.albard.mvc;

import java.text.NumberFormat;
import java.util.Collection;
import java.util.function.BiConsumer;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPasswordField;
import javax.swing.JTextField;

import com.jgoodies.binding.adapter.Bindings;
import com.jgoodies.binding.beans.BeanAdapter;
import com.jgoodies.binding.list.SelectionInList;
import com.jgoodies.binding.value.ConverterFactory;
import com.jgoodies.binding.value.ValueModel;

public final class BoundComponentFactory {
    private BoundComponentFactory() {
    }

    public static JTextField createStringTextField(final BeanAdapter<?> beanAdapter, final String propertyName) {
        return bindComponent(new JTextField(), beanAdapter.getValueModel(propertyName), Bindings::bind);
    }

    public static JTextField createStringTextField(final AbstractModel<?> model, final ModelProperty<String> property) {
        return createStringTextField(model.getAdapter(), property.getName());
    }

    public static JTextField createIntegerTextField(final BeanAdapter<?> beanAdapter, final String propertyName) {
        return bindComponent(new JTextField(), getIntegerValueModel(beanAdapter, propertyName), Bindings::bind);
    }

    public static JTextField createIntegerTextField(final AbstractModel<?> model,
            final ModelProperty<Integer> property) {
        return createIntegerTextField(model.getAdapter(), property.getName());
    }

    public static JPasswordField createPasswordField(final BeanAdapter<?> beanAdapter, final String propertyName) {
        return bindComponent(new JPasswordField(), beanAdapter.getValueModel(propertyName), Bindings::bind);
    }

    public static JPasswordField createPasswordField(final AbstractModel<?> model,
            final ModelProperty<String> property) {
        return createPasswordField(model.getAdapter(), property.getName());
    }

    public static JLabel createStringLabel(final BeanAdapter<?> beanAdapter, final String propertyName) {
        return bindComponent(new JLabel(), beanAdapter.getValueModel(propertyName), Bindings::bind);
    }

    public static JLabel createStringLabel(final AbstractModel<?> model, final ModelProperty<String> property) {
        return createStringLabel(model.getAdapter(), property.getName());
    }

    public static JLabel createIntegerLabel(final BeanAdapter<?> beanAdapter, final String propertyName) {
        return bindComponent(new JLabel(), getIntegerValueModel(beanAdapter, propertyName), Bindings::bind);
    }

    public static JLabel createIntegerLabel(final AbstractModel<?> model, final ModelProperty<String> property) {
        return createIntegerLabel(model.getAdapter(), property.getName());
    }

    public static <X> JList<X> createList(final BeanAdapter<?> beanAdapter, final String propertyName) {
        return bindComponent(new JList<>(), getListValueModel(beanAdapter, propertyName), Bindings::bind);
    }

    public static <X, Y extends Collection<X>> JList<X> createList(final AbstractModel<?> model,
            final ModelProperty<Y> property) {
        return createList(model.getAdapter(), property.getName());
    }

    private static <X extends JComponent, Y extends ValueModel> X bindComponent(final X component, final Y valueModel,
            final BiConsumer<X, Y> binder) {
        binder.accept(component, valueModel);
        return component;
    }

    private static ValueModel getIntegerValueModel(final BeanAdapter<?> beanAdapter, final String propertyName) {
        return ConverterFactory.createStringConverter(beanAdapter.getValueModel(propertyName),
                NumberFormat.getIntegerInstance());
    }

    private static <X> SelectionInList<X> getListValueModel(final BeanAdapter<?> beanAdapter,
            final String propertyName) {
        return new SelectionInList<>(beanAdapter.getValueModel(propertyName));
    }
}
