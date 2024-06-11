package week56;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import framework.Channel;
import framework.IllegalReceiveException;
import framework.Message;
import framework.Process;

public abstract class DepthFirstSearchProcess extends WaveProcess {

    private Process parent;
    private Set<Channel> sendTokenInChannels = new HashSet<Channel>();

    @Override
    public void init() {
    }

    @Override
    public void receive(Message m, Channel c) throws IllegalReceiveException {
        if (!(m instanceof TokenMessage)) {
            throw new IllegalReceiveException();
        }
        if (getParent() == null) {
            setParent(c.getSender());
        }
        if (isPassive()) {
            throw new IllegalReceiveException();
        }
        sendTokenInChannels(m, c);
    }

    protected void sendTokenInChannels(Message m, Channel c) {
        List<Channel> sendBackToChannels = getOutgoing().stream()
                .filter(ch -> ch.getReceiver() == c.getSender())
                .collect(Collectors.toList());

        if (!sendBackToChannels.isEmpty()) {
            Channel sendBackToChannel = sendBackToChannels.iterator().next();
            if (!sendTokenInChannels.contains(sendBackToChannel) && sendBackToChannel.getReceiver() != getParent()) {
                send(m, sendBackToChannel);
                sendTokenInChannels.add(sendBackToChannel);
                return;
            }
        }

        for (Channel c2 : getOutgoing()) {
            if (!sendTokenInChannels.contains(c2) && c2.getReceiver() != getParent()) {
                send(m, c2);
                sendTokenInChannels.add(c2);
                return;
            }
        }

        if (sendTokenInChannels.containsAll(getOutgoing())) {
            done();
        }

        List<Channel> parentChannels = getOutgoing().stream()
                .filter(c2 -> c2.getReceiver() == getParent())
                .collect(Collectors.toList());

        if (!parentChannels.isEmpty()) {
            send(m, parentChannels.iterator().next());
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
