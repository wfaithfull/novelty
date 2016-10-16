package ac.uk.bangor.experiment.casestudy;

import lombok.Getter;

/**
 * Represents a true change point in input data so that we can retroactively evaulate our performance.
 */
@Getter
public class ChangePoint implements Comparable<ChangePoint> {
    private final long index;
    private final String from;
    private final String to;
    private long detected = -1;
    private long ttd = 0;

    public ChangePoint(long index, String from, String to) {
        this.index = index;
        this.from = from;
        this.to = to;
    }

    public void setDetected(long detected) {
        if(detected != -1) {
            this.detected = detected;
            this.ttd = detected - index;
        }
    }

    @Override
    public String toString() {
        return String.format(getClass().getName() + "[from=%s,to=%s,index=%d,ttd=%d]", from, to, index, ttd);
    }

    @Override
    public int compareTo(ChangePoint o) {
        if(index < o.index)
            return -1;
        else if (index > o.index)
            return 1;
        else
            return 0;
    }
}