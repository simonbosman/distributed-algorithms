package week78;

import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import framework.Channel;
import framework.IllegalReceiveException;
import framework.Message;
import framework.Process;

public class BrachaTouegProcess extends DeadlockDetectionProcess {

    private boolean notified;
    private Process notifiedBy;
    private Process sendDoneTo;
    private boolean free;
    private Set<Channel> sendAckBuffer = new LinkedHashSet<Channel>();
    private Set<Channel> sendDoneBuffer = new LinkedHashSet<Channel>();
    protected Map<Process, Boolean> doneOutRequests = new HashMap<Process, Boolean>();
    private Map<Process, Boolean> ackOutRequests = new HashMap<Process, Boolean>();

    @Override
    public void init() {
    }

    @Override
    public void receive(Message m, Channel c) throws IllegalReceiveException {
        if (!(m instanceof AckMessage || m instanceof DoneMessage || m instanceof GrantMessage
                || m instanceof NotifyMessage)) {
            throw new IllegalReceiveException();
        }
        if (m instanceof AckMessage) {
            receiveAckMessage(c);
        }
        if (m instanceof DoneMessage) {
            receiveDoneMessage(c);
        }
        if (m instanceof NotifyMessage) {
            receiveNotifyMessage(c);
        }
        if (m instanceof GrantMessage) {
            receiveGrantMessage(c);
        }
        couldTerminate();
    }

    protected void notifyOutRequests() {
        notified = true;
        outRequests.stream().forEach(c -> {
            send(new NotifyMessage(), c);
            doneOutRequests.put(c.getReceiver(), false);
        });
        if (requests == 0) {
            grantInRequests();
        }
    }

    private void grantInRequests() {
        free = true;
        inRequests.stream().forEach(c -> {
            Channel sendGrantInChannel = getOutgoing().stream()
                    .filter(c2 -> c2.getReceiver() == c.getSender())
                    .findFirst()
                    .orElse(null);
            if (sendGrantInChannel != null) {
                send(new GrantMessage(), sendGrantInChannel);
            }
            ackOutRequests.put(c.getSender(), false);
        });
    }

    private void receiveAckMessage(Channel c) throws IllegalReceiveException {
        if (!ackOutRequests.containsKey(c.getSender())) {
            throw new IllegalReceiveException();
        }
        if (ackOutRequests.get(c.getSender()) == true) {
            throw new IllegalReceiveException();
        }
        ackOutRequests.put(c.getSender(), true);
        if (ackOutRequests.values().stream().allMatch(Boolean::booleanValue)) {
            sendAckBuffer.stream().forEach(c2 -> send(new AckMessage(), c2));
            sendAckBuffer.clear();
            Channel sendDoneInChannel = getOutgoing().stream()
                    .filter(c3 -> c3.getReceiver() == notifiedBy)
                    .findFirst()
                    .orElse(null);
            if (sendDoneInChannel != null && 
                doneOutRequests.values().stream().allMatch(Boolean::booleanValue) &&
                sendDoneTo != null &&
                !sendDoneTo.equals(c.getReceiver())) {
                sendDoneTo = sendDoneInChannel.getReceiver();
                send(new DoneMessage(), sendDoneInChannel);
            }
        }
    }

    private void receiveDoneMessage(Channel c) throws IllegalReceiveException {
        if (!doneOutRequests.containsKey(c.getSender())) {
            throw new IllegalReceiveException();
        }
        if (doneOutRequests.get(c.getSender())) {
            throw new IllegalReceiveException();
        }
        doneOutRequests.put(c.getSender(), true);
        if (doneOutRequests.values().stream().allMatch(Boolean::booleanValue)) {
            sendDoneBuffer.stream().forEach(c2 -> send(new DoneMessage(), c2));
            sendDoneBuffer.clear();
            Channel sendDoneInChannel = getOutgoing().stream()
                    .filter(c3 -> c3.getReceiver() == notifiedBy)
                    .findFirst()
                    .orElse(null);
            if (sendDoneInChannel != null) {
                send(new DoneMessage(), sendDoneInChannel);
            }
        }
    }

    private void receiveNotifyMessage(Channel c) throws IllegalReceiveException {
        if (!notified) {
            notifyOutRequests();
            notifiedBy = c.getSender();
            return;
        }
        Channel sendDoneInChannel = getOutgoing().stream()
                .filter(c2 -> c2.getReceiver() == c.getSender())
                .findFirst()
                .orElse(null);
        if (sendDoneInChannel == null) {
            return;
        }
        if (notified) {
            send(new DoneMessage(), sendDoneInChannel);
            sendDoneTo = sendDoneInChannel.getReceiver();
            return;
        }
        if (!allDone()) {
            sendDoneBuffer.add(sendDoneInChannel);
            return;
        }
        if ((doneOutRequests.values().stream().allMatch(Boolean::booleanValue) &&
                !doneOutRequests.isEmpty()) || notified) {
            send(new DoneMessage(), sendDoneInChannel);
            sendDoneTo = sendDoneInChannel.getReceiver();
            sendDoneBuffer.stream().forEach(c2 -> send(new DoneMessage(), c2));
            sendDoneBuffer.clear();
        } else {
            sendDoneBuffer.add(sendDoneInChannel);
        }
    }

    private void receiveGrantMessage(Channel c) {
        Channel sendAckInChannel = outRequests.stream()
                .filter(c2 -> c2.getReceiver() == c.getSender())
                .findFirst()
                .orElse(null);
        if (sendAckInChannel == null) {
            return;
        }
        if (requests == -1) {
            send(new AckMessage(), sendAckInChannel);
            return;
        }
        if (requests > 0) {
            requests--;
            if (requests == 0) {
                requests = -1;
                grantInRequests();
            }
            else {
                send(new AckMessage(), sendAckInChannel);
                return;
            }
        }
        if (ackOutRequests.values().stream().allMatch(Boolean::booleanValue) ||
                ackOutRequests.containsKey(c.getSender())) {
            send(new AckMessage(), sendAckInChannel);
            sendAckBuffer.stream().forEach(c2 -> send(new AckMessage(), c2));
            sendAckBuffer.clear();
        } else {
            sendAckBuffer.add(sendAckInChannel);
        }
    }

    private boolean allDone() {
        boolean isDone = true;
        for (Channel c : getOutgoing()) {
            BrachaTouegProcess receiver = (BrachaTouegProcess) c.getReceiver();
            if (!receiver.doneOutRequests.values().stream()
                    .allMatch(Boolean::booleanValue) || receiver.doneOutRequests.isEmpty()) {
                return false;
            }
        }
        return isDone;
    }

    private void couldTerminate() {
        if ((this instanceof BrachaTouegInitiator &&
                (doneOutRequests.values().stream().allMatch(Boolean::booleanValue))) &&
                (ackOutRequests.values().stream().allMatch(Boolean::booleanValue))) {
            String res = (free) ? "true" : "false";
            print(res);
        }
    }
}
