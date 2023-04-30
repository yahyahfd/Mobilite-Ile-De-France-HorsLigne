package fr.uparis.beryllium.model;

import static org.junit.jupiter.api.Assertions.*;
import java.time.Duration;

import org.junit.jupiter.api.Test;
import fr.uparis.beryllium.exceptions.FormatException;

public class Parser1Test {
    
    @Test
    public void testReadMapWithCorrectCSV() throws FormatException {

        Map map = Parser.readMap("src/test/resources/testCsvParser.csv");
        assertNotNull(map);
        Station firstStation = map.getStations().get(0);
        Station secondStation = map.getStations().get(1);

        Line line = map.getLines().get(0);

        Location firstStationLocation = firstStation.getLocation();
        Location secondStationLocation = secondStation.getLocation();

        Duration duration = firstStation.getNextStations().get(secondStation).get(0).getDuration();
        Double distance = firstStation.getNextStations().get(secondStation).get(0).getDistance();

        assertEquals("Lourmel", firstStation.getName());
        assertEquals("Boucicaut", secondStation.getName());
        assertEquals("8", line.toString());
        assertEquals(2.2822419598550767, firstStationLocation.getLongitude());
        assertEquals(48.83866086365992, firstStationLocation.getLatitude());
        assertEquals(2.2879184311245595, secondStationLocation.getLongitude());
        assertEquals(48.841024160993214, secondStationLocation.getLatitude());
        assertEquals(Duration.ofMillis(41004), duration);
        assertEquals(1.593935780373747, distance);

    }

}