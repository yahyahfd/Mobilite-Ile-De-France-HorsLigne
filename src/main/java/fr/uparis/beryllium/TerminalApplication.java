package fr.uparis.beryllium;
import java.util.Scanner;
/**
 * The Controller is considered the main in our MVC
 */
public class TerminalApplication {

    /**
     * No arguments are needed.
     * Unicodes used for colors here: 
     * Red: "\u001B[31m"
     * Green: "\u001B[32m"
     * Blue: "\u001B[34m"
     * Cyan: "\u001B[36m"
     * Color resetting at the end of each string: "\u001B[0m"
     */
    public static void main(String[] args) {
        Scanner scanner  = new Scanner(System.in);
        System.out.println("\u001B[36mWelcome to our interactive (Terminal Only) program for finding routes.");
        System.out.println("\u001B[36mIf you ever want to leave, just type \u001B[31mquit\u001B[0m");
        while(true){
            System.out.println("\u001B[34m\nLet's check if there is a route for you\u001B[0m");
            System.out.print("\u001B[32mEnter your first station's name: \u001B[0m");
            String station1 = scanner.nextLine();
            if (station1.equalsIgnoreCase("quit")) {
                break;
            }
            System.out.print("\u001B[32mEnter your second station's name: \u001B[0m");
            String station2 = scanner.nextLine();
            String route = null;
            if (station2.equalsIgnoreCase("quit")) {
                break;
            }
            // We'll add verifications here to check if the names are valid (I don't know if it's necessary?)
            // If we add verifications, we'll set station1 or station2's colors to green or red whether they exist or not
            // We add the method (the algorithm) to look for the path
            if(route == null){
                System.out.println("Looks like there is no route to go from \u001B[31m"+station1+"\u001B[0m to \u001B[31m"+station2+"\u001B[0m");
            }
            // else{
            //     System.out.println(route);
            // }
        }
        
        scanner.close();
    }
}
