package core.io.data.format;

import javax.swing.filechooser.FileFilter;
import java.io.File;
import java.util.Arrays;
import java.util.Collections;
import java.util.SortedSet;
import java.util.TreeSet;

/**
 * Created by equipo on 15/02/2017.
 */
public class FormatCatalog {

    public final static Format ARFF =
            new Format("ARFF", "Weka ARFF format", "arff");
    public final static Format ARFF_GZ =
            new Format("ARFF_GZ", "Gzipped Weka ARFF File", "arff.gz");
    public final static Format CSV =
            new Format("CSV", "Comma Separated Value (CSV) Document", "csv");
    public final static Format HLCM = new Format("HLCM", "HLCM format", "txt");

    public final static SortedSet<Format> INPUT_FORMATS =
            Collections.unmodifiableSortedSet(new TreeSet<Format>(Arrays.asList(
                    ARFF, ARFF_GZ, CSV, HLCM)));

    public final static SortedSet<Format> OUTPUT_FORMATS =
            Collections.unmodifiableSortedSet(new TreeSet<Format>(Arrays.asList(
                    ARFF, HLCM)));

    public static Format getInputFormat(String filename) {
        for (Format format : INPUT_FORMATS) {
            if (format.hasExtension(filename))
                return format;
        }

        return null;
    }

    public static Format getOutputFormat(String filename) {
        for (Format format : OUTPUT_FORMATS) {
            if (format.hasExtension(filename))
                return format;
        }

        return null;
    }
}
