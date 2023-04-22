package fr.uparis.beryllium.model;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;
import fr.uparis.beryllium.exceptions.FormatException;

public class Parser5Test {
    
    @Test
    public void testReadMapHoraireWithIncorrectCsv() {
        assertThrows(FormatException.class, () -> {
            Map map = Parser.readMap("src/test/resources/testCsvParser.csv");
            Parser.readMapHoraire("src/test/resources/testCsvHoraireIncorrect.csv", map);
        });
    }


}