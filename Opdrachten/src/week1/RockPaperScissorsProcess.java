package week1;

import java.util.LinkedHashSet;
import java.util.Set;

import framework.Channel;
import framework.IllegalReceiveException;
import framework.Message;
import framework.Process;

public class RockPaperScissorsProcess extends Process {

    private Item item;
    private Set<Process> otherProcesses = new LinkedHashSet<>();
    private boolean win;
    private boolean loose;

    @Override
    public void init() {
        this.item = Item.random();
        RockPaperScissorsMessage m = new RockPaperScissorsMessage(this.item);
        for (Channel c : this.getOutgoing()) {
            this.send(m, c);
            this.otherProcesses.add(c.getReceiver());
        }
    }

    @Override
    public void receive(Message m, Channel c) throws IllegalReceiveException {
        if (!(m instanceof RockPaperScissorsMessage)) {
            throw new IllegalReceiveException();
        }
        if (!this.otherProcesses.contains(c.getSender())) {
            throw new IllegalReceiveException();
        }
        RockPaperScissorsMessage message = (RockPaperScissorsMessage) m;
        Item opponentItem = message.getItem();
        win = win || this.item.beats(opponentItem);
        loose = loose || opponentItem.beats(this.item);
        this.otherProcesses.remove(c.getSender());
        if (this.otherProcesses.isEmpty()){
            this.print(win + " " + loose);
        }
    }
}
