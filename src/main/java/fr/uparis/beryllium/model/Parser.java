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

                    //Add neighbours
                    stat1.addNextStation(stat2, line);
                    stat2.addNextStation(stat1, line);
                }
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
