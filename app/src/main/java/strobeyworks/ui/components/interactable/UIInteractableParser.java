package strobeyworks.ui.components.interactable;

public interface UIInteractableParser<B, L> {

    public L parseBoundToLocal(B boundValue);
    public B parseLocalToBound(L localValue);
}
