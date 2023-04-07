package fr.uparis.beryllium.model;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

class MapTest {

    @Test
    void walkToBestStation() {
        // Given
        Map m = Parser.readMap("map_data.csv");
        Station start = new Station("LocalPosition", new Localisation(2.3483939, 48.8811423));
        Station dest = (m.getStationsByName("Châtelet")).get(0);

        // When
        m.walkToBestStation(start, dest);

        // Assert
        assert (start.getDistanceToAStation(dest).equals(/*radius*/));
        assert (start.getNextStations().size().equals(/*nb voisins*/));

        Itinerary i = new Itinerary(m.getAllStations());
        HashMap<Station, Line> path = i.shortestWay(start, dest, 0);
        ArrayList<Station> station = new ArrayList<>(path.keySet());
        assertEquals("Gare du Nord", station.get(1).getName());
        
    }
}