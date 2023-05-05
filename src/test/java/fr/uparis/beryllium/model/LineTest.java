package fr.uparis.beryllium.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class LineTest {

    @Test
    public void testGetName() {
        Line line = new Line("8.1");
        assertEquals("8.1", line.getName());
    }

    @Test
    public void testGetNameWithoutVariant() {
        Line line = new Line("8.1");
        assertEquals("8", line.getLineNameWithoutVariant());
    }

    @Test
    public void testToString() {
        Line line = new Line("8.1");
        assertEquals("8", line.toString());
    }
}
