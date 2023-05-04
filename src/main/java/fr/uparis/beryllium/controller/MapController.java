package fr.uparis.beryllium.controller;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
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
/**
 * Map Controller class that allows us to make HTTP requests
 */
public class MapController {

  /**
   * Logger to print a message saying that we are loading the data (parsing)
   */
  private static final Logger LOGGER = LogManager.getLogger(Parser.class);

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
    LOGGER.info("\033[1;30mLoading data... Might take some time...\u001B[0m");
    Map m = Parser.readMap("map_data.csv");
    map = Parser.readMapHoraire("newtimetables.csv", m);
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
  public void shortestWay(@RequestParam String depart,
                          @RequestParam String arrivee,
                          @RequestParam Integer preference,
                          @RequestParam String time,
                          HttpServletResponse response) throws FormatException {
    try {
      LocalTime timeWeLeft = LocalTime.now();
      if (shortestPath == null) {
        ArrayList<Station> start = map.getStationsByName(depart);
        ArrayList<Station> dest = map.getStationsByName(arrivee);

        timeWeLeft = (time == null || time.equals("") || time.equals("now")) ? LocalTime.now()
                : LocalTime.parse(time, DateTimeFormatter.ofPattern("HH:mm"));
        shortestPath = itinerary.shortestMultiplePaths(start, dest, preference, timeWeLeft);
      }

      ObjectMapper pathMapper = new ObjectMapper();
      pathMapper.registerModule(new JavaTimeModule());

      JSONObject jsonObject = new JSONObject();
      jsonObject.put("stations", itinerary.getPathStations(shortestPath));
      jsonObject.put("lines", itinerary.getPathLines(shortestPath));
      jsonObject.put("dates",itinerary.getDates(shortestPath));
      jsonObject.put("startingTime",timeWeLeft);
      Double totalDist = itinerary.getDistCountTimes(shortestPath).getLast().getLeft();
      Long totalTime = itinerary.getDistCountTimes(shortestPath).getLast().getRight();
      jsonObject.put("distTotal",totalDist);
      jsonObject.put("timeTotal",totalTime);
      jsonObject.put("endingTime",timeWeLeft.plusMinutes(totalTime));
      String responseString = pathMapper.writeValueAsString(jsonObject);

      response.setContentType("application/json");
      response.setCharacterEncoding("UTF-8");

      response.getWriter().write(responseString);

      shortestPath = null;

    } catch (Exception e) {
      e.printStackTrace();
      shortestPath = null;
    }
  }

  /**
   * This method allows us to send an HTTP GET request to get all schedules of a station for a line
   * @param stationName
   * @param lineName
   * @return
   * @throws FormatException
   */
  @GetMapping("/schedules")
  public List<String> getSchedules(@RequestParam String stationName, @RequestParam String lineName) throws FormatException {
    List<String> schedules = new ArrayList<>();
    ArrayList<Line> lines = map.searcheLines(lineName);
    ArrayList<Station> stations = map.getStationsByName(stationName);

    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm:ss");

    List<LocalTime> times = new ArrayList<>();

    for (Station station : stations) {
      for (Line line : lines) {

        List<LocalTime> tmp = station.getSchedulesOfLine(line);
        if(tmp != null) {
          times.addAll(tmp);
          Collections.sort(times);
        }
      }
    }

    for (LocalTime time : times) {
      schedules.add(time.format(formatter));
    }
    return schedules;
  }
}
