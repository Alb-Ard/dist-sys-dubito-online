package org.albard.mvc;

import java.util.function.Consumer;

public final class ExecutableViewCommand<X> extends ViewCommandBase<X> {
    public void execute(final Consumer<X> invoker) {
        this.getListeners().forEach(invoker);
    }
}