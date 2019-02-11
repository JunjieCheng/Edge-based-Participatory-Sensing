package Util;

import java.io.File;
import java.io.IOException;

public class Util {

    public static void createFiles(String taskName) throws IOException {
        // Create file for saving data
        // Temp target file: save periodical target data. Cleaned after uploading
        // Temp incentive file: save periodical price and accepted data. Cleaned after uploading
        // All data file: save data of [target, price, accepted, data]
        File tempTargetFile = new File("./src/data/" + taskName + "_target.temp");
        File tempIncentiveFile = new File("./src/data/" + taskName + "_incentive.temp");
        File dataFile = new File("./src/data/" + taskName + ".dat");

        if (tempTargetFile.createNewFile()) {
            System.out.println("Temp target file created");
        } else {
            System.out.println("Error: cannot create temp target file");
        }

        if (tempIncentiveFile.createNewFile()) {
            System.out.println("Temp incentive file created");
        } else {
            System.out.println("Error: cannot create temp incentive file");
        }

        if (dataFile.createNewFile()) {
            System.out.println("Data file created");
        } else {
            System.out.println("Error: cannot create data file");
        }
    }
}
