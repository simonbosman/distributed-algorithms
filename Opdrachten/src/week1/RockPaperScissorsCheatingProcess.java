package week1;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import framework.Channel;
import framework.IllegalReceiveException;
import framework.Message;
import framework.Process;

public class RockPaperScissorsCheatingProcess extends Process {

    private Item item;
    private List<Item> items = new ArrayList<>();
    private Set<Process> otherProcesses = new LinkedHashSet<>();
    private boolean win;
    private boolean loose;

    @Override
    public void init() {
        for (Channel c : this.getOutgoing()) {
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

        this.items.add(opponentItem);
        this.otherProcesses.remove(c.getSender());
        if (this.otherProcesses.isEmpty()) {
            this.item = items.iterator().next().getBeatingItem();
            RockPaperScissorsMessage m2 = new RockPaperScissorsMessage(this.item);
            for (Channel c2 : this.getOutgoing()) {
                this.send(m2, c2);
            }
            for (Item i : this.items) {
                win = win || this.item.beats(i);
                loose = loose || i.beats(this.item);
            }
            this.print(win + " " + loose);
        }
    }
}
