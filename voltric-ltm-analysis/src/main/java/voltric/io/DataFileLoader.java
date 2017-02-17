package voltric.io;

import voltric.data.Data;
import voltric.io.arff.ArffFileReader;

import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;

/**
 * Created by Fernando on 2/16/2017.
 */
public class DataFileLoader {

    public static Data loadData(String filePathString){
        try {
            return selectDataFileReader(filePathString).readData(filePathString);
        }catch(IOException ex){
            throw new UncheckedIOException(ex);
        }
    }

    private static DataFileReader selectDataFileReader(String filePathString){
        if(new File(filePathString).isDirectory())
            throw new IllegalArgumentException("The path refers to a directory, which is not supported yet");

        // TODO: de forma simple, si termina en ARFF, es arff reader y palante
        String[] parts = filePathString.split("\\.");
        String fileExtension = parts[parts.length - 1];
        if(fileExtension.equals("arff"))
            return new ArffFileReader();
        else
            throw new IllegalArgumentException("File extension not supported");
    }
}