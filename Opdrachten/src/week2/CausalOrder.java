package week2;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import framework.Network;
import framework.Process;

public class CausalOrder {

    private Set<Pair> pairs = new LinkedHashSet<>();

    public CausalOrder() {
    }

    public CausalOrder(List<Event> sequence) {

        Map<Process, ArrayList<Event>> events = new LinkedHashMap<>();

        for (Event event : sequence) {
            if (!events.containsKey(event.getProcess())) {
                events.put(event.getProcess(), new ArrayList<>());
            }
            events.get(event.getProcess()).add(event);
            if (event instanceof SendEvent) {
                addPair(event, ((SendEvent) event).getCorrespondingReceiveEvent(sequence));
            }
        }

        for (ArrayList<Event> list : events.values()) {
            for (int i = 1; i < list.size(); i++) {
                addPair(list.get(i - 1), list.get(i));
            }
        }
    }

    public Set<List<Event>> toComputation(Set<Event> events) {
        Set<List<Event>> result = new LinkedHashSet<>();
        permutation(events, new ArrayList<>(), result);
        return result;
    }

    private void permutation(Set<Event> events, List<Event> prefix, Set<List<Event>> result) {
        if (events.isEmpty() && checkCausalOrder(prefix)) {
            result.add(new ArrayList<>(prefix));
        } else {
            for (Event event : new LinkedHashSet<>(events)) {
                Set<Event> remaining = new LinkedHashSet<>(events);
                remaining.remove(event);
                prefix.add(event);
                permutation(remaining, prefix, result);
                prefix.remove(prefix.size() - 1);
            }
        }
    }

    private boolean checkCausalOrder(List<Event> sequence) {
        for (Pair pair : pairs) {
            int left = sequence.indexOf(pair.getLeft());
            int right = sequence.indexOf(pair.getRight());
            if (left == -1 || right == -1 || left > right) {
                return false;
            }
        }
        return true;
    }

    /*
     * -------------------------------------------------------------------------
     */

    @Override
    public boolean equals(Object o) {
        if (o instanceof CausalOrder) {
            CausalOrder that = (CausalOrder) o;
            return this.pairs.equals(that.pairs);
        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        return pairs.size();
    }

    @Override
    public String toString() {
        StringBuilder b = new StringBuilder();
        for (Pair p : pairs) {
            b.append(" ").append(p);
        }
        return b.toString().trim();
    }

    public void addPair(Event left, Event right) {
        pairs.add(new Pair(left, right));
    }

    public Set<Pair> getPairs() {
        return new LinkedHashSet<>(pairs);
    }

    public static CausalOrder parse(String s, Network n) {

        CausalOrder order = new CausalOrder();

        Map<String, Event> events = new LinkedHashMap<>();

        String[] tokens = s.split(" ");
        for (String token : tokens) {

            String[] subtokens = token.split("<");
            if (subtokens.length != 2) {
                throw new IllegalArgumentException();
            }

            String left = subtokens[0];
            String right = subtokens[1];

            if (!events.containsKey(left)) {
                events.put(left, Event.parse(left, n));
            }
            if (!events.containsKey(right)) {
                events.put(right, Event.parse(right, n));
            }

            order.addPair(events.get(left), events.get(right));
        }

        return order;
    }
}
