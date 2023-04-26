package fr.uparis.beryllium.model;

public class Line {

    private String lineName;

    Line(String name) {
        lineName = name;
    }

    public String getName() {
        return lineName;
    }

    public String getLineNameWithoutVariant() {
        return lineName.split("\\.")[0];
    }

    public String toString() {
        return getLineNameWithoutVariant();
    }
}

