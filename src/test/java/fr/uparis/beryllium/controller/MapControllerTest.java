package fr.uparis.beryllium.controller;

import fr.uparis.beryllium.exceptions.FormatException;
import fr.uparis.beryllium.model.*;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletResponse;

import java.io.UnsupportedEncodingException;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;


@ExtendWith(MockitoExtension.class)
public class MapControllerTest {


    static private MapController mapController;

    @BeforeAll
    public static void setUp() throws FormatException {
        mapController = new MapController();
    }

    @Test
    public void testGetStations() throws FormatException {

        List<Station> stations = mapController.getStations();

        assertNotNull(stations);
        assertEquals(712, stations.size());
    }

    @Test
    public void testShortestWay() throws FormatException, UnsupportedEncodingException {

        HttpServletResponse httpServletResponseMock = new MockHttpServletResponse();

        mapController.shortestWay("Gare de lyon", "Bercy", 1, "10:00", httpServletResponseMock);

        assertEquals(200, httpServletResponseMock.getStatus());

        assertEquals(true,
                ((MockHttpServletResponse) httpServletResponseMock)
                        .getContentAsString()
                        .startsWith("{\"timeTotal\":4,\"startingTime\":[10,0],\"distTotal\":2.9669736164790885,\"endingTime\":[10,4]"));

    }

    @Test
    public void testGetSchedules() throws FormatException {
        List<String> schedules = mapController.getSchedules("Bercy", "6");
        assertNotNull(schedules);
        assertEquals(2691, schedules.size());
    }


}
