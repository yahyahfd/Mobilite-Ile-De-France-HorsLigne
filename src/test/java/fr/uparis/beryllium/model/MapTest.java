package fr.uparis.beryllium.model;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Map testing class
 * @see Map
 */
class MapTest {

    /**
     * Test for walkToBestStation
     */
    @Test
    void walkToBestStation() {
        Line walkingLine = new Line("--MARCHE--");
        Map m = Map.getMapInstance();
        m.addStation(48.84268433479670,2.2928472203679453, "LocalPosition");
        m.addStation(48.84268433479664, 2.2918472203679703, "Félix Faure");
        m.addStation(48.84461151236847, 2.293796842192864, "Commerce");
        m.addStation(48.87059812248669, 2.332135072925294, "Opéra");

        Station start = m.getStations().get(0);
        Station station1 = m.getStations().get(1);
        Station dest = m.getStations().get(2);
        
        m.walkToBestStation(start, true, dest.getLocation());

        System.out.println(start.getNextStations().keySet());
        assertEquals(2, start.getNextStations().size());
        assert (start.getNextStations().get(station1).get(0).getLine().getName().equals(walkingLine.getName()));
        assert (start.getNextStations().get(dest).get(0).getLine().getName().equals(walkingLine.getName()));
    }
}
