package core.io.data.format;

import javax.swing.filechooser.FileFilter;
import java.io.File;

/**
 * Created by equipo on 15/02/2017.
 */
public class Format implements Comparable<Format> {

    /**
     * Name of format
     */
    private final String name;

    /**
     * Description of format
     */
    private final String description;

    /**
     * Extension of the file, not including the dot.
     */
    private final String extension;

    /**
     * Suffix to the file name based on the extension.
     */
    private final String suffix;

    private final FileFilter filter = new FileFilter() {

        public boolean accept(File file) {
            return file.isDirectory() ? true : hasExtension(file.getName());
        }

        @Override
        public String getDescription() {
            return description;
        }

    };

    Format(String name, String description, String extension) {
        this.name = name;
        this.description = description;
        this.extension = extension;
        this.suffix = "." + extension;
    }

    @Override
    public String toString() {
        return description;
    }

    public int compareTo(Format o) {
        return name.compareTo(o.name);
    }

    /**
     * Returns whether the filename has extension of this format.
     *
     * @param filename
     *            name of the file
     * @return whether the file name has the extension of this format
     */
    public boolean hasExtension(String filename) {
        return filename.endsWith(suffix)
                && filename.length() > suffix.length();
    }
}
