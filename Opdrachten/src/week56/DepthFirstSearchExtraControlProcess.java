package week56;

import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;

import framework.Channel;
import framework.IllegalReceiveException;
import framework.Message;
import framework.Process;
import framework.SetChannel;

public abstract class DepthFirstSearchExtraControlProcess extends WaveProcess {

    private Process parent;
    protected TokenMessage tokenMessage;
    private Set<Channel> sendTokenInChannels = new LinkedHashSet<Channel>();
    private Set<Process> receivedInfoMessages = new LinkedHashSet<Process>();
    private Map<Process, Boolean> receivedAckMessages = new HashMap<Process, Boolean>();

    @Override
    public void init() {
    }

    @Override
    public void receive(Message m, Channel c) throws IllegalReceiveException {
        if (!(m instanceof TokenMessage || m instanceof InfoMessage || m instanceof AckMessage)) {
            throw new IllegalReceiveException();
        }
        if (isPassive()) {
            throw new IllegalReceiveException();
        }
        if (m instanceof TokenMessage && getParent() == null) {
            tokenMessage = (TokenMessage) m;
            setParent(c.getSender());
            sendInfoMessages();
        }
        if (m instanceof InfoMessage) {
            receiveInfoMessage(c);
        }
        if (m instanceof AckMessage) {
            receiveAckMessage(c);
        }
        if (receivedAckMessages.values().stream().allMatch(Boolean::booleanValue)) {
            sendToken();
        }
    }

    protected void sendInfoMessages() {
        getOutgoing().stream()
                .filter(c -> c != futureChild() && c.getReceiver() != getParent())
                .forEach(c -> {
                    send(new InfoMessage(), c);
                    receivedAckMessages.put(c.getReceiver(), false);
                });
        if (receivedAckMessages.isEmpty()) {
            sendToken();
        }
    }

    private Channel futureChild() {
        return getOutgoing().stream()
                .filter(c -> c.getReceiver() != getParent() &&
                        !sendTokenInChannels.contains(c) &&
                        !receivedInfoMessages.contains(c.getReceiver()))
                .findFirst().orElse(null);
    }

    private void receiveInfoMessage(Channel c) {
        receivedInfoMessages.add(c.getSender());
    }

    private void receiveAckMessage(Channel c) {
        receivedAckMessages.put(c.getSender(), true);
    }

    private void sendToken() {
        if (futureChild() != null) {
            send(tokenMessage, futureChild());
            sendTokenInChannels.add(futureChild());
        } else if (this == getParent()) {
            done();
        } else {
            send(tokenMessage, new SetChannel(this, getParent()));
            sendTokenInChannels.add(new SetChannel(this, getParent()));
            done();
        }
    }

    protected void setParent(Process parent) {
        this.parent = parent;
    }

    protected Process getParent() {
        return parent;
    }
}
