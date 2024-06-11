package week56;

import framework.Channel;
import framework.IllegalReceiveException;
import framework.Message;

public class DepthFirstSearchInitiator extends DepthFirstSearchProcess {

    @Override
    public void init() {
        super.init();
        setParent(this);
        sendTokenInChannels(new TokenMessage(), getOutgoing().iterator().next());
    }

    @Override
    public void receive(Message m, Channel c) throws IllegalReceiveException {
        super.receive(m, c);
    }
}
