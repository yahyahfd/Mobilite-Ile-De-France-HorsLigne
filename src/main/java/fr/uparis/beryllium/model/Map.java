package fr.uparis.beryllium.model;

import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;

/**
 * Our map class, designed to store all the stations
 */
public class Map {

    // Singleton
    private static Map mapInstance = null;
    private boolean isMapLoaded = false;
    private final ArrayList<Line> lines = new ArrayList<>();
    private final ArrayList<Station> stations = new ArrayList<>();

    private Map() {
    }

    /**
     * Method used to get the map instance. Creates a new map if it isn't created yet
     *
     * @return An instance of the used map. Creates it if it doesn't exist.
     */
    public static Map getMapInstance() {
        if (mapInstance == null) {
            mapInstance = new Map();
        }
        return mapInstance;
    }

    /**
     * @return <code>true</code> if the map exists, <code>false</code> otherwise
     */
    public boolean isMapLoaded() {
        return isMapLoaded;
    }

    /**
     * Setter for <code>isMapLoaded</code>
     * <code>true</code> if the map is being loaded, <code>false</code> otherwise
     */
    public void setMapLoaded() {
        isMapLoaded = true;
    }

    public ArrayList<Line> getLines() {
        return lines;
    }

    public ArrayList<Station> getStations() {
        return stations;
    }

    /**
     * Looking for a station in our map, creates it before adding it to the map if
     * needed
     *
     * @param name     The Name of the station we're looking for
     * @param location Location of the station we're looking for
     * @return returns the Station if it exists, creates it before returning it if
     *         it doesn't exist
     */
    public Station searchStation(String name, Location location) {
        for (Station s : stations) {
            if ((s.getName()).equals(name) && (s.getLocation()).sameLocation(location))
                return s;
        }
        Station newStation = new Station(name, location);
        stations.add(newStation);
        return newStation;
    }

    /**
     * Looking for a line in our map
     *
     * @param name the name of a line we're looking for
     * @return Returns the Line if it exists, null otherwise
     */
    public Line searchLine(String name) {
        for (Line l : lines) {
            if ((l.getName()).equals(name))
                return l;
        }
        return null;
    }


    /**
     * Looking for lines with a given name in our map
     * @param name
     * @return all lines without variant with the name given
     */
    public ArrayList<Line> searcheLines(String name) {
        ArrayList<Line> result = new ArrayList<Line>();
        for (Line l : lines) {
            if(l.getLineNameWithoutVariant().equals(name))
                result.add(l);
        }
        return result;
    }

    /**
     * Creates a new line and adds to our map
     * 
     * @param name the name of our line
     * @return The line that was created
     */
    public Line addLine(String name){
        Line newLine = new Line(name);
        lines.add(newLine);
        return newLine;
    }

    /**
     * Method used to remove a station from the list of all stations
     *
     * @param station The station to remove from the list
     */
    public void removeStation(Station station) {
        stations.remove(station);
    }

    /**
     * creates and adds a new station to our map
     *
     * @param latitude  latitude of our new station
     * @param longitude longitude of our new station
     * @param name      name of our new station
     */
    public void addStation(Double latitude, Double longitude, String name) {
        Station s = new Station(name, new Location(latitude, longitude));
        // we add the station to the list of stations
        stations.add(s);
    }

    /**
     * Method used in terminal mode to get all the stations with <code>name</code>
     * as a name
     * 
     * @param name A station's name
     * @return An ArrayList of stations with the corresponding name (can be empty if
     *         no stations found)
     */
    public ArrayList<Station> getStationsByName(String name) {
        ArrayList<Station> result = new ArrayList<Station>();
        for (Station s : stations) {
            if (StringUtils.stripAccents(s.getName()).equalsIgnoreCase(StringUtils.stripAccents(name)))
                result.add(s);
        }
        return result;
    }

    /**
     * Search for all stations between dist (start to dest) and add these stations
     * as neighbors of the initial position
     *
     * @param start           The starting station (coordonnees)
     * @param addFirstStation
     * @param lDest           the location of the destination station
     */
    public void walkToBestStation(Station start, Boolean addFirstStation, Location lDest) {
        // we get the walking line, create it if it doesn't exist
        Line getLine = this.searchLine("--MARCHE--");
        Line walkingLine = getLine==null?addLine("--MARCHE--"):getLine;
        // we get the distance from the starting point to the final destination (it will
        // be our area of research)
        double radius = start.getDistanceToAStation(lDest);
        // we add the stations that are within this perimeter as neighbors of the position
        start.addWalkingNeighbours(walkingLine, this.getStations(), radius, addFirstStation);
    }

}
