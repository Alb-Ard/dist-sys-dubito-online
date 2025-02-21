package org.albard.dubito.utils;

import java.util.Arrays;

import javax.swing.BoxLayout;
import javax.swing.JComponent;
import javax.swing.JPanel;

public final class SimpleComponentFactory {
    private SimpleComponentFactory() {
    }

    public static JComponent createHorizontalPanel(final JComponent... elements) {
        final JPanel container = new JPanel();
        container.setLayout(new BoxLayout(container, BoxLayout.X_AXIS));
        Arrays.stream(elements).forEach(container::add);
        return container;
    }

    public static JComponent createVerticalPanel(final JComponent... elements) {
        final JPanel container = new JPanel();
        container.setLayout(new BoxLayout(container, BoxLayout.Y_AXIS));
        Arrays.stream(elements).forEach(container::add);
        return container;
    }
}
