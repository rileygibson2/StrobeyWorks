package strobeyworks.pipeline;

import java.util.ArrayList;
import java.util.List;

import strobeyworks.pipeline.configs.ControlConfig;

public sealed interface ControlItem permits ControlItem.ControlElement, ControlItem.ControlGroup, ControlItem.ControlTab {
    
    public record ControlTab(String name, List<ControlGroup> items) implements ControlItem {
        public ControlTab(String name) {
            this(name, new ArrayList<>());
        }

        public void add(ControlGroup group) {
            items.add(group);
        }
    }
    
    public record ControlGroup(String name, List<ControlElement> items, ControlTab tab) implements ControlItem {
        public ControlGroup(String name, ControlTab tab) {
            this(name, new ArrayList<>(), tab);
        }

        public void add(ControlElement control) {
            items.add(control);
        }
    }

    public record ControlElement(ControlConfig config, ControlGroup group) implements ControlItem {
        
    }
}
