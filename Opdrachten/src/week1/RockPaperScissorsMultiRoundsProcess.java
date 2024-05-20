package week1;

import framework.Channel;
import framework.IllegalReceiveException;
import framework.Message;
import framework.Process;

import java.util.LinkedHashSet;
import java.util.Set;

/**
 * This class represents a process for playing multiple rounds of Rock-Paper-Scissors.
 * Each process communicates with other processes through channels and determines winners and losers.
 */
public class RockPaperScissorsMultiRoundsProcess extends Process {

    private Item item; // Current round item
    private Item itemNextRound; // Next round item
    private Set<Process> otherProcesses = new LinkedHashSet<>(); // Set of other processes for the current round
    private Set<Process> otherProcessesNextRound = new LinkedHashSet<>(); // Set of other processes for the next round
    private boolean win; // Win status for the current round
    private boolean loose; // Lose status for the current round
    private boolean winNextRound; // Win status for the next round
    private boolean looseNextRound; // Lose status for the next round
    private boolean isLooser; // Final loser status
    private boolean isWinner; // Final winner status

    /**
     * Initializes the process for a new round of Rock-Paper-Scissors.
     */
    @Override
    public void init() {
        win = false;
        loose = false;

        // Initialize the set of other processes for the first run
        if (otherProcessesNextRound.isEmpty()) {
            for (Channel c : this.getOutgoing()) {
                otherProcessesNextRound.add(c.getReceiver());
            }
        }

        // Copy next round state to the current round
        otherProcesses.clear();
        otherProcesses.addAll(otherProcessesNextRound);
        win = winNextRound;
        loose = looseNextRound;

        // Set the current item to the next round item or generate a new random item
        item = (itemNextRound != null) ? itemNextRound : Item.random();

        // Send the current item to all outgoing channels
        RockPaperScissorsMessage m = new RockPaperScissorsMessage(item);
        for (Channel c : this.getOutgoing()) {
            this.send(m, c);
        }

        // Reset the state for the next round
        itemNextRound = null;
        winNextRound = false;
        looseNextRound = false;
        otherProcessesNextRound.clear();
        for (Channel c : this.getOutgoing()) {
            otherProcessesNextRound.add(c.getReceiver());
        }
    }

    /**
     * Receives a message from a channel and processes it.
     * @param m The message received
     * @param c The channel from which the message is received
     * @throws IllegalReceiveException If the message is not valid for the process
     */
    @Override
    public void receive(Message m, Channel c) throws IllegalReceiveException {
        // If the process has already won or lost, no further action is needed
        if (isWinner) {
            return;
        }
        if (isLooser) {
            // Forward the message to the original sender
            for (Channel c2 : this.getOutgoing()) {
                if (c2.getReceiver().equals(c.getSender())) {
                    send(m, c2);
                }
            }
            return;
        }

        // Validate the received message
        if (!(m instanceof RockPaperScissorsMessage)) {
            throw new IllegalReceiveException();
        }
        if (!this.otherProcesses.contains(c.getSender()) && !this.otherProcessesNextRound.contains(c.getSender())) {
            throw new IllegalReceiveException();
        }

        RockPaperScissorsMessage message = (RockPaperScissorsMessage) m;
        Item opponentItem = message.getItem();

        // Process the current or next round item based on the sender
        if (otherProcesses.contains(c.getSender())) {
            playCurrent(opponentItem, c);
        } else if (otherProcessesNextRound.contains(c.getSender())) {
            if (itemNextRound == null) {
                itemNextRound = Item.random();
            }
            playNextRound(opponentItem, c);
        }

        // If all processes have been handled, determine the outcome
        if (otherProcesses.isEmpty()) {
            if (win && loose) {
                init();
            } else if (win) {
                isWinner = true;
                print("true");
            } else if (loose) {
                isLooser = true;
                print("false");
            } else {
                init();
            }
        }
    }

    /**
     * Processes the current round with the opponent's item.
     * @param i The opponent's item
     * @param c The channel from which the message is received
     */
    private void playCurrent(Item i, Channel c) {
        win = win || item.beats(i);
        loose = loose || i.beats(item);
        otherProcesses.remove(c.getSender());
    }

    /**
     * Processes the next round with the opponent's item.
     * @param i The opponent's item
     * @param c The channel from which the message is received
     */
    private void playNextRound(Item i, Channel c) {
        winNextRound = winNextRound || itemNextRound.beats(i);
        looseNextRound = looseNextRound || i.beats(itemNextRound);
        otherProcessesNextRound.remove(c.getSender());
    }
}
