package core.io.data.arff;

import core.data.Data;
import core.io.data.Reader;
import core.io.data.arff.parser.ArffParser;

import java.io.IOException;
import java.io.InputStream;
import java.util.zip.GZIPInputStream;

public class ArffReader implements Reader{

    private final InputStream input;

    /**
     * Constructs a ArffReader with the given input stream
     *
     * @param input
     *            input stream
     * @throws IOException
     *             if an I/O error occurs
     */
    public ArffReader(InputStream input) throws IOException {
        this(input, false);
    }

    /**
     * Constructs a ArffReader, with an option that supports a gzipped input
     * stream.
     *
     * @param input
     *            input stream
     * @param gzipped
     *            whether the input stream is gzipped
     * @throws IOException
     *             if an I/O error occurs
     */
    public ArffReader(InputStream input, boolean gzipped) throws IOException {
        this.input = gzipped ? new GZIPInputStream(input) : input;
    }

    /**
     * Returns the data reading from this reader.
     *
     * @return data reading from this reader
     */
    public Data read() throws Exception {
        return ArffParser.parse(input);
    }

}