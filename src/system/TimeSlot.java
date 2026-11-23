package system;

public class TimeSlot {

    private final IDateTime start;
    private final IDateTime end;

    public TimeSlot(IDateTime start, IDateTime end) throws IllegalArgumentException {
        if (start.compareTo(end) >= 0)
            throw new IllegalArgumentException("Invalid timeslot, start time must be earlier than end time.");

        this.start = start;
        this.end = end;
    }

    public IDateTime getStart() {
        return start;
    }

    public IDateTime getEnd() {
        return end;
    }

    public int compareTo(ITimeSlot other) {
        if (this.end.compareTo(other.getStart()) <=0)             // this slot ends at or before other starts.
            return -1;
        if (this.start.compareTo(other.getEnd()) >= 0)            // a positive value if this slot starts at or after other ends.
            return 1;
        return 0;                                                 // 0 if the two slots overlap.
    }




}
