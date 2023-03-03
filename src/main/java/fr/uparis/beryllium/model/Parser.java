package fr.uparis.beryllium.model;

import java.io.FileReader;
import java.io.IOException;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;

public class Parser {

    /**
     * Static method that is used to parse a file containing
     * the network data (CSV file)
     * 
     * @param file String of the path to the file
     * @return A map of the network described by 
     * @throws FileNotFoundException if the file doesn't exist
     * @throws FormatException if the appropriate format isn't respected
     */
    public static Map readMap(String file){
        // // Example of use of FormatException: (custom exception)
        // try {
        //     throw new FormatException("Bad format (+ligne?)");
        // } catch (FormatException e) {
        // }
            //First try to verify if the file exists
            Map map = new Map();
        try (FileReader reader = new FileReader(file)) {
            //Build format of the csv file
            CSVFormat format = CSVFormat.DEFAULT.builder()
                .setHeader("station1","station2","line")
                .setSkipHeaderRecord(true)
                .build();

            try{
                Iterable<CSVRecord> records  = format.parse(reader);
                //Creating map 
                for (CSVRecord record : records) {
                    String s1 = record.get("station1");
                    String s2 = record.get("station2");
                    String l = record.get("line");

                    //Search for the stations and line
                    //If they exist, search function return their object in map's lists
                    //Else, it create a new object, put it in map's lists and return it 
                    Station stat1 = map.searchStation(s1);
                    Station stat2 = map.searchStation(s2);
                    Line line = map.searchLine(l);

                    //Add station to line's list
                    //addStation verify if the station is already in the list or not
                    line.addStation(stat1);
                    line.addStation(stat2);

                    //J'explique ici en français mon problème, c'est plus simple
                    //Ici, je dois faire maintenant les voisin des station
                    //Problème, c est que voisin c'est Station, Line
                    // Sauf que la ligne se met à jour au fur et à mesure de la lecture donc si je crée le voisin maintenant
                    //les information de la ligne sont peut être encore incomplète
                    //Donc une solution que j'aurai pensé, c'est de crée une liste temporaire genre RappelVoisin
                    // Où je met les deux station et la ligne en question
                    //et à la fin de ma boucle pour record, je recupère les stations et la ligne dans map comme ça les infos sont entièrement à jour
                    // Et là, je créé les voisin 
                    //Autre problème, si je dois faire cette liste qui mélange donc un triplé Station, Station, Line
                    //Je pense direct au map, faire Map<Set<Station>,Line>
                    //Sauf que vu qu'on utilise Map de model, ça pose conflit d'import
                    // Deux import se contredit, le model.Map et le java.util.Map
                    // Donc je sais pas si je cherche trop compliqué et y a plus simple xD
                    // Help ? 

                    //Update line and stations' new informations
                    map.updateLines(line);
                    map.updateStation(stat1);
                    map.updateStation(stat2);
                }

                //Initialize all neighbours TODO
                
            }catch(IllegalArgumentException e){
                // the StackTrace doesn't tell where is the error, only lines of error in our code like line 12 in Controller, 127 in apache csv
                System.out.println("Csv format incorrect, the map is incomplete.");
            }
            
        } catch (IOException e) {
            System.out.println("File name doesn't exists.");
        }

        return map;
    }
}
