package strobeyworks.pipeline.controls;

import java.util.ArrayList;
import java.util.List;

public abstract class ControlItem {

    String name;

    public ControlItem(String name) {
        this.name = name;
    }

    public String getName() {
        return this.name;
    }
    
    public static class ControlTab extends ControlItem {
        List<ControlGroup> items;

        public ControlTab(String name) {
            super(name);
            items = new ArrayList<>();
        }

        public void add(ControlGroup group) {
            items.add(group);
            group.setParent(this);
        }

        public List<ControlGroup> getItems() {
            return List.copyOf(items);
        }
    }

    public static class ControlGroup extends ControlItem {
        List<ControlElement> items;
        ControlTab parent;

        public ControlGroup(String name) {
            super(name);
            items = new ArrayList<>();
        }

        public void add(ControlElement element) {
            items.add(element);
            element.setParent(this);
        }

        public void setParent(ControlTab tab) {
            this.parent = tab;
        }

        public ControlTab getTab() {
            return this.parent;
        }

        public List<ControlElement> getItems() {
            return List.copyOf(items);
        }
    }
}
