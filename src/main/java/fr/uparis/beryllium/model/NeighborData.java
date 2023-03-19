package fr.uparis.beryllium.model;

import java.time.Duration;

public class NeighborData {

    private final SubLine subLine;
    private final Duration duration;
    private final Double distance;

    public NeighborData(SubLine subLine, Duration duration, Double distance) {
        this.subLine = subLine;
        this.duration = duration;
        this.distance = distance;
    }

    public SubLine getLine() {
        return subLine;
    }

    public Duration getDuration() {
        return duration;
    }

    public Double getDistance() {
        return distance;
    }

}
