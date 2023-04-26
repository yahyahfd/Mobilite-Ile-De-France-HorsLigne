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

    /**
     * Compares <code>nData</code> to the NeighborData Object it's called on
     * 
     * @param nData a NeighborData
     * @return <code>true</code> if <code>nData</code> corresponds fully to <code>this</code>, false otherwise
     */
    public boolean compareNeighborData(NeighborData nData){
        return nData.duration.equals(this.duration) && nData.distance.equals(this.distance) && nData.line.equals(this.line);
    }

    public Line getLine() {
        return line;
    }

    public Duration getDuration() {
        return duration;
    }

    public Long getMillisDuration(){
        return duration.toMillis();
    }
    
    public Double getDistance() {
        return distance;
    }

    public String toString(){
        return line.getName() + " " ;
    }

}