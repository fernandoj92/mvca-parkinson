package org.latlab.io.io;

import org.latlab.io.ParseException;
import org.latlab.io.Parser;
import org.latlab.model.BayesNet;

public abstract class AbstractParser implements Parser {
	public BayesNet parse() throws ParseException {
		BayesNet result = new BayesNet("");
		parse(result);
		return result;
	}
}
