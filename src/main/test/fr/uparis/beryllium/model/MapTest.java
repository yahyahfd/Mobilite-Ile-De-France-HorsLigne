package fr.uparis.beryllium.model;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

class MapTest {

    @Test
    void walkToBestStation() {
        // Given
        Station start = new Station("LocalPosition", new Localisation(2.3483939, 48.8811423));
        Station  station1 = new Station("Félix Faure", new Localisation(48.84268433479664, 2.2918472203679703));
        Station dest = new Station("Commerce", new Localisation(48.84461151236847, 2.293796842192864));
        Station station2 = new Station("Opéra", new Localisation(48.87059812248669, 2.332135072925294));
        ArrayList<Station> allStations = new ArrayList<>(List.of(station1, station2, start, dest));
        

        // When
        m.walkToBestStation(start, dest, true);

        // Assert
        assert (start.getNextStations().size().equals(2));
        assert (start.getNextStations().get(station1).get(0).getLine().equals(walkingLine));
        assert (station1.getNextStations().get(dest).get(0).getLine().equals(walkingLine));
        assert (station1.getNextStations().get(station2).get(0).getLine().notequals(walkingLine));
    }
}