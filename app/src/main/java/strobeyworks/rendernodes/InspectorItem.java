package strobeyworks.rendernodes;

import java.util.List;

public sealed interface InspectorItem permits InspectorItem.InspectorControl, InspectorItem.InspectorGroup {
    
    record InspectorControl(ControlConfig<?> config) implements InspectorItem {}

    record InspectorGroup(String name, List<InspectorItem> items) implements InspectorItem {}
}
