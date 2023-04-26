package fr.uparis.beryllium.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.time.Duration;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;

public class Station {
    private final String name;
    private final Localisation localisation;
    @JsonIgnore
    private final HashMap<Station, ArrayList<NeighborData>> nextStations = new HashMap<>();
    private final LinkedHashMap<Line, ArrayList<LocalTime>> lineSchedules = new LinkedHashMap<>();
    
    public boolean isSamebutWalk(Station stDestination, Line lnDestination){
        return stDestination == null ||(this.getName().equals(stDestination.getName()) && (lnDestination!= null && lnDestination.getName().equals("--MARCHE--")));
    }

    public ArrayList<LocalTime> getSchedulesOfLine(Line line) {
        return lineSchedules.get(line);
    }

    public LinkedHashMap<Line, ArrayList<LocalTime>> getLineSchedules(){
        return lineSchedules;
    }

    /**
     * Add time schedules for a specific line for a station
     * 
     * @param line line to be added
     * @param time time to be added to the line
     */
    public void addLineSchedule(Line line, LocalTime time){
        if(lineSchedules.containsKey(line)){
            lineSchedules.get(line).add(time);
        }else{
            ArrayList<LocalTime> times = new ArrayList<>();
            times.add(time);
            lineSchedules.put(line,times);
        }
        // Collections.sort(lineSchedules.get(line));
    }

    public Station(String name, Localisation localisation) {
        this.name = name;
        this.localisation = localisation;
    }

    public String getName() {
        return name;
    }

    public HashMap<Station, ArrayList<NeighborData>> getNextStations() {
        return nextStations;
    }

    public Localisation getLocalisation() {
        return localisation;
    }

    /**
     * All neighboring lines
     *
     * @return all lines of the current station to reach a neighbor station
     */
    public ArrayList<String> getNeighboringLines(){
        HashSet<String> result = new HashSet<>();
        nextStations.forEach((station, neighborDataList) -> {
            for(NeighborData nData:neighborDataList){
                result.add(nData.getLine().getLineNameWithoutVariant());
            }
        });
        return new ArrayList<>(result);
    }

    public boolean containsLine(String lineName){
        ArrayList<String> neighboringLines = getNeighboringLines();
        for(String l : neighboringLines){
            if(l.equals(lineName)) return true;
        }
        return false;
    }

    public ArrayList<Station> getNeighborsForLine2(String lineName){
        ArrayList<Station> resultStations = new ArrayList<>();
        nextStations.forEach((station, neighborDataList) -> {
            for(NeighborData nData:neighborDataList){
                if(nData.getLine().getName().equals(lineName)){
                    resultStations.add(station);
                    break; // On passe à l'itération suivante du foreach, pas la peine de vérifier la suite
                }
            }
        });
        return resultStations;
    }

    public HashMap<Station,Long> getNeighborsForLine(String lineName){
        HashMap<Station,Long> resultStations = new HashMap<>();
        nextStations.forEach((station, neighborDataList) -> {
            for(NeighborData nData:neighborDataList){
                if(nData.getLine().getName().equals(lineName)){
                    long d = nData.getDuration().getSeconds();
                    resultStations.put(station,d);
                    break; // On passe à l'itération suivante du foreach, pas la peine de vérifier la suite
                }
            }
        });
        return resultStations;
    }
    /**
     * Add station as a neighbor
     *
     * @param station        the neighbor station
     * @param line           the line with which you can reach s
     * @param durationArray  the time with which you can reach s
     * @param distance       the distance between these stations
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
                    break; // If nData exists we just stop
                }                
            }
            if(canAdd) neighborDataArrayList.add(n);
        } else {
            ArrayList<NeighborData> tmp = new ArrayList<>();
            tmp.add(n);
            nextStations.put(station, tmp);
        }
    }

    public boolean sameStation(Station station2){
        return this.name.equals(station2.name) && this.localisation.sameLocalisation(station2.localisation);
    }

    /**
     * Add all the walking neighbors
     *
     * @param walkingLine the walking line
     * @param allStations the list of all existing stations
     * @param radius the distance we want the neighbors to be in
     * @param addFirstStation 
     */
    public void addWalkingNeighbours(Line walkingLine, ArrayList<Station> allStations, double radius, Boolean addFirstStation) {
        List<Station> reacheable1kmStations = allStations.stream().filter(s -> 
        (this.isWithinARadius(radius, s.localisation) && !this.sameStation(s))).toList();
        for (Station s : reacheable1kmStations) {
            double distance = this.getDistanceToAStation(s.localisation);
            double speedHumanWalk = 8.0;
            double secondsInAnHour = 3600;
            double time = (distance * secondsInAnHour)/speedHumanWalk;
            String[] stringTime = {"0", String.valueOf(time).split("\\.")[0]};
            // ^ à modifier? Dans le parser on conserve les millis, mais pas ici

            // we only add neighbors to the initial station, because it's temporary (we don't touch the "real" stations)
            this.addNextStation(s, walkingLine, stringTime, distance, true);
            if(!addFirstStation){
                s.addNextStation(this, walkingLine, stringTime, distance, true);
            }
            // addFirstStation vraiment nécessaire? ça sert à quoi au juste?
        }
    }

    /**
     * Check if the station is within the radius we wanted
     *
     * @param dest localisation of the destination point
     * @param radius the radius we wanted to be in (in km)
     * @return a boolean that determines if the station is within the <code>radius</code>
     */
    public boolean isWithinARadius(double radius, Localisation dest){
        return getDistanceToAStation(dest)<= radius;
    }
    
    /**
     * Calculate the distance between our station and the destination point
     *
     * @param dest localisation of the destination point
     * @return the distance between our station and the destination point in km
     */
    public double getDistanceToAStation(Localisation dest){
        double x0 = this.localisation.getLongitude() * 111;
        double y0 = this.localisation.getLatitude() * (111.11 * Math.cos(Math.toRadians(this.localisation.getLongitude())));
        double x = dest.getLongitude() * 111;
        double y = dest.getLatitude() * (111.11 * Math.cos(Math.toRadians(dest.getLongitude())));
        return Math.sqrt(Math.pow(x - x0, 2.0) + Math.pow(y - y0, 2.0));
    }

    public String toString() {
        return name;
    }

    /**
     * Remove all occurrences of the temporary station which was added
     *
     * @param allStations all the stations
     * @param radius distance between the two stations
     * @param start localisation of the starting point
     * @param dest localisation of the destination point
     */
    public void removeWalkingNeighbors(ArrayList<Station> allStations, double radius, Localisation start, Localisation dest) {
        // get all stations where we added the temporary station
        // Pas correct, si on veut retirer toutes les stations temporaires rajoutée
        // Il faut supprimer toutes les stations dont le nom de line est marche
        List<Station> reacheable1kmStations = allStations.stream().filter(s -> 
        (this.isWithinARadius(radius, s.localisation) && !this.sameStation(s))).toList();
        // for all these stations, we remove the temporary station from nextstation
        for (Station s : reacheable1kmStations) {
            s.nextStations.remove(this);
        }
    }

}
