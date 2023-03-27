package fr.uparis.beryllium.model;

import java.io.FileReader;
import java.io.IOException;

import org.apache.commons.csv.CSVFormat;
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
                .setHeader("station1","gps1","station2","gps2","line","h","dist")
                .setSkipHeaderRecord(true)
                .setDelimiter(";")
                .build();

            try{
                Iterable<CSVRecord> records  = format.parse(reader);
                //Creating map 
                for (CSVRecord record : records) {
                    String s1 = record.get("station1");
                    String s2 = record.get("station2");
                    String l[] = record.get("line").split(" ");
                    String gps1[] = (record.get("gps1")).split(",");
                    String gps2[] = (record.get("gps2")).split(",");
                    String h[] = (record.get("h")).split(":");
                    String dist = (record.get("dist"));

                    //Search for the stations and line
                    //If they exist, search function return their object in map's lists
                    //Else, it create a new object, put it in map's lists and return it 
                    Station stat1 = map.searchStation(s1, new Localisation(Double.parseDouble(gps1[1]),Double.parseDouble(gps1[0])));
                    Station stat2 = map.searchStation(s2,new Localisation( Double.parseDouble(gps2[1]),Double.parseDouble(gps2[0])));
                    //Add line with variant in the name
                    Line line = map.searchLine(l[0]+"."+l[2]);

                    //Add station to line's list
                    //addStation verify if the station is already in the list or not
                    line.addStation(stat1);
                    line.addStation(stat2);

                    //Add neighbours
                    stat1.addNextStation(stat2, line,h,Double.parseDouble(dist));
                    stat2.addNextStation(stat1, line, h,Double.parseDouble(dist));
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
