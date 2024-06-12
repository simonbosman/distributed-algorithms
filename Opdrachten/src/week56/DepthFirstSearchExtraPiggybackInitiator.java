package week56;

import framework.Channel;
import framework.IllegalReceiveException;
import framework.Message;

public class DepthFirstSearchExtraPiggybackInitiator extends DepthFirstSearchExtraPiggybackProcess {

    @Override
    public void init() {
        super.init();
        setParent(this);
        sendTokenInChannels(new TokenWithIdsMessage(getName()));
    }

    @Override
    public void receive(Message m, Channel c) throws IllegalReceiveException {
        super.receive(m, c);
    }
}
