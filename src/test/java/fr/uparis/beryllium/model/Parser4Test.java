package fr.uparis.beryllium.model;

import static org.junit.jupiter.api.Assertions.*;
import java.time.LocalTime;
import org.junit.jupiter.api.Test;
import fr.uparis.beryllium.exceptions.FormatException;

public class Parser4Test {

    @Test
    public void testReadMapHoraireWithCorrectCsv() throws FormatException {
        Map map = Parser.readMap("src/test/resources/testCsvParser.csv");
        map = Parser.readMapHoraire("src/test/resources/testCsvHoraire.csv", map);
        assertNotNull(map);
        Line line = map.searchLine("8.1");
        LocalTime time = LocalTime.of(10, 42);

        assertEquals(time, line.getStationTimes(map.getStationByName("Lourmel")).get(0));
    }
}