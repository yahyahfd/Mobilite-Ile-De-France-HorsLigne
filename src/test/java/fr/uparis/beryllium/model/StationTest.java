package fr.uparis.beryllium.model;

import org.junit.jupiter.api.Test;
import java.util.ArrayList;
import java.util.List;

/**
 * Station testing class
 * 
 * @see Station
 */
import static org.junit.jupiter.api.Assertions.assertEquals;

class StationTest {

    /**
     * Test for radius method (is in 1km radius)
     */
    @Test
    void isWithinA1KmRadius() {
        // Given
        Station station1 = new Station("Félix Faure", new Location(48.84268433479664, 2.2918472203679703));
        Station station2 = new Station("Commerce", new Location(48.84461151236847, 2.293796842192864));

        // When
        boolean result = station1.isWithinARadius(1,station2.getLocation());

        // Assert
        assertEquals(true, result);
    }

    @Test
    void isNotWithinA1KmRadius() {
        // Given
        Station station1 = new Station("Boucicaut", new Location(48.841024160993214, 2.2879184311245595));
        Station station2 = new Station("Opéra", new Location(48.87059812248669, 2.332135072925294));


        boolean result = station1.isWithinARadius(1,station2.getLocation());

        // Assert
        assertEquals(false, result);
    }

    // faux/modifier add walkingneighbors
    @Test
    void addWalkingNeighbours() {
        // Given
        Line walkingLine = new Line("--MARCHE--");
        Station station1 = new Station("République", new Location(48.86742161002706, 2.363149406900382));
        Station voisin1 = new Station("Filles du Calvaire", new Location(48.8630698834507, 2.366745297742701));
        Station voisin2 = new Station("Saint-Sébastien - Froissart", new Location(48.8609681457564, 2.3672615397172687));
        Station voisin3 = new Station("République", new Location(48.86748513277203, 2.3632563242479026));
        ArrayList<Station> liste = new ArrayList<>(List.of(voisin1, voisin2, voisin3));

        // When
        station1.addWalkingNeighbours(walkingLine, liste, 1, false);

        // Assert
        assertEquals(station1.getNextStations().keySet().containsAll(liste), true);
        assertEquals(station1.getNextStations().get(voisin1).get(0).getLine().equals(walkingLine), true);
        assertEquals(station1.getNextStations().get(voisin2).get(0).getLine().equals(walkingLine), true);
        assertEquals(station1.getNextStations().get(voisin3).get(0).getLine().equals(walkingLine), true);
    }

}
