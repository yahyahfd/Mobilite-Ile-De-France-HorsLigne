package fr.uparis.beryllium.model;

import java.time.Duration;

/**
 * This class is used to store all the data to be known to reach
 * a neighboring station, when you are on a starting station.
 */
public class NeighborData {

    /**
     * The line to be used to reach this station 
     * from our starting station.
     */
    private Line line;
    /**
     * The time it takes to reach this station
     * from our starting station.
     */
    private final Duration duration;
    /**
     * The distance between our starting station
     * and this station. 
     */
    private final Double distance;

    /**
     * Constructor for oNeighborData.
     * 
     * @param line The line used to reach this station
     * @param duration The time it takes to reach this station
     * @param distance The distance to travel to reach this station
     */
    public NeighborData(Line line, Duration duration, Double distance) {
        this.line = line;
        this.duration = duration;
        this.distance = distance;
    }

    public void setLine(Line line) {
        this.line = line;
    }

    /**
     * Compares <code>nData</code> to the NeighborData Object it's called on.
     * 
     * @param nData a NeighborData
     * @return <code>true</code> if <code>nData</code> corresponds fully to <code>this</code>, false otherwise
     */
    public boolean compareNeighborData(NeighborData nData){
        return nData.duration.equals(this.duration) && nData.distance.equals(this.distance) && nData.line.equals(this.line);
    }

    /**
     * Getter for line.
     * 
     * @return <code>line</code>
     */
    public Line getLine() {
        return line;
    }

    /**
     * Getter for duration.
     * 
     * @return <code>duration</code>
     */
    public Duration getDuration() {
        return duration;
    }

    /**
     * Getter for duration that converts it to milliseconds.
     * 
     * @return <code>duration<code> in milliseconds
     */
    public Long getMillisDuration(){
        return duration.toMillis();
    }
    
    /**
     * Getter for distance.
     * 
     * @return <code>distance<code>
     */
    public Double getDistance() {
        return distance;
    }

    // Pas nécessaire ? on peut juste faire un println(line) ou line.toString là où on veut etc
    // Changer le toString pour qu'il soit plus parlant
    // (Line,time,distance)
    public String toString(){
        return line.getName() + " " ;
    }

}