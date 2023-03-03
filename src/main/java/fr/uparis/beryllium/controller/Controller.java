package fr.uparis.beryllium.controller;

import fr.uparis.beryllium.model.*;
/**
 * The Controller is considered the main in our MVC
 */
public class Controller {

    public static void main(String[] args) {
        //Test for parser, to be deleted later
       Map m = Parser.readMap("test.csv");
       m.display();
       //Test for parser, to be deleted later
    }
}