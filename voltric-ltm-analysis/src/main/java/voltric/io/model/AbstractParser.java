package voltric.io.model;


import voltric.model.BayesNet;

public abstract class AbstractParser implements Parser {
	public BayesNet parse() throws CustomParseException {
		BayesNet result = new BayesNet("");
		parse(result);
		return result;
	}
}
