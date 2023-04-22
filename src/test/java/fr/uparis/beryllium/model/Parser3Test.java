package fr.uparis.beryllium.model;


import static org.junit.jupiter.api.Assertions.*;


import org.junit.jupiter.api.Test;

import fr.uparis.beryllium.exceptions.FormatException;

public class Parser3Test {

    @Test
    public void testReadMapWithIncorrectCSV() {
        assertThrows(FormatException.class, () -> {
            Parser.readMap("src/test/resources/testCsvParserIncorrect.csv");
        });
    }


}