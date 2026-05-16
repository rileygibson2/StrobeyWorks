package strobeyworks.ui.style;

import java.util.Objects;

import strobeyworks.utils.Vec4;

public class UIStyleProperty<T> {
    
    private final Class<T> valueType;
    private final String name;

    public UIStyleProperty(String name, Class<T> valueType) {
        this.name = name;
        this.valueType = valueType;
    }

    public String getName() {
        return this.name;
    }

    public Class<T> getValueType() {
        return this.valueType;
    }

    public boolean isTransitionable() {
        return valueType==Float.class || valueType==Vec4.class;
    }

    @Override
    public boolean equals(Object obj) {
        if (this==obj) return true;
        if (!(obj instanceof UIStyleProperty<?> other)) return false;

        return name.equals(other.name) && valueType.equals(other.valueType);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, valueType);
    }
}
