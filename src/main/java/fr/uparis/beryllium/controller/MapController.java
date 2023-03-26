package fr.uparis.beryllium.controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import fr.uparis.beryllium.model.*;

@RestController
public class MapController {

  /**
   * This method allows us to send an HTTP GET request to get all stations
   * 
   * @return a list of all stations
   */
  @GetMapping("/stations")
  public List<Station> getStations() {
    Map m = Parser.readMap("map_data.csv");
    List<Station> stations = m.getStations();

    return stations;
  }

  /**
   * @param depart  contains 'name' and 'localisation' of station
   * @param arrivee contains 'name' and 'localisation' of station
   * @return path from <code>depart</code> to <code>arrivee</code>
   */
  @GetMapping("/shortest-way")
  public List<Station> shortestWay(@RequestParam String depart, @RequestParam String arrivee) {
    Map m = Parser.readMap("map_data.csv");
    Itinerary i = new Itinerary(m.getAllStations());
    // Convertir les valeurs des champs de saisie en instances de Station
    try {
      Station start = m.searchStation(getName(depart), new Localisation(getX(depart), getY(depart)));
      Station dest = m.searchStation(getName(arrivee), new Localisation(getX(arrivee), getY(arrivee)));
      HashMap<Station,Line> res = i.shortestWay(start, dest, 0);
      if(res == null){
        return new ArrayList<>();
      }else{
        return i.getPathStations(res);
      }
      
    } catch (Exception e) {
      return null;
    }
  }

  private static String getName(String s) {
    return s.substring(0, s.indexOf(" ["));
  }

  private static Double getX(String s) {
    String coordonnees = s.substring(s.indexOf("(") + 1, s.indexOf(","));
    return Double.parseDouble(coordonnees);
  }

  private static Double getY(String s) {
    String coordonnees = s.substring(s.indexOf(",") + 1, s.indexOf(")"));
    return Double.parseDouble(coordonnees);
  }
}
