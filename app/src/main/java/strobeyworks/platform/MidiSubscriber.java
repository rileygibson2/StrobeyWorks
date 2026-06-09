package strobeyworks.platform;

import strobeyworks.platform.MidiManager.MidiEvent;

public interface MidiSubscriber {

    public void receiveMidiEvent(MidiEvent event);
    
}
