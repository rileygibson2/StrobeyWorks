package strobeyworks.ui.components.input.field;

import strobeyworks.ui.components.input.UIValueAdaptor;

public interface UIFieldRule<E> extends UIValueAdaptor<E, String> {
    
    public static UIFloatFieldRule defaultFloat() {return new UIFloatFieldRule();}
    public static UIStringFieldRule defaultString() {return new UIStringFieldRule();}

    boolean inputFilter(String s);
    boolean acceptsChar(char c);
}