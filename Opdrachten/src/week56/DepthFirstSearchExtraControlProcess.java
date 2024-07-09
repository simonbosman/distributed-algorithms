package week56;

import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import framework.Channel;
import framework.IllegalReceiveException;
import framework.Message;
import framework.Process;

public abstract class DepthFirstSearchExtraControlProcess extends WaveProcess {

    private Process parent;
    private boolean didReceiveMessage;
    private Set<Channel> sendTokenInChannels = new LinkedHashSet<Channel>();
    private Set<Process> receivedInfoMessages = new LinkedHashSet<Process>();
    private Map<Process, Boolean> receivedAckMessages = new HashMap<Process, Boolean>();
    protected TokenMessage tokenMessage;

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
        didReceiveMessage = true;
        if (m instanceof TokenMessage && getParent() == null) {
            tokenMessage = (TokenMessage) m;
            setParent(c.getSender());
            sendInfoMessages();
        }
        if (m instanceof InfoMessage) {
            receiveInfoMessage(c);
            return;
        }
        if (m instanceof AckMessage) {
            receiveAckMessage(c);
        }
        if (receivedAckMessages.values().stream()
                .allMatch(Boolean::booleanValue) &&
                !receivedAckMessages.isEmpty()) {
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

    private Channel parent() {
        return getOutgoing().stream()
                .filter(c -> c.getReceiver() == getParent())
                .findFirst().orElse(null);
    }

    private void receiveInfoMessage(Channel c) {
        receivedInfoMessages.add(c.getSender());
        Channel sendAckInChannel = getOutgoing().stream()
                .filter(c2 -> c2.getReceiver() == c.getSender())
                .findFirst().orElse(null);
        if (sendAckInChannel != null) {
            send(new AckMessage(), sendAckInChannel);
        }
    }

    private void receiveAckMessage(Channel c) throws IllegalReceiveException {
        if (!receivedAckMessages.containsKey(c.getSender())) {
            throw new IllegalReceiveException();
        }
        if (receivedAckMessages.get(c.getSender())) {
            throw new IllegalReceiveException();
        }
        receivedAckMessages.put(c.getSender(), true);
    }

    private void sendToken() {
        if (futureChild() != null) {
            send(tokenMessage, futureChild());
            sendTokenInChannels.add(futureChild());
        } else if (this.equals(getParent()) && didReceiveMessage) {
            done();
        } else if (parent() != null) {
            send(tokenMessage, parent());
            sendTokenInChannels.add(parent());
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
