package strobeyworks.ui.components.input;

import static strobeyworks.ui.core.UILength.pch;
import static strobeyworks.ui.core.UILength.pcw;
import static strobeyworks.ui.core.UILength.px;

import strobeyworks.platform.IOEvent;
import strobeyworks.ui.core.UIColor;
import strobeyworks.ui.core.UIFont;
import strobeyworks.ui.primitives.UIIcon;
import strobeyworks.ui.primitives.UIRectFactory;
import strobeyworks.ui.primitives.UIRectangle;
import strobeyworks.ui.primitives.UIText;

public class UIDropDown extends UIBindableInput<Integer, String> {
    
    private static class SelectValueMapper implements UIValueMapper<Integer, String> {
        private final String[] options;
        
        public SelectValueMapper(String[] options) {
            this.options = options;
        }
        
        @Override
        public UIMapResult<String> mapExternalToLocal(Integer value) {
            if (value==null||value<0||value>=options.length) {
                return UIMapResult.success(options.length>0 ? options[0] : "");
            }
            
            return UIMapResult.success(options[value]);
        }
        
        @Override
        public UIMapResult<Integer> mapLocalToExternal(String value) {
            for (int i=0; i<options.length; i++) {
                if (options[i].equals(value)) {
                    return UIMapResult.success(i);
                }
            }
            
            return UIMapResult.failure();
        }
    }
    
    private UIText label;
    
    public UIDropDown(UIFont font, String[] options) {
        super(new SelectValueMapper(options));
        
        clickable(true);
        
        style("box", UIBoxMode.FIXED);
        style("flow-direction", UIFlowDirection.ROW);
        style("align-items", UIAlignItems.CENTER);
        style("color", UIColor.rgb(0.3f));
        style("padding-left", px(10));
        style("padding-right", px(3));
        style("padding-top", px(3));
        style("padding-bottom", px(3));
        
        UIRectangle labelBox = new UIRectangle();
        labelBox.style("width", px(1))
        .style("height", pch(1f))
        .style("grow", 1f)
        .style("overflow-x", UIOverflowMode.HIDDEN)
        .style("align-items", UIAlignItems.CENTER)
        .style("color", UIColor.transparent());
        
        label = new UIText(font, "");
        label.style("color", UIColor.rgb(0.7f));

        UIRectangle iconBox = new UIRectangle();
        iconBox.style("width", px(12))
        .style("height", pch(1f))
        .style("flow-direction", UIFlowDirection.COLUMN)
        .style("color", UIColor.transparent());
        
        UIIcon icon = new UIIcon("dropdown_arrow.png");
        icon.style("width", px(12))
        .style("height", px(12))
        .style("icon-fit-mode", UIIcon.UIIconFitMode.FIT)
        .style("tint", UIColor.rgb(0.7f));
        
        labelBox.addChild(label);
        iconBox.addChild(UIRectFactory.colGrow(1f));
        iconBox.addChild(icon);
        addChild(labelBox);
        addChild(iconBox);
    }
    
    @Override
    protected String getDefaultLocalValue() {
        if (getMapper() instanceof SelectValueMapper s) return s.options.length>0 ? s.options[0] : "";
        return "";
    }
    
    @Override
    protected void implementLocalValueOnUI() {
        label.setText(getLocalValue());
    }
    
    @Override
    public void clicked(IOEvent event) {
        super.clicked(event);
        
        String[] options = null;
        if (getMapper() instanceof SelectValueMapper s) options = s.options;
        else return;
        
        if (options==null||options.length==0) return;
        int index = 0;
        for (int i=0; i<options.length; i++) {
            if (options[i].equals(getLocalValue())) {
                index = i;
                break;
            }
        }
        
        int next = (index+1)%options.length; // Wrap
        setLocalValue(options[next]);
        commitLocalValue();
    }
}
