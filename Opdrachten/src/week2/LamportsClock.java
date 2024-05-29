package week2;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import framework.Process;

public class LamportsClock extends LogicalClock<Integer> {

    private Map<Process, Integer> clocks = new HashMap<>();
    private List<Event> allEvents = new ArrayList<>();

    public LamportsClock(Map<Process, List<Event>> sequences) {
        allEvents = sequences.values().stream().collect(ArrayList::new, List::addAll, List::addAll);
        sequences.keySet().forEach(p -> clocks.put(p, 0));
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
                int clock = clocks.get(p) + 1;
                addTimestamp(e, clock);
                clocks.put(p, clock);
            } else {
                SendEvent correspondingSendEvent = ((ReceiveEvent) e).getCorrespondingSendEvent(allEvents);
                int clock = Math.max(clocks.get(p), getTimestamp(correspondingSendEvent)) + 1;
                addTimestamp(e, clock);
                clocks.put(p, clock);
            }
        }
    }
}
