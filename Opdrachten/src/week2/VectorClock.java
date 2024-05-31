package week2;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import framework.Network;
import framework.Process;

public class VectorClock extends LogicalClock<Map<Process, Integer>> {

    private Map<Process, Map<Process, Integer>> vectorClock = new HashMap<>();
    private List<Event> allEvents = new ArrayList<>();

    public VectorClock(Map<Process, List<Event>> sequences) {
        allEvents = sequences.values().stream().collect(ArrayList::new, List::addAll, List::addAll);
        sequences.keySet().forEach(p -> {
            vectorClock.put(p, new HashMap<Process, Integer>() {
                {
                    sequences.keySet().forEach(p -> put(p, 0));
                }
            });
        });
        buildTimestamps(sequences);
    }

    private void buildTimestamps(Map<Process, List<Event>> sequences) {
        if (sequences.values().stream().allMatch(List::isEmpty)) {
            return;
        }

        Map<Process, List<Event>> remaining = new HashMap<>(sequences);

        for (Entry<Process, List<Event>> entry : sequences.entrySet()) {
            Process p = entry.getKey();
            List<Event> events = entry.getValue();

            List<Event> eventsToRemove = getEventsToRemove(p, events);
            updateClock(p, eventsToRemove);
            events.removeAll(eventsToRemove);
            remaining.put(p, new ArrayList<>(events));
        }
        buildTimestamps(remaining);
    }

    private List<Event> getEventsToRemove(Process p, List<Event> events) {
        List<Event> eventsToRemove = new ArrayList<>();

        for (Event e : events) {
            if (e instanceof InternalEvent || e instanceof SendEvent) {
                if (!containsTimestamp(e)) {
                    eventsToRemove.add(e);
                }
            } else if (e instanceof ReceiveEvent) {
                if (!containsTimestamp(e)) {
                    SendEvent correspondingSendEvent = ((ReceiveEvent) e).getCorrespondingSendEvent(allEvents);
                    if (containsTimestamp(correspondingSendEvent)) {
                        eventsToRemove.add(e);
                    } else {
                        break;
                    }
                }
            }
        }
        return eventsToRemove;
    }

    private void updateClock(Process p, List<Event> eventsToRemove) {
        for (Event e : eventsToRemove) {
            if (e instanceof InternalEvent || e instanceof SendEvent) {
                vectorClock.get(p).put(p, vectorClock.get(p).get(p) + 1);
                addTimestamp(e, new HashMap<>(vectorClock.get(p)));
            } else if (e instanceof ReceiveEvent) {
                SendEvent correspondingSendEvent = ((ReceiveEvent) e).getCorrespondingSendEvent(allEvents);
                Map<Process, Integer> correspondingTimestamp = getTimestamp(correspondingSendEvent);
                Map<Process, Integer> previousTimestamp = vectorClock.get(p);
                Map<Process, Integer> maxTimestamp = maxTimestamps(previousTimestamp, correspondingTimestamp);
                vectorClock.put(p, maxTimestamp);
                vectorClock.get(p).put(p, vectorClock.get(p).get(p) + 1);
                addTimestamp(e, new HashMap<>(vectorClock.get(p)));
            }
        }
    }

    private Map<Process, Integer> maxTimestamps(Map<Process, Integer> previousTimestamp,
            Map<Process, Integer> correspondingTimestamp) {

        Map<Process, Integer> result = new HashMap<>();

        for (Map.Entry<Process, Integer> entry : previousTimestamp.entrySet()) {
            result.put(entry.getKey(), Math.max(entry.getValue(), correspondingTimestamp.getOrDefault(entry.getKey(), 0)));
        }

        return result;
    }

    /*s
     * -------------------------------------------------------------------------
     */

    public static Map<Process, Integer> parseTimestamp(String s, Network n) {
        String[] tokens = s.split(",");
        List<Process> processes = new ArrayList<>(n.getProcesses().values());
        if (tokens.length != processes.size()) {
            throw new IllegalArgumentException();
        }

        Map<Process, Integer> timestamp = new LinkedHashMap<>();

        for (int i = 0; i < tokens.length; i++) {
            try {
                timestamp.put(processes.get(i), Integer.parseInt(tokens[i]));
            } catch (Throwable t) {
                throw new IllegalArgumentException();
            }
        }

        return timestamp;
    }
}
