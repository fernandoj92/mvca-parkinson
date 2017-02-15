package core.data.dataset;

import core.data.Data;
import core.io.data.DataManager;
import core.io.data.Reader;
import core.io.data.Writer;
import core.io.data.format.Format;
import core.io.data.format.FormatCatalog;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.InputStream;

/**
 * A helper class for loading data with different formats into {@code DiscreteDataSet}.
 * It first converts the data to Hlcm format in memory if necessary, and then
 * uses the constructor of {@code DiscreteDataSet} to read the data.
 * 
 * @author leonard
 * 
 */
public class DiscreteDataSetLoader {
	/**
	 * Loads a data set from a file with the given name.
	 * 
	 * @param filename
	 *            name of file to load from
	 * @return a data set loaded from the given file
	 * @throws Exception
	 */
	public static DiscreteDataSet load(String filename) throws Exception {
		try {
			return new DiscreteDataSet(convert(filename));
		} catch (Exception e) {
			return new DiscreteDataSet(filename);
		}
	}

	/**
	 * Loads a data set from a given input stream with the specified format.
	 * 
	 * @param input
	 *            input stream holding the data
	 * @param format
	 *            format of the data
	 * @return data set loaded from the given input stream
	 * @throws Exception
	 */
	public static DiscreteDataSet load(InputStream input, Format format)
			throws Exception {
		return new DiscreteDataSet(convert(input, format));
	}

	/**
	 * Converts a file with the given name into a input stream from which
	 * {@code DiscreteDataSet} can read. It determines the format of the data using the
	 * extension of the file.
	 * 
	 * @param filename
	 *            name of the data file.
	 * @return an input stream from which the {@code DiscreteDataSet} can read
	 * @throws Exception
	 */
	public static InputStream convert(String filename) throws Exception {
		Format format = FormatCatalog.getInputFormat(filename);
		
		if(format == FormatCatalog.HLCM)
		{
			return (new FileInputStream(filename));
		}
		
		return convert(new FileInputStream(filename), format);
	}

	/**
	 * Converts a data in a given input stream in the specified format into a
	 * input stream from which {@code DiscreteDataSet} can read.
	 * 
	 * @param input
	 *            holds the data
	 * @param format
	 *            format of the data
	 * @return an input stream from which the {@code DiscreteDataSet} can read
	 * @throws Exception
	 */
	public static InputStream convert(InputStream input, Format format)
			throws Exception {
		Reader reader = DataManager.createReader(input, format);
		Data data = reader.read();

		ByteArrayOutputStream os = new ByteArrayOutputStream();
		Writer writer = DataManager.createWriter(os, FormatCatalog.HLCM);
		writer.write(data);

		return new ByteArrayInputStream(os.toByteArray());
	}
}
