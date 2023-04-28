package fr.uparis.beryllium.controller;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import fr.uparis.beryllium.exceptions.FormatException;
import fr.uparis.beryllium.model.*;

@RestController
public class MapController {

  /**
   * We stock our filled map here
   */
  private final Map map;
  /**
   * We stock our initialized itinerary here
   */
  private final Itinerary itinerary;
  /**
   * We stock all our stations here
   */
  private final ArrayList<Station> stations;
  /**
   * Our calculated path is stocked here
   */
  private HashMap<Station, Line> shortestPath = null;

  /**
   * Constructor for our MapController, called once when the app
   * is launched. Is used to initialize all the needed attributes
   */
  public MapController() throws FormatException {
    Map m = Parser.readMap("map_data.csv");
    map = Parser.readMapHoraire("timetables.csv", m);
    stations = map.getStations();
    itinerary = new Itinerary(stations);
  }

  /**
   * This method allows us to send an HTTP GET request to get all stations
   * 
   * @return a list of all stations
   */
  @GetMapping("/stations")
  public List<Station> getStations() throws FormatException {
    return stations;
  }

  /**
   * Doesn't recalculate the path if it's already calculated.
   * 
   * @param depart  contains 'name' and 'localisation' of station
   * @param arrivee contains 'name' and 'localisation' of station
	 * @param preference 0: shortest distance, 1: closest in the tree, 2: shortest time
   * 
   * @return stations from <code>depart</code> to <code>arrivee</code>
   */
  @GetMapping("/shortest-way")
  public ArrayList<Station> shortestWay(@RequestParam String depart, @RequestParam String arrivee,
      @RequestParam Integer preference) throws FormatException {
    try {
      HashMap<Station, Line> path;
      if (shortestPath == null) {
        ArrayList<Station> start = map.getStationsByName(depart);
        ArrayList<Station> dest = map.getStationsByName(arrivee);
        shortestPath = itinerary.shortestMultiplePaths(start, dest, preference);
      }
      path = shortestPath;
      shortestPath = null;
      return itinerary.getPathStations(path);
    } catch (Exception e) {
      return null;
    }
  }

  /**
   * Doesn't recalculate the path if it's already calculated.
   * 
   * @param depart  contains 'name' and 'localisation' of station
   * @param arrivee contains 'name' and 'localisation' of station
	 * @param preference 0: shortest distance, 1: closest in the tree, 2: shortest time
   * 
   * @return path from <code>depart</code> to <code>arrivee</code>
   */
  @GetMapping("/shortest-way/lines")
  public HashMap<Station, Line> shortestWayLines(@RequestParam String depart, @RequestParam String arrivee,
      @RequestParam Integer preference) throws FormatException {
    try {
      if (shortestPath == null) {
        ArrayList<Station> start = map.getStationsByName(depart);
        ArrayList<Station> dest = map.getStationsByName(arrivee);
        shortestPath = itinerary.shortestMultiplePaths(start, dest, preference);
      }
      return shortestPath;
    } catch (Exception e) {
      return null;
    }
  }
}
