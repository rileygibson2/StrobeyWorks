package strobeyworks.ui.style;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;

public class UIStyle {
    
    Map<UIStyleProperty<?>, Object> properties;
    
    public UIStyle() {
        properties = new HashMap<>();
    }
    
    public <T> UIStyle set(UIStyleProperty<T> property, T value) {
        if (!property.type().isInstance(value)) {
            throw new IllegalArgumentException("Invalid type for "+property.name());
        }
        properties.put(property, value);
        return this;
    }

    public UIStyle setRaw(UIStyleProperty<?> property, Object value) {
        if (!property.type().isInstance(value)) {
            throw new IllegalArgumentException("Invalid type for "+property.name());
        }
        properties.put(property, value);
        return this;
    }
    
    public <T> T get(UIStyleProperty<T> property) {
        Object value = properties.get(property);
        if (value==null) return null;
        
        return property.type().cast(value);
    }
    
    public <T> void ifPresent(UIStyleProperty<T> property, Consumer<T> applier) {
        T value = get(property);
        if (value!=null) applier.accept(value);
    }
    
    public <T> boolean has(UIStyleProperty<T> property) {
        return properties.containsKey(property);
    }
    
    public <T> boolean has(String property) {
        for (Map.Entry<UIStyleProperty<?>, Object> entry : properties.entrySet()) {
            if (entry.getKey().name().equals(property)) return true;
        }
        return false;
    }

    public Set<UIStyleProperty<?>> properties() {
        Set<UIStyleProperty<?>> p = new HashSet<>();
        for (Map.Entry<UIStyleProperty<?>, Object> entry : properties.entrySet()) p.add(entry.getKey());
        return p;
    }
}
