package fr.uparis.beryllium.model;


import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

import fr.uparis.beryllium.exceptions.FormatException;

public class Parser2Test {
   
    @Test
    public void testReadMapWithEmptyCSV() throws FormatException {
        Map map = Parser.readMap("src/test/resources/testCsvParserEmpty.csv");
        assertNotNull(map);
        assertEquals(0, map.getLines().size());
        assertEquals(0, map.getStations().size());
    }
}