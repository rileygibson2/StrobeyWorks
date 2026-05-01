package strobeyworks.ui;

import java.util.ArrayList;
import java.util.List;

import strobeyworks.ui.primitives.UIElement;
import strobeyworks.ui.primitives.UIPair;
import strobeyworks.ui.primitives.UIRectangle;

public class UIPane extends UIRectangle {

    private List<UIElement> elements;

    public UIPane(UIPair x, UIPair y, UIPair width, UIPair height) {
        super(x, y, width, height);
        elements = new ArrayList<>();
    }

    public void addElement(UIElement e) {
        elements.add(e);
        e.setParent(this);
    }

    public void removeElement(UIElement e) {
        elements.remove(e);
        e.setParent(null);
    }

    public List<UIElement> getElements() {
        List<UIElement> e = new ArrayList<>();
        e.addAll(elements);
        return e;
    }
}
