package org.albard.mvc;

public final class ModelProperty<Y> {
    private final String name;

    private ModelProperty(final String name) {
        this.name = name;
    }

    public String getName() {
        return this.name;
    }

    public static <Y> ModelProperty<Y> define(final String name) {
        return new ModelProperty<>(name);
    }

    public static <Y> ModelProperty<Y> define(final String name, final Class<Y> propertyClass) {
        return new ModelProperty<>(name);
    }
}
