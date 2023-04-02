package fr.uparis.beryllium.model;

import org.junit.jupiter.api.Test;

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
}