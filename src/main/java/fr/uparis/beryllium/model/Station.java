package fr.uparis.beryllium.model;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Station {
    private final String name;
    private Localisation usedLocalisation;
    private HashMap<String, Localisation> localisations = new HashMap<>();
    @JsonIgnore
    private final Map<Station, ArrayList<NeighborData>> nextStations = new HashMap<>();

    public Station(String n, Localisation localisation, String lineNumber) {
        name = n;
        localisations.put(lineNumber, localisation);
    }

    public String getName() {
        return name;
    }

    public Map<Station, ArrayList<NeighborData>> getNextStations() {
        return nextStations;
    }

    public HashMap<String, Localisation> getLocalisations() {
        return localisations;
    }

    public Localisation getLocalisation() {
        return usedLocalisation;
    }

    /**
     * All neighboring lines
     *
     * @return all lines of the current station to reach a neighbor station
     */
    public ArrayList<String> getNeighboringLines() {
        ArrayList<String> result = new ArrayList<>();
        nextStations.forEach((station, neighborDataList) -> {
            for (NeighborData nd : neighborDataList) {
                String lineName = nd.getLine().getLineNameWithoutVariant();
                if (!result.contains(lineName)) {
                    result.add(lineName);
                }
            }
        });
        return result;
    }

    public void setLocalisation(Localisation localisation) {
        usedLocalisation = localisation;
    }

    public void setLocalisations(HashMap<String, Localisation> localisations) {
        this.localisations = localisations;
    }

    /**
     * ...
     *
     * @param localisation
     * @return
     */
    public boolean hasThisLocalisation(Localisation localisation) {
        for (Localisation localisationI : localisations.values()) {
            if (localisationI.sameLocalisation(localisation)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Add station as a neighbor
     *
     * @param station        the neighbor station
     * @param line           the line with which you can reach s
     * @param durationArray  the time with which you can reach s
     * @param distance       the distance between these stations
     * @param addWalkingNeig
     * @param localisation   the localisation of the starting point
     */
    public void addNextStation(Station station, Line line, String[] durationArray, Double distance, Boolean addWalkingNeig, Localisation localisation) {
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
            if (!neighborDataIsIn(neighborDataArrayList, duration, line, distance)) {
                neighborDataArrayList.add(n);
            }
        } else {
            ArrayList<NeighborData> tmp = new ArrayList<>();
            tmp.add(n);
            nextStations.put(station, tmp);
        }
        if (addWalkingNeig) {
            localisations.put(line.getLineName(), localisation);
        }
    }

    /**
     * Check if the Neighbor exist in the list for nextStations
     *
     * @param list     the list of the neighbors
     * @param duration the duration between 2 stations
     * @param l        the line used between 2 stations
     * @param dist     the distance between 2 stations
     * @return true if the neighbor already exist in the list of nextStations
     */
    public boolean neighborDataIsIn(ArrayList<NeighborData> list, Duration duration, Line l, Double dist) {
        for (NeighborData n : list) {
            if (n.getDuration().equals(duration) && n.getDistance().equals(dist) && n.getLine() == l) return true;
        }
        return false;
    }

    /**
     * Add all the walking neighbors
     *
     * @param walkingLine the walking line
     * @param allStations the list of all existing stations
     * @param radius the distance we want the neighbors to be in
     * @param addFirstStation
     * @param st the localisation of the starting point
     */
    public void addWalkingNeighbours(Line walkingLine, ArrayList<Station> allStations, double radius, Boolean addFirstStation, Localisation st) {
        //List<Station> reacheable1kmStations = allStations.stream().filter(s -> (s.isWithinARadius(this, radius) && !s.equals(this))).collect(Collectors.toList());;
        for (Station s: allStations) {
            List<Localisation> destLocalisation = s.localisations.values().stream().toList();
                for (Localisation dest : destLocalisation) {
                    if (dest.getLongitude() != st.getLongitude() && dest.getLatitude() != st.getLatitude()) {
                        List<Localisation> startLocalisation = localisations.values().stream().toList();
                        for (Localisation start : startLocalisation) {
                            if (s.isWithinARadius(start, dest, radius)) {
                                double distance = s.getDistanceToAStation(start, dest);
                                double time = s.getWalkingTimeInSecondsFromADistance(distance);
                                String[] stringTime = {"0", String.valueOf(time).split("\\.")[0]};
                                // we only add neighbors to the initial station, because it's temporary (we don't touch the "real" stations)
                                this.addNextStation(s, walkingLine, stringTime, distance, true, start);
                                if (!addFirstStation) {
                                    s.addNextStation(this, walkingLine, stringTime, distance, true, dest);
                                }
                            }
                        }
                    }
                }

        }
    }

    /**
     * Check if the station is in the radius we wanted
     *
     * @param start localisation of the starting point
     * @param dest localisation of the destination point
     * @param radius the radius we wanted to be in (in km)
     * @return a boolean that determines if the station is in the radius away or less
     */
    public boolean isWithinARadius(Localisation start, Localisation dest, double radius) {
        double distance = getDistanceToAStation(start, dest);
        return distance <= radius;
    }

    /**
     * Calculate the distance to the other station
     *
     * @param start localisation of the starting point
     * @param dest localisation of the destination point
     * @return the distance between our station and the other station in km
     */
    public double getDistanceToAStation(Localisation start, Localisation dest) {
        double x0 = start.getLongitude() * 111;
        double y0 = start.getLatitude() * (111.11 * Math.cos(Math.toRadians(start.getLongitude())));
        double x = dest.getLongitude() * 111;
        double y = dest.getLatitude() * (111.11 * Math.cos(Math.toRadians(dest.getLongitude())));
        return Math.sqrt(Math.pow(x - x0, 2.0) + Math.pow(y - y0, 2.0));
    }

    /**
     * Calculate the time it takes to cover a distance
     *
     * @param distance the distance between 2 stations in km
     * @return the time it takes to cover the distance
     */
    private double getWalkingTimeInSecondsFromADistance(double distance) {
        double speedHumanWalk = 8.0;
        double timeHumanWalk = 3600;
        
        return (distance * timeHumanWalk) / speedHumanWalk;
    }

    /**
     * Remove all occurrences of the temporary station which was added
     *
     * @param allStations all the stations
     * @param radius distance between the two stations
     * @param start localisation of the starting point
     * @param dest localisation of the destination point
     */
    public void removeWalkingNeighbours(ArrayList<Station> allStations, double radius, Localisation start, Localisation dest) {
        // get all stations where we added the temporary station 
        List<Station> reacheable1kmStations = allStations.stream().filter(s -> s.isWithinARadius(start, dest, radius) && !s.equals(this)).toList();
        // for all these stations, we remove the temporary station from nextstation
        for (Station s : reacheable1kmStations) {
            s.nextStations.remove(this);
        }
    }

    public String toString() {
        return name;
    }
}
