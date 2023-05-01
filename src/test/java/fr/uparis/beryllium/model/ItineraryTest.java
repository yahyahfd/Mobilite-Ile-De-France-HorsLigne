package fr.uparis.beryllium.model;

import fr.uparis.beryllium.exceptions.FormatException;
import org.apache.commons.lang3.tuple.MutableTriple;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.Test;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
public class ItineraryTest {

    private static final Logger LOGGER = LogManager.getLogger(ItineraryTest.class);

    @Test
    public void testItineraryConstructor(){
        ArrayList<Station> stations = new ArrayList<>();
        stations.add(new Station("start",new Location(0, 0)));
        stations.add(new Station("start",new Location(0, 1)));
        stations.add(new Station("start",new Location(0, 2)));
        stations.add(new Station("start",new Location(0, 3)));

        stations.add(new Station("station1",new Location(2, 2)));
        stations.add(new Station("station2",new Location(1, 2)));
        stations.add(new Station("station3",new Location(2, 3)));

        stations.add(new Station("goal",new Location(10, 10)));
        stations.add(new Station("goal",new Location(9, 10)));
        stations.add(new Station("goal",new Location(8, 10)));
        stations.add(new Station("goal",new Location(11, 10)));
        
        Itinerary itinerary = new Itinerary(stations);
        assertNotNull(itinerary, "Itinerary wasn't initialized correctly");
        assertEquals(stations,itinerary.getAllStations());
    }

    @Test
    public void testShortestMultiplePaths() throws FormatException {
        Map map = Parser.readMap("src/test/resources/testCsvItinerary.csv");
        map = Parser.readMapHoraire("newtimetables.csv", map);
        ArrayList<Station> olympiades = map.getStationsByName("Olympiades");
        ArrayList<Station> bercys = map.getStationsByName("Bercy");
        ArrayList<Station> cstEmils = map.getStationsByName("Cour Saint-Emilion");
        assertEquals(1, olympiades.size());
        assertEquals(1, bercys.size());
        assertEquals(2, cstEmils.size());
        LocalTime timeWeLeft = LocalTime.of(10, 0, 0);
        Itinerary itinerary = new Itinerary(map.getStations());

        //Test with equal weight
        HashMap<Station, Line> path_1 = itinerary.shortestMultiplePaths(olympiades,bercys, 1, timeWeLeft);
        HashMap<Station, MutableTriple<Double,Integer,Long>>  distCountTime = itinerary.getDistCountTimeToStart();
        assertNotNull(path_1);

        //List is reverse, a first element is the end of the path found
        LOGGER.info(itinerary.showPath(path_1, timeWeLeft));
        
        ArrayList<Station> stations_1 = new ArrayList<>(path_1.keySet());
        assertEquals(3, distCountTime.get(stations_1.get(0)).getMiddle());

        assertEquals("Bercy", stations_1.get(0).getName());
        assertEquals("Cour Saint-Emilion", stations_1.get(1).getName());
        assertEquals("Bibliothèque François Mitterrand", stations_1.get(2).getName());
        assertEquals("Olympiades", stations_1.get(3).getName());

        //Test with preference dist
        HashMap<Station, Line> path_2 = itinerary.shortestMultiplePaths(olympiades, bercys, 0, timeWeLeft);
        distCountTime = itinerary.getDistCountTimeToStart();
        assertNotNull(path_2);

        //List is reverse, a first element is the end of the path found
        LOGGER.info(itinerary.showPath(path_2, timeWeLeft));

        ArrayList<Station> stations_2 = new ArrayList<>(path_2.keySet());
        assertEquals(8.598552886278775, distCountTime.get(stations_2.get(0)).getLeft());

        //Test with preference time
        HashMap<Station, Line> path_3 = itinerary.shortestMultiplePaths(olympiades,bercys, 2, timeWeLeft);
        distCountTime = itinerary.getDistCountTimeToStart();
        assertNotNull(path_3);

        //List is reverse, a first element is the end of the path found
        LOGGER.info(itinerary.showPath(path_3, timeWeLeft));

        ArrayList<Station> stations_3 = new ArrayList<>(path_3.keySet());
        assertEquals(400008, distCountTime.get(stations_3.get(0)).getRight());
    }
}