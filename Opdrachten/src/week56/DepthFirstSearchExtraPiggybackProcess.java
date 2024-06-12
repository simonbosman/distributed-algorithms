package week56;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import framework.Channel;
import framework.IllegalReceiveException;
import framework.Message;
import framework.Process;

public abstract class DepthFirstSearchExtraPiggybackProcess extends WaveProcess {

    private Process parent;

    @Override
    public void init() {
    }

    @Override
    public void receive(Message m, Channel c) throws IllegalReceiveException {
        if (!(m instanceof TokenWithIdsMessage)) {
            throw new IllegalReceiveException();
        }
        if (getParent() == null) {
            setParent(c.getSender());
            ((TokenWithIdsMessage) m).addId(getName());
        }
        if (isPassive()) {
            throw new IllegalReceiveException();
        }
        sendTokenInChannels(m);
    }

    protected void sendTokenInChannels(Message m) {

        if (getParent() == this) {
            Set<String> ids = ((TokenWithIdsMessage) m).getIds();
            Set<Channel> channels = getOutgoing();
            Set<String> allProcessNames = channels.stream().map(ch -> ch.getReceiver().getName()).collect(Collectors.toSet());
            allProcessNames.add(getName());

            if (ids.containsAll(allProcessNames)) {
                done();
                return;
            }
        }

        for (Channel c2 : getOutgoing()) {
            if (c2.getReceiver() != getParent() &&
                    !((TokenWithIdsMessage) m).getIds().contains(c2.getReceiver().getName())) {
                send(m, c2);
                return;
            }
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
