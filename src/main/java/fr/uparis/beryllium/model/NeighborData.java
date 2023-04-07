package fr.uparis.beryllium.model;

import java.time.Duration;

public class NeighborData {

    private final Line line;
    private final Duration duration;
    private final Double distance;

    public NeighborData(Line line, Duration duration, Double distance) {
        this.line = line;
        this.duration = duration;
        this.distance = distance;
    }

    public Line getLine() {
        return line;
    }

    public Duration getDuration() {
        return duration;
    }

    public Double getDistance() {
        return distance;
    }


    public String toString(){
        return line.getLineName() + " " ;
    }

}