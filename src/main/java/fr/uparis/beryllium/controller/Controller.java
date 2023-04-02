package fr.uparis.beryllium.controller;
import fr.uparis.beryllium.exceptions.FormatException;
import fr.uparis.beryllium.model.*;


/**
 * The Controller is considered the main in our MVC
 */
public class Controller {

    public static void main(String[] args) throws FormatException {
       Map m = Parser.readMap("map_data.csv");
    }
}