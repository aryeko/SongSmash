package com.amitay.arye.songsmash;


import android.net.Uri;
import android.os.Environment;
import android.text.TextUtils;
import java.io.BufferedReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by A&A on 2/21/2017.
 *
 * This class is for handling CSV files
 */

public class CsvHelper {

    private static final String DEFAULT_EXPORT_PATH = Environment.getExternalStorageDirectory().getAbsolutePath() + "/summarizedList.csv";

    /**
     * Constructs a FileWriter object given a file name.
     *
     * @param inputStream  List of String[] that contains song name and status
     * @return List<String[]> that contains the CSV data
     * @throws RuntimeException  If got an error with reading the CSV file
     *                           or while closing the inputStream
     */
    public static List<String[]> readCsv(InputStream inputStream) throws RuntimeException {
        List<String[]> resultList = new ArrayList<>();
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));

        try {
            String csvLine;
            while((csvLine = reader.readLine()) != null) {
                String[] row = csvLine.split(",");
                resultList.add(row);
            }
        } catch(IOException ex) {
            throw new RuntimeException("Error in reading CSV file:" + ex);
        } finally {
            try{
                inputStream.close();
            } catch(IOException e) {
                throw new RuntimeException("Error while closing input stream: " + e);
            }
        }
        return resultList;
    }

    /**
     * Constructs a FileWriter object given a file name.
     *
     * @param inputList  List of String[] that contains song name and status
     * @param targetFilePath  Set a custom file path - use null for default
     * @return Target file path
     * @throws IOException  if the named file exists but is a directory rather
     *                  than a regular file, does not exist but cannot be
     *                  created, or cannot be opened for any other reason
     */
    public static String writeToCsv(List<String[]> inputList, String targetFilePath) throws IOException {

        targetFilePath = targetFilePath == null ? DEFAULT_EXPORT_PATH : targetFilePath;

        FileWriter writer = new FileWriter(targetFilePath);

        for(String[] csvRow : inputList){
            writer.append(TextUtils.join(",", csvRow) + "\n");
        }

        writer.flush();
        writer.close();

        return targetFilePath;
    }

    /**
     * Constructs a FileWriter object given a file name.
     *
     * @param uri  Uri object with file details
     * @return file real path
     */
    public static String getFilePath(Uri uri) {
        if ("content".equalsIgnoreCase(uri.getScheme())) {
            //will return:  primary:<file path inside the sdcard>
            return Environment.getExternalStorageDirectory().getAbsolutePath() + "/" + uri.getLastPathSegment().split(":")[1];
        }
        if ("file".equalsIgnoreCase(uri.getScheme())) {
            return uri.getPath();
        }

        return null;
    }
}
