package fr.uparis.beryllium.model;


import static org.junit.jupiter.api.Assertions.*;

import java.time.Duration;

import org.junit.jupiter.api.Test;
import fr.uparis.beryllium.exceptions.FormatException;

public class ParserTest {

    @Test
    public void testReadMapWithCorrectCSV() throws FormatException {

        Map map = Parser.readMap("src/test/resources/testCsvParser.csv");
        assertNotNull(map);
        Station firstStation = map.getStations().get(0);
        Station secondStation = map.getStations().get(1);

        Line line = map.getLines().get(0);

        Localisation firstStationLocalisation = firstStation.getLocalisations().get(line.getLineNameWithoutVariant());
        Localisation secondStationLocalisation = secondStation.getLocalisations().get(line.getLineNameWithoutVariant());

        Duration duration = firstStation.getNextStations().get(secondStation).get(0).getDuration();
        Double distance = firstStation.getNextStations().get(secondStation).get(0).getDistance();

        assertEquals("Lourmel", firstStation.getName());
        assertEquals("Boucicaut", secondStation.getName());
        assertEquals("8", line.getLineNameWithoutVariant());
        assertEquals(2.2822419598550767, firstStationLocalisation.getLongitude());
        assertEquals(48.83866086365992, firstStationLocalisation.getLatitude());
        assertEquals(2.2879184311245595, secondStationLocalisation.getLongitude());
        assertEquals(48.841024160993214, secondStationLocalisation.getLatitude());
        assertEquals(Duration.ofMillis(41004), duration);
        assertEquals(1.593935780373747, distance);

    }

    @Test
    public void testReadMapWithEmptyCSV() throws FormatException {
        Map map = Parser.readMap("src/test/resources/testCsvParserEmpty.csv");
        assertNotNull(map);
        assertEquals(0, map.getLines().size());
        assertEquals(0, map.getStations().size());
    }

    @Test
    public void testReadMapWithIncorrectCSV() {
        assertThrows(FormatException.class, () -> Parser.readMap("src/test/resources/testCsvParserIncorrect.csv"));
    }
}