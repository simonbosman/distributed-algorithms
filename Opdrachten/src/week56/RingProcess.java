package week56;

import java.util.HashSet;
import java.util.Set;

import framework.Channel;
import framework.IllegalReceiveException;
import framework.Message;
import framework.Process;

public abstract class RingProcess extends WaveProcess {

    private Process parent;

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
            send(m, getOutgoing().iterator().next());
            done();
        } else if (getParent() != this || isPassive()) {
            throw new IllegalReceiveException();
        } else {
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
