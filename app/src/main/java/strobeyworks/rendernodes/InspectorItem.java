package strobeyworks.rendernodes;

import java.util.ArrayList;
import java.util.List;

import strobeyworks.rendernodes.configs.ControlConfig;

public sealed interface InspectorItem permits InspectorItem.InspectorControl, InspectorItem.InspectorGroup, InspectorItem.InspectorTab {
    
    public record InspectorTab(String name, List<InspectorGroup> items) implements InspectorItem {
        public InspectorTab(String name) {
            this(name, new ArrayList<>());
        }

        public void add(InspectorGroup group) {
            items.add(group);
        }
    }
    
    public record InspectorGroup(String name, List<InspectorItem> items) implements InspectorItem {
        public InspectorGroup(String name) {
            this(name, new ArrayList<>());
        }

        public void add(InspectorControl control) {
            items.add(control);
        }
    }

    public record InspectorControl(ControlConfig config) implements InspectorItem {}
}
