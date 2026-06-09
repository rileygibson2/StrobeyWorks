package strobeyworks.platform;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;

import javax.sound.midi.MidiDevice;
import javax.sound.midi.MidiMessage;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.MidiUnavailableException;
import javax.sound.midi.Receiver;
import javax.sound.midi.ShortMessage;
import javax.sound.midi.Transmitter;

import strobeyworks.logger.LogColor;
import strobeyworks.logger.LogColorEnum;
import strobeyworks.logger.Logger;

@LogColor(LogColorEnum.CYAN)
public class MidiManager {
    
    private static MidiManager instance;
    
    private final List<MidiDevice> openDevices;
    private final List<Transmitter> openTransmitters;
    private Receiver receiver;
    private boolean started;
    
    public enum MidiHandleType {
        FADER,
        BUTTON
    }
    public enum MidiType {
        NOTE_ON,
        NOTE_OFF
    }
    public record MidiHandle(MidiHandleType type, int id, int channel, int note) {}
    public record MidiEvent(MidiHandle handle, float value, MidiType noteType) {}
    
    private List<MidiHandle> handles;
    
    private final ConcurrentLinkedQueue<MidiEvent> pendingEvents;
    private HashMap<MidiHandle, Set<MidiSubscriber>> subscribers;
    
    private boolean midiDebug = false;
    
    public static MidiManager getInstance() {
        if (instance==null) instance = new MidiManager();
        return instance;
    }
    
    private MidiManager() {
        openDevices = new ArrayList<>();
        openTransmitters = new ArrayList<>();
        pendingEvents = new ConcurrentLinkedQueue<>();
        
        handles = new ArrayList<>();
        subscribers = new HashMap<>();
        
        loadDefaultConfig();
    }
    
    public void loadDefaultConfig() {
        addFader(1, 4, 15);
        addFader(2, 4, 16);
        addFader(3, 4, 17);
        addFader(4, 4, 18);
        addFader(5, 4, 19);
        
        addButton(1, 4, 10);
        addButton(2, 4, 11);
        addButton(3, 4, 12);
        addButton(4, 4, 13);
        addButton(5, 4, 14);
        
        addButton(6, 4, 20);
        addButton(7, 4, 21);
        addButton(8, 4, 22);
        addButton(9, 4, 23);
        addButton(10, 4, 24);
        
        addButton(11, 4, 25);
        addButton(12, 4, 26);
        addButton(13, 4, 27);
        addButton(14, 4, 28);
        addButton(15, 4, 29);
    }
    
    public void addFader(int id, int channel, int note) {
        handles.add(new MidiHandle(MidiHandleType.FADER, id, channel, note));
    }
    
    public void addButton(int id, int channel, int note) {
        handles.add(new MidiHandle(MidiHandleType.BUTTON, id, channel, note));
    }
    
    public MidiHandle getHandle(MidiHandleType type, int id) {
        for (MidiHandle h : handles) {
            if (h.type==type&&h.id==id) return h;
        }
        return null;
    }
    
    private Set<MidiHandle> getHandle(int channel, int note) {
        Set<MidiHandle> found = new HashSet<>();
        for (MidiHandle h : handles) {
            if (h.channel==channel&&h.note==note) found.add(h);
        }
        return found;
    }
    
    public void subscribe(MidiSubscriber subscriber, MidiHandle handle) {
        if (!subscribers.containsKey(handle)) subscribers.put(handle, new HashSet<>());
        subscribers.get(handle).add(subscriber);
    }
    
    public void unsubscribe(MidiSubscriber subscriber, MidiHandle handle) {
        Set<MidiSubscriber> f = subscribers.get(handle);
        if (f==null) return;
        f.remove(subscriber);
    }
    
    public void unsubscribeAll(MidiSubscriber subscriber) {
        for (Map.Entry<MidiHandle, Set<MidiSubscriber>> entry : subscribers.entrySet()) {
            entry.getValue().remove(subscriber);
        }
    }
    
    public void update() {
        MidiEvent event;
        while ((event = pendingEvents.poll()) != null) {
            Set<MidiSubscriber> set = subscribers.get(event.handle());
            if (set==null) return;
            
            for (MidiSubscriber s : new HashSet<>(set)) {
                s.receiveMidiEvent(event);
            }
        }
    }
    
    private void recieveMidi(MidiMessage message) {
        if (!(message instanceof ShortMessage sm)) return;
        
        int channel = sm.getChannel();
        int data1 = sm.getData1();
        int data2 = sm.getData2();
        float normalized = data2 / 127f;
        
        if (sm.getCommand() == ShortMessage.NOTE_ON) {
            if (midiDebug) Logger.debug("Note On: ch"+channel+" n"+data1+" v"+data2+" - "+normalized);
            
            Set<MidiHandle> handles = getHandle(channel, data1);
            for (MidiHandle h : handles) pendingEvents.add(new MidiEvent(h, normalized, MidiType.NOTE_ON));
        }
        else if (sm.getCommand() == ShortMessage.NOTE_OFF) {
            if (midiDebug) Logger.debug("Note Off: ch"+channel+" n"+data1+" v"+data2+" - "+normalized);
            
            Set<MidiHandle> handles = getHandle(channel, data1);
            for (MidiHandle h : handles) pendingEvents.add(new MidiEvent(h, normalized, MidiType.NOTE_OFF));
        }
    }
    
    public void listAllMidiDevices() {
        try {
            for (MidiDevice.Info info : MidiSystem.getMidiDeviceInfo()) {
                MidiDevice device = MidiSystem.getMidiDevice(info);
                
                //if (device.getMaxTransmitters() != 0) {
                Logger.debug(
                    "MIDI input: " + info.getName() +
                    " - " + info.getDescription() +
                    " : Max Transmitters = " + device.getMaxTransmitters() +
                    " : Max Recievers = " + device.getMaxReceivers()
                );
                //}
            }
        }
        catch (MidiUnavailableException e) {Logger.error(e.getMessage());}
    }
    
    public void open(String deviceNameContains) {
        if (started) return;
        
        String target = deviceNameContains.toLowerCase();
        
        // Create reciever
        receiver = new Receiver() {
            @Override
            public void send(MidiMessage message, long timeStamp) {
                recieveMidi(message);
            }
            
            @Override
            public void close() {}
        };
        
        MidiDevice.Info[] infos = MidiSystem.getMidiDeviceInfo();
        
        for (MidiDevice.Info info : infos) {
            String name = info.getName().toLowerCase();
            
            if (!name.contains(target)) continue;
            
            MidiDevice device = null;
            try {device = MidiSystem.getMidiDevice(info);} 
            catch (MidiUnavailableException e) {
                Logger.error(e.getMessage());
                return;
            }
            
            if (device.getMaxTransmitters() == 0) continue;
            
            try {
                device.open();
                
                Transmitter transmitter = device.getTransmitter();
                transmitter.setReceiver(receiver);
                
                openDevices.add(device);
                openTransmitters.add(transmitter);
                
                Logger.info("Opened MIDI input device: " + info.getName());
                
                started = true;
                return;
            } catch (Exception e) {
                Logger.error("Failed to open MIDI input device '" + info.getName() + "': " + e.getMessage());
                if (device.isOpen()) device.close();
            }
        }
        
        Logger.debug("No matching MIDI input device opened: " + deviceNameContains);
        cleanup();
    }
    
    public void cleanup() {
        Logger.info("Cleaning up");
        
        for (Transmitter transmitter : openTransmitters) {
            try {
                transmitter.setReceiver(null);
                transmitter.close();
            } catch (Exception e) {
                Logger.error("Failed to close MIDI transmitter: " + e.getMessage());
            }
        }
        openTransmitters.clear();
        
        if (receiver != null) {
            try {receiver.close();}
            catch (Exception e) {
                Logger.error("Failed to close MIDI receiver: " + e.getMessage());
            }
            receiver = null;
        }
        
        for (MidiDevice device : openDevices) {
            try {
                for (Transmitter transmitter : device.getTransmitters()) {
                    transmitter.setReceiver(null);
                    transmitter.close();
                }
                
                if (device.isOpen()) {
                    device.close();
                }
            } catch (Exception e) {
                Logger.error("Failed to close MIDI device '" + device.getDeviceInfo().getName() + "': " + e.getMessage());
            }
        }
        openDevices.clear();
        
        started = false;
    }
}
