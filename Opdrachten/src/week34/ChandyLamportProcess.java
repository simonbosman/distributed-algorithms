package week34;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import framework.Channel;
import framework.IllegalReceiveException;
import framework.Message;

public abstract class ChandyLamportProcess extends SnapshotProcess {

    List<Message> messages = new ArrayList<>();
    Set<Channel> receivedMarkers = new HashSet<>();

    @Override
    public void init() {
    }

    @Override
    public void receive(Message m, Channel c) throws IllegalReceiveException {
        if (m instanceof ChandyLamportControlMessage) {
            handleControlMessage(c);
        } else if (m instanceof ChandyLamportBasicMessage && hasStarted() && !hasFinished()) {
            messages.add(m);
        } else {
            throw new IllegalReceiveException();
        }
    }

    private void handleControlMessage(Channel c) throws IllegalReceiveException {
        if (hasFinished()) {
            throw new IllegalReceiveException();
        } else if (!hasStarted()) {
            startSnapshot();
            sendMarkersToOutgoingChannels();
        } else {
            record(c, messages);
            messages.clear();
            terimate(c);
        }
    }

    protected void terimate(Channel c) throws IllegalReceiveException {
        if (receivedMarkers.contains(c)) {
            throw new IllegalReceiveException();
        }
        receivedMarkers.add(c);
        if (receivedMarkers.containsAll(getIncoming())) {
            finishSnapshot();
        }
    }

    protected void sendMarkersToOutgoingChannels() {
        for (Channel c2 : getOutgoing()) {
            send(new ChandyLamportControlMessage(), c2);
        }
    }
}
