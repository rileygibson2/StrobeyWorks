package strobeyworks.ui.components.interactable.input;

import strobeyworks.ui.components.interactable.UIValueAdaptor;

public interface UIInputRule<E> extends UIValueAdaptor<E, String> {
    
    boolean draftValid(String s);
    boolean acceptsChar(char c);
}