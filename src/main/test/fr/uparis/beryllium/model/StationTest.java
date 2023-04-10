package fr.uparis.beryllium.model;

import org.junit.jupiter.api.Test;

import ch.qos.logback.core.util.Duration;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

class StationTest {

    @Test
    void isWithinA1KmRadius() {
        // Given
        Station station1 = new Station("Félix Faure", new Localisation(48.84268433479664, 2.2918472203679703));
        Station station2 = new Station("Commerce", new Localisation(48.84461151236847, 2.293796842192864));

        // When
        boolean result = station1.isWithinARadius(station2, 1);

        // Assert
        assert (result);
    }

    @Test
    void isNotWithinA1KmRadius() {
        // Given
        Station station1 = new Station("Boucicaut", new Localisation(48.841024160993214, 2.2879184311245595));
        Station station2 = new Station("Opéra", new Localisation(48.87059812248669, 2.332135072925294));
        
        boolean result = station1.isWithinARadius(station2, 1);

        // Assert
        assert (!result);
    }

    @Test
    void addWalkingNeighbours() {
        // Given
        Line walkingLine = new Line("--MARCHE--");
        Station station1 = new Station("République", new Localisation(48.86742161002706, 2.363149406900382));
        Station voisin1 = new Station("Filles du Calvaire", new Localisation(48.8630698834507, 2.366745297742701));
        Station voisin2 = new Station("Saint-Sébastien - Froissart", new Localisation(48.8609681457564, 2.3672615397172687));
        Station voisin3 = new Station("République", new Localisation(48.86748513277203, 2.3632563242479026));
        ArrayList<Station> liste = new ArrayList<>(List.of(voisin1, voisin2, voisin3));
        
        // When
        station1.addWalkingNeighbours(walkingLine, liste, 1, false);
        
        // Assert
        assert (station1.getNextStations().keySet().containsAll(liste));
        assert (station1.getNextStations().get(voisin1).get(0).getLine().equals(walkingLine));
        assert (station1.getNextStations().get(voisin2).get(0).getLine().equals(walkingLine));
        assert (station1.getNextStations().get(voisin3).get(0).getLine().equals(walkingLine));
    }
    
    @Test
    void removeWalkingNeighbours() {
        // Given
        Line line = new Line("--MARCHE--");
        Station station1 = new Station("Félix Faure", new Localisation(48.84268433479664, 2.2918472203679703));
        Station station2 = new Station("Commerce", new Localisation(48.84461151236847, 2.293796842192864));
        Station station3 = new Station("Opéra", new Localisation(48.87059812248669, 2.332135072925294));
        ArrayList<Station> allStations = new ArrayList<>(List.of(station1, station2, station3));
        Map<Station, ArrayList<NeighborData>> nextS = new HashMap<>();
        nextS.put(station2, new NeighborData(line, new Duration(1), 10.0));
        nextS.put(station3, new NeighborData(line, new Duration(1), 10.0));
        station1.nextStation = nextS;
        
        // When
        station1.removeWalkingNeighbours(line, allStations, 1);
        
        // Assert
        assert (station1.getNextStations().keySet().contains(station3));
        assert (!station1.getNextStations().keySet().contains(station2));
    }
}