package strobeyworks.ui.style;

import java.util.Objects;

import strobeyworks.utils.Vec4;

public class UIStyleProperty<T> {
    
    private final Class<T> type;
    private final String name;

    public UIStyleProperty(String name, Class<T> type) {
        this.name = name;
        this.type = type;
    }

    public String name() {
        return this.name;
    }

    public Class<T> type() {
        return this.type;
    }

    public boolean isTransitionable() {
        return type==Float.class || type==Vec4.class;
    }

    @Override
    public boolean equals(Object obj) {
        if (this==obj) return true;
        if (!(obj instanceof UIStyleProperty<?> other)) return false;

        return name.equals(other.name) && type.equals(other.type);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, type);
    }
}
