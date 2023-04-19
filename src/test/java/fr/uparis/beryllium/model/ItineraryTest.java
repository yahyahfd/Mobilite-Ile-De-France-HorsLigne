package fr.uparis.beryllium.model;

import static org.junit.jupiter.api.Assertions.*;
import java.util.ArrayList;
import java.util.HashMap;

import org.apache.commons.lang3.tuple.MutablePair;
import org.junit.jupiter.api.Test;

import ch.qos.logback.classic.Logger;
import fr.uparis.beryllium.exceptions.FormatException;
import org.apache.logging.log4j.LogManager;
public class ItineraryTest {

    private static final org.apache.logging.log4j.Logger LOGGER = LogManager.getLogger(ItineraryTest.class);

    @Test
    public void testShortestWay() throws FormatException  {
        Map map = Parser.readMap("src/test/resources/testCsvItinerary.csv");
        Station olympiade = map.getStations().get(0);
        Station bercy = map.getStations().get(3);
        Itinerary itinerary = new Itinerary(map.getAllStations());
        //Test with equal weight
        HashMap<Station, Line> path_1 = itinerary.shortestWay(olympiade,bercy, 2);
        //List is reverse, first element is the end of the path found
        LOGGER.info(itinerary.showPath(path_1));
        ArrayList<Station> stations_1 = new ArrayList<>(path_1.keySet());
        assertEquals(4, stations_1.size());
        assertEquals("Cour Saint-Emilion", stations_1.get(1).getName());


        //Test with preference dist
        HashMap<Station, Line> path_2 = itinerary.shortestWay(olympiade,bercy, 0);
          HashMap<Station, MutablePair<Double,Double>> timeDist = itinerary.getDistTime();
        //Find a way to calculate dist total
        ArrayList<Station> stations_2 = new ArrayList<>(path_2.keySet());
        assertEquals(85.98552886278773,timeDist.get(stations_2.get(0)).getLeft());


        //Test with preference time
        HashMap<Station, Line> path_3 = itinerary.shortestWay(olympiade,bercy, 1);
        timeDist = itinerary.getDistTime();
        ArrayList<Station> stations_3 = new ArrayList<>(path_3.keySet());
        assertEquals(1328.0, timeDist.get(stations_3.get(0)).getRight());
    }

    @Test
    public void testShortestDist() throws FormatException  {
        Map map = Parser.readMap("src/test/resources/testCsvItinerary.csv");
        Itinerary itinerary = new Itinerary(map.getAllStations());
        Station station1 = map.getStations().get(0);
        Station station2 = map.getStations().get(3);
        Station station3 = map.getStations().get(3);

        HashMap<Station, MutablePair<Double,Double>> listStart = new HashMap<>();
        listStart.put(station1, new MutablePair<>(12.0, 4.0));
        listStart.put(station3, new MutablePair<>( 15.7,2.4));
        listStart.put(station2, new MutablePair<>( 10.1, 2.5));
        itinerary.distTimeToStart = listStart;
        ArrayList<Station> notVisited = new ArrayList<>();
        notVisited.add(station1);
        notVisited.add(station2);
        notVisited.add(station3);

        itinerary.shortestDist(notVisited,0);
        assertEquals(station2, itinerary.shortestDist(notVisited,0));
        assertEquals(station3, itinerary.shortestDist(notVisited,1));
    }

}