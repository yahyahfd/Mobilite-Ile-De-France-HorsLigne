package fr.uparis.beryllium.controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import fr.uparis.beryllium.exceptions.FormatException;
import fr.uparis.beryllium.model.*;
import jakarta.servlet.http.HttpServletResponse;
import net.minidev.json.JSONObject;

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
   * @return path from <code>depart</code> to <code>arrivee</code>
   */
  @GetMapping("/shortest-way")
  public void shortestWay(@RequestParam String depart, @RequestParam String arrivee,
                                 @RequestParam Integer preference, HttpServletResponse response) throws FormatException {
    try {
      if (shortestPath == null) {
        ArrayList<Station> start = map.getStationsByName(depart);
        ArrayList<Station> dest = map.getStationsByName(arrivee);
        shortestPath = itinerary.shortestMultiplePaths(start, dest, preference);
      }

      ObjectMapper pathMapper = new ObjectMapper();
      pathMapper.registerModule(new JavaTimeModule());

      JSONObject jsonObject = new JSONObject();
      jsonObject.put("stations", itinerary.getPathStations(shortestPath));
      jsonObject.put("lines", itinerary.getPathLines(shortestPath));

      String responseString = pathMapper.writeValueAsString(jsonObject);

      response.setContentType("application/json");
      response.setCharacterEncoding("UTF-8");

      response.getWriter().write(responseString);

      shortestPath = null;

    } catch (Exception e) {
      e.printStackTrace();
    }
  }






}
