package fr.uparis.beryllium.model;
import java.util.ArrayList;
import org.apache.commons.lang3.StringUtils;
// import java.text.Normalizer;

// On a vraiment besoin de lines ici? l'information est déjà stockée dans les neighbors non?
// ON duplique l'information lors du parsing, on rajoute la ligne ici ET dans le neighbor
// lors de la création d'une station.
// MAP à REVOIR
/**
 * Our map class, designed to store all the stations
 */
public class Map {

    // Singleton
    private static Map mapInstance = null;
    private boolean isMapLoaded = false;
    private Map(){}

    /**
     * Method used to get the map instance. Creates a new map if it isn't created yet
     * @return An instance of the used map. Creates it if it doesn't exist.
     */
    public static Map getMapInstance() {
        if(mapInstance == null){
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
     * @param mapLoaded <code>true</code> if the map is being loaded, <code>false</code> otherwise
     */
    public void setMapLoaded() {
        isMapLoaded = true;
    }

    private ArrayList<Line> lines = new ArrayList<>();
    private ArrayList<Station> stations = new ArrayList<>();

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
     * @param name         Name of the station we are looking for
     * @param location Location of the station we are looking for
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
     * @param name the name of a line we are looking for
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
    // ^ à modifier, utilisé une seule fois uniquement pour une marche à pieds au
    // début et à la fin (Terminal)

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
     * Search for all stations inbetween dist (start to dest) and add these stations
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

    /**
     * Method used in terminal mode to get the first station with <code>name</code>
     * as a name
     * 
     * @param name
     * @return The first station with the corresponding name (can be null if no
     *         station found)
     */
    public Station getStationByName(String name) {
        for (Station s : stations) {
            if (StringUtils.stripAccents(s.getName()).equalsIgnoreCase(StringUtils.stripAccents(name)))
                return s;
        }
        return null;
    }
}
