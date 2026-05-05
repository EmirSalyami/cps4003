package librarysystem;

import java.util.Scanner;

public class Main {

    public static void main(String[] args) {
        Scanner scan = null;
        scan = new Scanner(System.in);

        int numChoice = 0;

        try {
            DatabaseHelper.makeDatabaseReady();
        } catch (Exception ex) {
            System.out.println("Database connection failed.");
            System.out.println(ex.getMessage());
            scan.close();
            return;
        }

        System.out.println("St Mary Digital Library System");
        System.out.println("Choose mode:");
        System.out.println("1 = Console menus");
        System.out.println("2 = Graphical user interface");

        numChoice = 0;
        if (scan.hasNextInt()) {
            numChoice = scan.nextInt();
        } else {
            scan.nextLine();
        }
        scan.nextLine();

        if (numChoice == 2) {
            scan.close();
            LibraryGui.launchGui();
            return;
        }

        if (numChoice != 1) {
            System.out.println("Wrong choice. Using console menus.");
        }

        ConsoleApp.runConsole(scan);
        scan.close();
    }
}
