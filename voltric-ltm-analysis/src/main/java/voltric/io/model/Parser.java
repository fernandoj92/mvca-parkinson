package voltric.io.model;

import voltric.model.BayesNet;

/**
 * Parser interface.
 * 
 * @author leonard
 * 
 */
public interface Parser {
	// /**
	// * Parses the embedded input stream and
	// * replaces the network content with that in the stream
	// * @param network network to hold the persisted content
	// */
	// void parse(BayesNet network);

	/**
	 * Parses the embedded input stream and returns the network read from the
	 * stream
	 * 
	 * @return network parsed from input stream
	 */
	void parse(BayesNet network) throws CustomParseException;

	/**
	 * Gets the properties found in the network file just read.
	 * 
	 * @return Properties of network, variables, probability definitions.
	 */
	Properties getProperties();
}
