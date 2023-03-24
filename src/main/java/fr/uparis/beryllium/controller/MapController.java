package fr.uparis.beryllium.controller;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import fr.uparis.beryllium.model.*;

@RestController
public class MapController {

  /**
   * This method allows us to send an HTTP GET request to get all stations
   * @return a list of all stations
   */
  @GetMapping("/stations")
  public List<Station> getStations() {
    Map m = Parser.readMap("map_data.csv");
    List<Station> stations = m.getStations();

    return stations;
  }

}

