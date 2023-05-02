package fr.uparis.beryllium.model;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.time.Duration;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Our station class. Used to store all the data needed for each station,
 * such as the location, the neighbors and the schedules. 
 */
public class Station {

    /**
     * Our station's name.
     */
    private final String name;

    /**
     * Our station's location.
     */
    private final Location location;

    /**
     * The neighboring stations of our stations.
     */
    @JsonIgnore
    private final HashMap<Station, ArrayList<NeighborData>> nextStations = new HashMap<>();

    /**
     * The line schedules of our station. Each station can have multiple lines, therefore,
     * we stock here an ArrayList of all the times for each line, if at least one
     * neighbor has this line (if to reach a certain neighbor, we need to take this line).
     */
    private final LinkedHashMap<Line, ArrayList<LocalTime>> lineSchedules = new LinkedHashMap<>();

    /**
     * Constructor of our station.
     *
     * @param name     name of our station
     * @param location location of our station
     */
    public Station(String name, Location location) {
        this.name = name;
        this.location = location;
    }

    /**
     * Getter for name.
     *
     * @return <code>name</code>
     */
    public String getName() {
        return name;
    }

    /**
     * Getter for nextStations, neighbors of our current station.
     *
     * @return <code>nextStations</code>
     */
    public HashMap<Station, ArrayList<NeighborData>> getNextStations() {
        return nextStations;
    }

    /**
     * Getter for location.
     *
     * @return <code>location</code>
     */
    public Location getLocation() {
        return location;
    }

    /**
     * Getter for the schedule of a certain line.
     *
     * @param line line for which we're looking for the ArrayList of schedules
     * @return the ArrayList of schedules if found, null otherwise
     */
    public ArrayList<LocalTime> getSchedulesOfLine(Line line) {
        return lineSchedules.get(line);
    }

    /**
     * Getter for lineSchedules.
     *
     * @return <code>lineSchedules</code>
     */
    public LinkedHashMap<Line, ArrayList<LocalTime>> getLineSchedules() {
        return lineSchedules;
    }

    /**
     * All neighboring lines, with the variant in line
     *
     * @return all lines of the current station to reach a neighboring station
     */
    public ArrayList<String> getNeighboringLines() {
        HashSet<String> result = new HashSet<>();
        nextStations.forEach((station, neighborDataList) -> {
            for (NeighborData nData : neighborDataList) {
                if (!nData.getLine().getName().equals("--MARCHE--")) {
                    result.add(nData.getLine().getName());
                }
            }
        });
        return new ArrayList<>(result);
    }

    public ArrayList<String> getNeighboringLinesWithoutVariant(){
        HashSet<String> result = getNeighboringLines().stream().map(lineName -> {
            return ! lineName.equals("--MARCHE--") ? lineName.split("\\.")[0] : lineName;
        }).collect(Collectors.toCollection(HashSet::new));
        return new ArrayList<>(result);
    }

    public boolean containsScheduleOnLine(Line line, LocalTime schedule) {
        ArrayList<LocalTime> mySchedule = getSchedulesOfLine(line);
        return mySchedule != null && mySchedule.contains(schedule);
    }

    /**
     * Add time schedules for a specific line of this station.
     * Doesn't check if a neighbor can be reached with this line.
     * This checking wouldn't be needed anyway thanks to our
     * path searching algorithm.
     *
     * @param line line to be added
     * @param time time to be added to the line
     */
    private void addLineSchedule(Line line, LocalTime time){
        if(lineSchedules.containsKey(line)){
            lineSchedules.get(line).add(time);
        }else{
            ArrayList<LocalTime> times = new ArrayList<>();
            times.add(time);
            lineSchedules.put(line,times);
        }
        Collections.sort(lineSchedules.get(line));
    }
    
    public void propagateSchedules(LocalTime time, Line line){
        if(!this.containsLine(line)) return; // juste pour la premiere vérification
        this.addLineSchedule(line, time);
        HashMap<Station,Long> neighbors = getNeighborsForLine(line.getName());
        neighbors.forEach((station,duration)->{
            station.propagateSchedules(time.plusSeconds(duration), line);
        });
    }

    /**
     * Checks whether a line is present in any of our neighbors.
     * 
     * @return true if the line is present in at least a neighbor, false otherwise
     */
    private boolean containsLine(Line line){
        if(line == null) return false;
        ArrayList<String> neighboringLines = getNeighboringLines();
        for(String l : neighboringLines){
            if(l.equals(line.getName())) return true;
        }
        return false;
    }

    /**
     * Gets all the neighbors that we can reach using a certain line.
     * 
     * @param lineName name of the line
     * @return ArrayList of all the neighbors that we can reach using <code>lineName</code>
     */
    private HashMap<Station,Long> getNeighborsForLine(String lineName){
        HashMap<Station,Long> resultStations = new HashMap<>();
        nextStations.forEach((station, neighborDataList) -> {
            for(NeighborData nData:neighborDataList){
                if(nData.getLine().getName().equals(lineName)){
                    long d = nData.getDuration().getSeconds();
                    resultStations.put(station,d);
                }
            }
        });
        return resultStations;
    }

    /**
     * Add station as a neighbor.
     *
     * @param station        the neighboring station
     * @param line           the line to use to get <code>station</code>
     * @param durationArray  the time it takes to reach <code>station</code>
     * @param distance       the distance to travel to reach <code>station</code>
     * @param addWalkingNeig
     */
    public void addNextStation(Station station, Line line, String[] durationArray, Double distance, Boolean addWalkingNeig) {
        Duration duration = Duration.ZERO;
        // we get the duration from the csv
        if(!addWalkingNeig){
            String temps = durationArray[0].concat(String.valueOf(durationArray[1].charAt(0)));
            duration = duration.plusSeconds(Long.parseLong(temps));
            String milli = durationArray[1].substring(1);
            if (milli.equals("")) {
                milli = "0";
            }
            duration = duration.plusMillis(Long.parseLong(milli));
        }else{
            // we get the duration from calculation when walking
            duration = duration.plusSeconds(Long.parseLong(durationArray[1]));
        }
        NeighborData n = new NeighborData(line, duration, distance);

        if (nextStations.containsKey(station)) {
            ArrayList<NeighborData> neighborDataArrayList = nextStations.get(station);
            boolean canAdd = true;
            for(NeighborData nData: neighborDataArrayList){
                if(nData.compareNeighborData(n)){
                    canAdd = false;
                    break; // If nData exists, we just stop
                }                
            }
            if(canAdd) neighborDataArrayList.add(n);
        } else {
            ArrayList<NeighborData> tmp = new ArrayList<>();
            tmp.add(n);
            nextStations.put(station, tmp);
        }
    }

    /**
     * Checks if two stations are the same.
     * 
     * @param station station to compare to this
     * @return true if same stations, false otherwise
     */
    public boolean sameStation(Station station){
        return this.name.equals(station.name) && this.location.sameLocation(station.location);
    }

    /**
     * Add all the walking neighbors.
     *
     * @param walkingLine the walking line
     * @param allStations the list of all existing stations
     * @param radius the distance we want the neighbors to be in
     * @param addFirstStation 
     */
    public void addWalkingNeighbours(Line walkingLine, ArrayList<Station> allStations, double radius, Boolean addFirstStation) {
        List<Station> reacheable1kmStations = allStations.stream().filter(s -> 
        (this.isWithinARadius(radius, s.location) && !this.sameStation(s))).toList();
        for (Station s : reacheable1kmStations) {
            double distance = this.getDistanceToAStation(s.location);
            double speedHumanWalk = 8.0;
            double secondsInAnHour = 3600;
            double time = (distance * secondsInAnHour)/speedHumanWalk;
            String[] stringTime = {"0", String.valueOf(time).split("\\.")[0]};

            // we only add neighbors to the initial station, because it's temporary (we don't touch the "real" stations)
            this.addNextStation(s, walkingLine, stringTime, distance, true);
            if(!addFirstStation){
                s.addNextStation(this, walkingLine, stringTime, distance, true);
            }
        }
    }

    /**
     * Check if the station is within the radius we wanted.
     *
     * @param dest location of the destination point
     * @param radius the radius we wanted to be in (in km)
     * @return a boolean that determines if the station is within the <code>radius</code>
     */
    public boolean isWithinARadius(double radius, Location dest){
        return getDistanceToAStation(dest)<= radius;
    }
    
    /**
     * Calculate the distance between our station and the destination point.
     *
     * @param dest location of the destination point
     * @return the distance between our station and the destination point in km
     */
    public double getDistanceToAStation(Location dest){
        double x0 = this.location.getLongitude() * 111;
        double y0 = this.location.getLatitude() * (111.11 * Math.cos(Math.toRadians(this.location.getLongitude())));
        double x = dest.getLongitude() * 111;
        double y = dest.getLatitude() * (111.11 * Math.cos(Math.toRadians(dest.getLongitude())));
        return Math.sqrt(Math.pow(x - x0, 2.0) + Math.pow(y - y0, 2.0));
    }


    public LocalTime getNextTrainTime(Line line, LocalTime time) {
        ArrayList<LocalTime> schedule = this.getSchedulesOfLine(line);
        // there is no horaire for this station on the given line
        if(schedule == null) {
            return null;
        }
        for (LocalTime localTime : schedule) {
            if (localTime.equals(time) || localTime.isAfter(time)) {
                return localTime;
            }
        }
        return null;
    }

    @JsonIgnore
    public String toString() {
        return name;
    }
}
