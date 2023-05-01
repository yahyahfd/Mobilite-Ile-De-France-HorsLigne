package fr.uparis.beryllium.model;

/**
 * Our line class. Used to stock a line object that links
 * a station to its neighbor.
 */
public class Line {

    /**
     * Name of the lane: "number.variant".
     * 
     * @see Parser#readMap(String) details on syntax here
     */
    private String lineName;

    /**
     * Constructor of a line.
     * 
     * @param name line name
     */
    public Line(String name) {
        lineName = name;
    }

    /**
     * Getter for lineName.
     * 
     * @return <code>lineName</code>
     */
    public String getName() {
        return lineName;
    }

    public String getLineNameWithoutVariant() {
        return (!lineName.equals("--MARCHE--")) ? lineName.split("\\.")[0] : "--MARCHE--";
    }

    /**
     * Method used to return the linename without the variant.
     * Example: "1.1" -> "1".
     * 
     * @return lineName without variant name
     */
    public String toString() {
        return lineName.split("\\.")[0];
    }

}

