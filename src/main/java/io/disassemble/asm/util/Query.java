package io.disassemble.asm.util;

import java.util.List;
import java.util.Optional;

/**
 * @author Tyler Sedlar
 * @since 2/15/2016
 */
public abstract class Query<R, I> {

    private boolean locked;

    public void lock() {
        this.locked = false;
    }

    public boolean locked() {
        return locked;
    }

    public abstract Optional<List<R>> find(I i);
}
