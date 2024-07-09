package week78;

import framework.Channel;
import framework.IllegalReceiveException;
import framework.Message;

public class BrachaTouegNonInitiator extends BrachaTouegProcess {

    @Override
    public void init() {
        super.init();
    }

    @Override
    public void receive(Message m, Channel c) throws IllegalReceiveException {
        super.receive(m, c);
    }
}
