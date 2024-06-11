package week56;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import framework.Channel;
import framework.IllegalReceiveException;
import framework.Message;
import framework.Process;

public abstract class TarryProcess extends WaveProcess {

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
        sendTokenInChannels(m);
    }

    protected void sendTokenInChannels(Message m) {
        for(Channel c : getOutgoing()) {
            if(!sendTokenInChannels.contains(c) && c.getReceiver() != getParent()) {
                send(m, c);
                sendTokenInChannels.add(c);
                return;
            }
        }
        if (sendTokenInChannels.containsAll(getOutgoing())) {
            done();
        }
        List<Channel> parent =
                getOutgoing().stream().filter(c -> c.getReceiver() == getParent()).collect(Collectors.toList());
        if (parent.size() > 0) {
            send(m, parent.iterator().next());
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
