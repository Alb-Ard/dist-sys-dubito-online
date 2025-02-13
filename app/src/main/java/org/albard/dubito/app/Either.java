package org.albard.dubito.app;

import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;

public final class Either<X, Y> {
    private final Optional<X> x;
    private final Optional<Y> y;

    private Either(Optional<X> x, Optional<Y> y) {
        this.x = x;
        this.y = y;
    }

    public void match(final Consumer<X> ifX, final Consumer<Y> ifY) {
        this.x.ifPresent(ifX);
        this.y.ifPresent(ifY);
    }

    public <Z> Z map(final Function<X, Z> xMapper, final Function<Y, Z> yMapper) {
        if (this.x.isPresent()) {
            return xMapper.apply(this.x.get());
        } else {
            return yMapper.apply(this.y.get());
        }
    }

    public Optional<X> getX() {
        return this.x;
    }

    public Optional<Y> getY() {
        return this.y;
    }

    public static <X, Y> Either<X, Y> ofX(final X value) {
        return new Either<>(Optional.of(value), Optional.empty());
    }

    public static <X, Y> Either<X, Y> ofY(final Y value) {
        return new Either<>(Optional.empty(), Optional.of(value));
    }
}
