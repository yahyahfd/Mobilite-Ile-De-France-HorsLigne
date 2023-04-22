package fr.uparis.beryllium.controller;

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
   * This method allows us to send an HTTP GET request to get all stations
   * 
   * @return a list of all stations
   */
  @GetMapping("/stations")
  public List<Station> getStations() throws FormatException {
    Map m = Parser.readMap("map_data.csv");
    List<Station> stations = m.getAllStations();

    return stations;
  }

  /**
   * @param depart  contains 'name' and 'localisation' of station
   * @param arrivee contains 'name' and 'localisation' of station
   * @return path from <code>depart</code> to <code>arrivee</code>
   */
  @GetMapping("/shortest-way")
  public ArrayList<Station> shortestWay(@RequestParam String depart, @RequestParam String arrivee, @RequestParam Integer preference) throws FormatException {
    Map m = Parser.readMap("map_data.csv");
    Itinerary i = new Itinerary(m.getAllStations());
    try {
      Station start = m.searchStationByName(depart);
      Station dest = m.searchStationByName(arrivee);
      HashMap<Station,Line> res = i.shortestWay(start, dest, preference);
      return i.getPathStations(res);
    } catch (Exception e) {
      return null;
    }
  }

  @GetMapping("/shortest-way/lines")
  public HashMap<Station,Line> shortestWayLines(@RequestParam String depart, @RequestParam String arrivee, @RequestParam Integer preference) throws FormatException {
    Map m = Parser.readMap("map_data.csv");
    Itinerary i = new Itinerary(m.getAllStations());
    try {
      Station start = m.searchStationByName(depart);
      Station dest = m.searchStationByName(arrivee);
      HashMap<Station, Line> res = i.shortestWay(start, dest, preference);
      HashMap<Station, Line> resReversed = new HashMap<>();
      ArrayList<Station> stations = i.getPathStations(res);
      return res;
    } catch (Exception e) {
      return null;
    }
  }

  /**
   * Parsing method to get the name out of depart or arrivee in shortestWay method
   */
  private static String getName(String s) {
    return s.substring(0, s.indexOf(" ["));
  }

  /**
   * Parsing method to get the X coordinate out of depart or arrivee in shortestWay method
   */
  private static Double getX(String s) {
    String coordonnees = s.substring(s.indexOf("(") + 1, s.indexOf(","));
    return Double.parseDouble(coordonnees);
  }

  /**
   * Parsing method to get the Y coordinate out of depart or arrivee in shortestWay method
   */
  private static Double getY(String s) {
    String coordonnees = s.substring(s.indexOf(",") + 1, s.indexOf(")"));
    return Double.parseDouble(coordonnees);
  }
}
