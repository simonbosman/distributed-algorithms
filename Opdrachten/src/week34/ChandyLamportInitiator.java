package week34;

import framework.Channel;
import framework.IllegalReceiveException;
import framework.Message;

public class ChandyLamportInitiator extends ChandyLamportProcess {

    @Override
    public void init() {
        super.init();
        startSnapshot();
        sendMarkersToOutgoingChannels();
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
        record(c, messages);
        messages.clear();
        terimate(c);
    }
}
