package week34;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import framework.Channel;
import framework.IllegalReceiveException;
import framework.Message;

public abstract class LaiYangProcess extends SnapshotProcess {

    private Map<Channel, Integer> receivedPiggyBackFalseNs = new HashMap<>();
    private Map<Channel, Integer> shouldReceivePiggyBackFalseNs = new HashMap<>();
    private List<Message> messages = new ArrayList<>();
    private Set<Channel> receivedControlMessages = new HashSet<>();

    @Override
    public void init() {
    }

    @Override
    public void receive(Message m, Channel c) throws IllegalReceiveException {
        if (m instanceof LaiYangControlMessage) {
            handleControlMessage((LaiYangControlMessage) m, c);
        } else if (m instanceof LaiYangBasicMessage) {
            handleBasicMessage((LaiYangBasicMessage) m, c);
        } else {
            throw new IllegalReceiveException();
        }
    }

    private void handleControlMessage(LaiYangControlMessage m, Channel c) throws IllegalReceiveException {
        if (hasFinished()) {
            throw new IllegalReceiveException();
        }
        if (receivedControlMessages.contains(c)) {
            throw new IllegalReceiveException();
        }
        receivedControlMessages.add(c);
        shouldReceivePiggyBackFalseNs.put(c, m.getN());
        if (!hasStarted()) {
            startSnapshot();
            sendControlMessageOutgoingChannels();
        }
        couldTerminate(c);
    }

    private void handleBasicMessage(LaiYangBasicMessage m, Channel c) throws IllegalReceiveException {
        if (hasFinished()) {
            throw new IllegalReceiveException();
        }
        if (m.getTag() && !hasStarted()) {
            startSnapshot();
            sendControlMessageOutgoingChannels();
        } else if (!m.getTag()) {
            receivedPiggyBackFalseNs.put(c, receivedPiggyBackFalseNs.getOrDefault(c, 0) + 1);
            messages.add(m);
        }
        couldTerminate(c);
    }

    private void couldTerminate(Channel c) {
        boolean receivedAllControlMessages = receivedControlMessages.containsAll(getIncoming());
        boolean receivedAllPiggyBackFalseNs = true;
        for (Channel channel : getIncoming()) {
            if (shouldReceivePiggyBackFalseNs.getOrDefault(channel, 0) > receivedPiggyBackFalseNs.getOrDefault(channel, 0)) {
                receivedAllPiggyBackFalseNs = false;
            }
        }
        if (receivedAllControlMessages && receivedAllPiggyBackFalseNs) {
            record(c, messages);
            messages.clear();
            finishSnapshot();
        }
    }

    protected void sendControlMessageOutgoingChannels() {
        for (Channel c : getOutgoing()) {
            send(new LaiYangControlMessage(receivedPiggyBackFalseNs.getOrDefault(c, 0)), c);
        }
    }
}
