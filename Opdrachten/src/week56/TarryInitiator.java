package week56;

import framework.Channel;
import framework.IllegalReceiveException;
import framework.Message;

public class TarryInitiator extends TarryProcess {

    @Override
    public void init() {
        super.init();
        setParent(this);
        sendTokenInChannels(new TokenMessage());
    }

    @Override
    public void receive(Message m, Channel c) throws IllegalReceiveException {
        super.receive(m, c);
    }
}
