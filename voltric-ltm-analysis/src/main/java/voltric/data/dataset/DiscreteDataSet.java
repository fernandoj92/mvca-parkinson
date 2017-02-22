
package voltric.data.dataset;

import cern.jet.random.Uniform;
import voltric.data.Data;
import voltric.data.DataInstance;
import voltric.model.BayesNet;
import voltric.util.Algorithm;
import voltric.variables.DiscreteVariable;
import voltric.variables.StateSpaceType;
import voltric.variables.Variable;

import java.io.*;
import java.text.NumberFormat;
import java.util.*;

/**
 * This class provides an implementation for data sets.
 * 
 * @author Yi Wang
 * 
 */
public final class DiscreteDataSet {
	
	/**
	 * the constant for missing value.
	 */
	public final static int MISSING_VALUE = -1;

	/**
	 * the prefix of default names of data sets.
	 */
	private final static String NAME_PREFIX = "DiscreteDataSet";

	/**
	 * the number of created data sets.
	 */
	private static int _count = 0;

	/**
	 * the getName of this data set.
	 */
	protected String _name;

	/**
	 * the array of getVariables involved in this data set.
	 */
	private DiscreteVariable[] _variables;

    /**
     *
     */
    private Map<String, DiscreteVariable> variableNameMap;

	/**
	 * the list of distinct data cases. we use <code>ArrayList</code> for
	 * random access.
	 */
	private ArrayList<DiscreteDataCase> _data;

	/**
	 * the total weight, namely, number of data cases, of this data set.
	 */
	private double _totalWeight;

	/**
	 * the flag that indicates whether this data set contains missing values.
	 */
	private boolean _missing;

	/**
	 * Reads the data set(HLTM format) from the specified input stream.
	 * 
	 * @param stream
	 *            input stream of the data set
	 * @throws IOException
	 *             thrown when an IO error occurs
	 */
	public DiscreteDataSet(InputStream stream) throws IOException {
		StreamTokenizer tokenizer = new StreamTokenizer(new BufferedReader(
				new InputStreamReader(stream, "UTF8")));

		tokenizer.resetSyntax();

		// characters that will be ignored
		tokenizer.whitespaceChars(':', ':');
		tokenizer.whitespaceChars(' ', ' ');
		tokenizer.whitespaceChars('\t', '\t');

		// word characters
		tokenizer.wordChars('A', 'z');
		tokenizer.wordChars('\'', '\'');
		tokenizer.wordChars('(', '(');
		tokenizer.wordChars(')', ')');
		tokenizer.wordChars(']', ']');
		tokenizer.wordChars('[', '[');
		tokenizer.wordChars('-', '-');
		tokenizer.wordChars('0', '9');

		// we will parse numbers
		tokenizer.parseNumbers();

		// treats eol as a token
		tokenizer.eolIsSignificant(true);

		// ignores c++ comments
		tokenizer.slashSlashComments(true);

		NumberFormat nf = NumberFormat.getInstance();
		nf.setMinimumFractionDigits(0);

		// starts parsing
		int value = StreamTokenizer.TT_EOF;

		// getName of data set
		do {
			value = tokenizer.nextToken();
		} while (value != StreamTokenizer.TT_WORD);

		do {
			value = tokenizer.nextToken();
		} while (value != StreamTokenizer.TT_WORD);
		_name = tokenizer.sval;

		// reads getVariables
		ArrayList<DiscreteVariable> variables = new ArrayList<DiscreteVariable>();

		while (true) {
			// goes to the next word or number
			do {
				value = tokenizer.nextToken();
			} while (!(value == StreamTokenizer.TT_WORD || value == StreamTokenizer.TT_NUMBER));
			
			// if we see a number, we have read all getVariables
			if (value == StreamTokenizer.TT_NUMBER) {
				tokenizer.pushBack();
				break;
			}

			// we have at least one more variable to read
			String name = tokenizer.sval;
			
			// read state names
			ArrayList<String> states = new ArrayList<String>();
			do {
				value = tokenizer.nextToken();
				if (value == StreamTokenizer.TT_WORD) {
					states.add(tokenizer.sval);
				} else if (value == StreamTokenizer.TT_NUMBER) {
					states.add(nf.format(tokenizer.nval));
				}
			} while (value != StreamTokenizer.TT_EOL);

			variables.add(new DiscreteVariable(name, states));
		}

		// enforces order of getVariables
		_variables = variables.toArray(new DiscreteVariable[variables.size()]);
		Arrays.sort(_variables);

		// reads data cases
		_data = new ArrayList<DiscreteDataCase>();
		while (true) {
			// goes to the next number
			do {
				value = tokenizer.nextToken();
			} while (value != StreamTokenizer.TT_EOF
					&& value != StreamTokenizer.TT_NUMBER);

			// we have at least one more data case
			if (value == StreamTokenizer.TT_NUMBER) {
				tokenizer.pushBack();
			} else {
				break;
			}

			// reads states
			int[] states = new int[getDimension()];
			for (int i = 0; i < getDimension(); i++) {
				do {
					value = tokenizer.nextToken();
				} while (value != StreamTokenizer.TT_NUMBER);
				states[i] = (int) tokenizer.nval;
			}

			// reads weight
			do {
				value = tokenizer.nextToken();
			} while (value != StreamTokenizer.TT_NUMBER);
			double weight = tokenizer.nval;

			// adds data case
			addDataCase(states, weight);

			do {
				value = tokenizer.nextToken();
			} while (value != StreamTokenizer.TT_EOL
					&& value != StreamTokenizer.TT_EOF);

			if (value == StreamTokenizer.TT_EOF) {
				break;
			}
		}

		_count++;

        // Extra: VariableNameMap for quick access using its name
        variableNameMap = new HashMap<>();
        for(DiscreteVariable variable: _variables)
            if(variableNameMap.get(variable.getName()) != null)
                throw new IllegalArgumentException("Variable names should not be repeated");
            else
                variableNameMap.put(variable.getName(), variable);
	}

	/**
	 * Constructs a data set defined by the specified data file.
	 * 
	 * @param file
	 *            The getName of the specified data file.
	 * @throws IOException
	 *             if an I/O error occurs.
	 */
	public DiscreteDataSet(String file) throws IOException {
			this(new FileInputStream(file));
	}

	/**
	 * Constructs an empty data set of the specified array of getVariables.
	 * 
	 * @param variables
	 *            array of getVariables to be involved.
	 */
	public DiscreteDataSet(DiscreteVariable[] variables) {
		// default getName
		_name = NAME_PREFIX + _count++;

		// Added by Chen Tao
		_variables = variables;

		// enforces order of getVariables
	    Arrays.sort(_variables);

		_data = new ArrayList<DiscreteDataCase>();

        // Extra: VariableNameMap for quick access using its name
        variableNameMap = new HashMap<>();
        for(DiscreteVariable variable: _variables)
            if(variableNameMap.get(variable.getName()) != null)
                throw new IllegalArgumentException("Variable names should not be repeated");
            else
                variableNameMap.put(variable.getName(), variable);
        _count++;
	}
 
	public DiscreteDataSet(Data<DiscreteVariable> data){

		// Checks if there are any non-discrete variables in the Data
		for(Variable variable : data.getVariables())
			if(variable.getStateSpaceType() != StateSpaceType.FINITE)
				throw new IllegalArgumentException("Only discrete variables are allowed in a discrete dataset");

		// Set the name of the DataSet
		this._name = data.getName();

		// Set the variables
		List<DiscreteVariable> discreteVariablesList = data.getVariables();

		// TODO: the list is transformed into an array to avoid refactoring more code
		this._variables = new DiscreteVariable[discreteVariablesList.size()];
		for(int i=0; i < discreteVariablesList.size(); i++)
			this._variables[i] = discreteVariablesList.get(i);

		// Enforces an order for the variables
		Arrays.sort(this._variables);

		// reads the data cases
        this._data = new ArrayList<DiscreteDataCase>();

		for(int i=0; i<data.getInstances().size(); i++)
		{
			// reads the instance numeric values
			DataInstance ins = data.getInstances().get(i);
			double[] instanceDoubleValues = ins.getNumericValues();
			// Transform the values from Double to Int (discrete dataSet)
			int[] instanceIntValues = new int[ins.getNumericValues().length];
			for(int k=0; k < ins.getNumericValues().length; k++)
				instanceIntValues[k] = (int) instanceDoubleValues[k];

			// adds the instance as a new data case or adds 1 to the weight ot its data case
			addDataCase(instanceIntValues, 1);
		}

		_count++;

        // Extra: VariableNameMap for quick access using its name
        variableNameMap = new HashMap<>();
        for(DiscreteVariable variable: _variables)
            if(variableNameMap.get(variable.getName()) != null)
                throw new IllegalArgumentException("Variable names should not be repeated");
            else
                variableNameMap.put(variable.getName(), variable);
	}

	/**
	 * Constructs an empty data set of the specified array of getVariables.
	 * 
	 * @param variables
	 *            array of getVariables to be involved.
	 *        flag
	 *            decide whether to enforce the order. If following the order given, flag is false
	 */
	public DiscreteDataSet(DiscreteVariable[] variables, boolean flag) {
		// default getName
		_name = NAME_PREFIX + _count++;

		// Added by Chen Tao
		_variables = variables;

		// enforces order of getVariables
	   if(flag){
		   Arrays.sort(_variables);
	   }

		_data = new ArrayList<DiscreteDataCase>();

        // Extra: VariableNameMap for quick access using its name
        variableNameMap = new HashMap<>();
        for(DiscreteVariable variable: _variables)
            if(variableNameMap.get(variable.getName()) != null)
                throw new IllegalArgumentException("Variable names should not be repeated");
            else
                variableNameMap.put(variable.getName(), variable);
	}
	/**
	 * Adds the specified data case with the specified weight to this data set.
	 * 
	 * @param states
	 *            data case to be added to this data set.
	 * @param weight
	 *            weight of the data case.
	 */
	public void addDataCase(int[] states, double weight) {
		DiscreteDataCase dataCase = new DiscreteDataCase(this, states, weight);

		// finds the position for this data case
		int index = Collections.binarySearch(_data, dataCase);

		if (index < 0) {
			// adds unseen data case
			_data.add(-index - 1, dataCase);

			// updates missing value flag
			for (int state : states) {
				_missing |= (state == MISSING_VALUE);
			}
		} else {
			// increases weight for existing data case
			dataCase = _data.get(index);
			dataCase.setWeight(dataCase.getWeight() + weight);
		}

		// updates total weight
		_totalWeight += weight;
	}

	public double SearchDataCase(int[] states) {
		DiscreteDataCase dataCase = new DiscreteDataCase(this, states, 1.0);

		// finds the position for this data case
		int index = Collections.binarySearch(_data, dataCase);
		
		dataCase = _data.get(index);

		return dataCase.getWeight();
	}

    /**
     * This method is a workaround to get the indexes of the getInstances the DataCases represent.
     *
     * @param originalDataFile
     * @return
     * @throws IOException
     */
	public int[] getDatacaseIndex(String originalDataFile) throws IOException {
		if(!(originalDataFile.endsWith("arff")))
		{
			System.out.println("The data is not arff format.Please check.");
			System.exit(1);
		}
		
		int[] index = new int[(int) _totalWeight];
		
		StreamTokenizer tokenizer = new StreamTokenizer(new BufferedReader(
				new InputStreamReader(new FileInputStream(originalDataFile), "UTF8")));

		tokenizer.resetSyntax();

		// characters that will be ignored
		tokenizer.whitespaceChars(':', ':');
		tokenizer.whitespaceChars(' ', ' ');
		tokenizer.whitespaceChars('\t', '\t');
		tokenizer.whitespaceChars('{', '{');
		tokenizer.whitespaceChars('}', '}');
		tokenizer.whitespaceChars(',', ',');

		// word characters
		tokenizer.wordChars('A', 'z');
		tokenizer.wordChars('\'', '\'');
		tokenizer.wordChars('(', '(');
		tokenizer.wordChars(')', ')');
		tokenizer.wordChars(']', ']');
		tokenizer.wordChars('[', '[');
		tokenizer.wordChars('-', '-');
		tokenizer.wordChars('0', '9');
		tokenizer.wordChars('@', '@');

		// we will parse numbers
		tokenizer.parseNumbers();

		// treats eol as a token
		tokenizer.eolIsSignificant(true);

		// ignores c++ comments
		tokenizer.slashSlashComments(true);
		
		// ignores arff comments
		tokenizer.commentChar('%');

		NumberFormat nf = NumberFormat.getInstance();
		nf.setMinimumFractionDigits(0);

		// starts parsing
		int value = StreamTokenizer.TT_EOF;

		// getName of data set
		do {
			value = tokenizer.nextToken();
		} while (value != StreamTokenizer.TT_WORD);

		do {
			value = tokenizer.nextToken();
		} while (value != StreamTokenizer.TT_EOL);
//		_name = tokenizer.sval;

		// reads getVariables
		ArrayList<DiscreteVariable> variables = new ArrayList<DiscreteVariable>();

		while (true) {
			
			//skip the word "@attribute", "@data"
			do {
				value = tokenizer.nextToken();
			} while (value != StreamTokenizer.TT_WORD);
			
			// goes to the next word or number
			do {
				value = tokenizer.nextToken();
			} while (!(value == StreamTokenizer.TT_WORD || value == StreamTokenizer.TT_NUMBER));

			// if we see a number, we have read all getVariables
			if (value == StreamTokenizer.TT_NUMBER) {
				tokenizer.pushBack();
				break;
			}

			// we have at least one more variable to read
			String name = tokenizer.sval;

			// read state names
			ArrayList<String> states = new ArrayList<String>();
			do {
				value = tokenizer.nextToken();
				if (value == StreamTokenizer.TT_WORD) {
					states.add(tokenizer.sval);
				} else if (value == StreamTokenizer.TT_NUMBER) {
					states.add(nf.format(tokenizer.nval));
				}
			} while (value != StreamTokenizer.TT_EOL);

			variables.add(new DiscreteVariable(name, states));
		}

		// enforces order of getVariables
//		_variables = getVariables.toArray(new DiscreteVariable[getVariables.size()]);
//		Arrays.sort(_variables);

		// reads data cases
//		_data = new ArrayList<DiscreteDataCase>();
		int dataIndex = 0;
		while (true) {
			// goes to the next number
			do {
				value = tokenizer.nextToken();
			} while (value != StreamTokenizer.TT_EOF
					&& value != StreamTokenizer.TT_NUMBER);

			// we have at least one more data case
			if (value == StreamTokenizer.TT_NUMBER) {
				tokenizer.pushBack();
			} else {
				break;
			}

			// reads states
			int[] states = new int[getDimension()];
			for (int i = 0; i < getDimension(); i++) {
				do {
					value = tokenizer.nextToken();
				} while (value != StreamTokenizer.TT_NUMBER);
				states[i] = (int) tokenizer.nval;
			}

			// reads weight
			double weight = 1.0;
			
			DiscreteDataCase dataCase = new DiscreteDataCase(this, states, weight);
			index[dataIndex++] = Collections.binarySearch(_data, dataCase);

			do {
				value = tokenizer.nextToken();
			} while (value != StreamTokenizer.TT_EOL
					&& value != StreamTokenizer.TT_EOF);

			if (value == StreamTokenizer.TT_EOF) {
				break;
			}
		}

//		_count++;

		return index;
		
	}
	
	/**
	 * Returns the list of distinct data cases in this data set.
	 * 
	 * @return the list of distinct data cases in this data set.
	 */
	public ArrayList<DiscreteDataCase> getData() {
		return _data;
	}

	/**
	 * Returns the dimension, namely, the number of getVariables, of this data set.
	 * 
	 * @return the dimension of this data set.
	 */
	public int getDimension() {
		return _variables.length;
	}

	/**
	 * Returns the getName of this data set.
	 * 
	 * @return the getName of this data set.
	 */
	public String getName() {
		return _name;
	}

	/**
	 * Returns the number of distinct data cases in this data set.
	 * 
	 * @return the number of distinct data cases in this data set.
	 */
	public int getNumberOfEntries() {
		return _data.size();
	}

	/**
	 * Returns the total weight, namely, the number of data cases, of this data
	 * set.
	 * 
	 * @return the total weight of this data set.
	 */
	public double getTotalWeight() {
		return _totalWeight;
	}

	/**
	 * Returns the array of getVariables involved in this data set.
	 * 
	 * @return the array of getVariables involved in this data set.
	 */
	public DiscreteVariable[] getVariables() {
		return _variables;
	}

	public List<DiscreteVariable> getVariableList(){
		return Arrays.asList(_variables);
	}

	/**
	 * Returns <code>true</code> if this data set contains missing values.
	 * 
	 * @return <code>true</code> if this data set contains missing values.
	 */
	public boolean hasMissingValues() {
		return _missing;
	}

	/** Added by Peixian Chen
	 * Returns a data set that is the projection of this data set on the
	 * specified list of getVariables.
	 * 
	 * @param variables
	 *            list of getVariables onto which this data set is to be project.
	 * @return a projected data set on the specified list of getVariables(You can choose whether to enforce order).
	 */
	public DiscreteDataSet project(ArrayList<DiscreteVariable> variables, boolean flag) {
		// array representation
		DiscreteVariable[] varArray = variables.toArray(new DiscreteVariable[variables.size()]);

		// you can choose whether to enforce  order of getVariables
		DiscreteDataSet dataSet = new DiscreteDataSet(varArray, flag);

		// maps argument getVariables to that in this data set
		int dimension = variables.size();
		int[] map = new int[dimension];
		
	/*	for (int i = 0; i < dimension; i++) {
			map[i] = Arrays.binarySearch(_variables, varArray[i]);

			// argument variable must be involved in this data set
			asser map[i] >= 0;
		}
    */
		////////////////////////////////////////////////////////////////
		for(int i = 0; i < dimension; i++)
		{
			for(int j = 0; j < _variables.length; j++)
			{
				if(_variables[j].getName().compareTo(varArray[i].getName()) == 0)
				{
					map[i] = j;
				}
			}
		}
		/////////////////////////////////////////////////////////////////
		
		// projection
		// int[] projectedStates = new int[dimension];
		for (DiscreteDataCase dataCase : _data) {
			int[] projectedStates = new int[dimension];

			int[] states = dataCase.getStates();
			for (int i = 0; i < dimension; i++) {
				projectedStates[i] = states[map[i]];
			}

			dataSet.addDataCase(projectedStates, dataCase.getWeight());
		}

		return dataSet;
	}
	
	/**
	 * Count the frequencies of every variable
	 * Author Peixian Chen
	 **/
	public Map<DiscreteVariable,Double> getFreq(){
		Map<DiscreteVariable,Double> Frequencies = new HashMap<DiscreteVariable,Double>();
		for (DiscreteDataCase dataCase : _data) {
			int[] states = dataCase.getStates();
			for(int i = 0; i<_variables.length;i++){
				if(!Frequencies.containsKey(_variables[i])){
					Frequencies.put(_variables[i], states[i]*dataCase.getWeight());
				}else{
				Double cnt = Frequencies.get(_variables[i])+states[i]*dataCase.getWeight();
				Frequencies.put(_variables[i], cnt);
				}
			}
		}
		return Frequencies;
		
	}
	
	
	/**
	 * Returns a data set that is the projection of this data set on the
	 * specified list of getVariables.
	 * 
	 * @param variables
	 *            list of getVariables onto which this data set is to be project.
	 * @return a projected data set on the specified list of getVariables(enforced order).
	 */
	public DiscreteDataSet project(ArrayList<DiscreteVariable> variables) {
		// array representation
		DiscreteVariable[] varArray = variables.toArray(new DiscreteVariable[variables.size()]);

		// order of getVariables will be enforced in the constructor
		DiscreteDataSet dataSet = new DiscreteDataSet(varArray);

		// maps argument getVariables to that in this data set
		int dimension = variables.size();
		int[] map = new int[dimension];
		
	/*	for (int i = 0; i < dimension; i++) {
			map[i] = Arrays.binarySearch(_variables, varArray[i]);

			// argument variable must be involved in this data set
			asser map[i] >= 0;
		}
    */
		////////////////////////////////////////////////////////////////
		for(int i = 0; i < dimension; i++)
		{
			for(int j = 0; j < _variables.length; j++)
			{
				if(_variables[j].getName().compareTo(varArray[i].getName()) == 0)
				{
					map[i] = j;
				}
			}
		}
		/////////////////////////////////////////////////////////////////
		
		// projection
		// int[] projectedStates = new int[dimension];
		for (DiscreteDataCase dataCase : _data) {
			int[] projectedStates = new int[dimension];

			int[] states = dataCase.getStates();
			for (int i = 0; i < dimension; i++) {
				projectedStates[i] = states[map[i]];
			}

			dataSet.addDataCase(projectedStates, dataCase.getWeight());
		}

		return dataSet;
	}

	/**
	 * Outputs this data set to the specified file.
	 * 
	 * @param file
	 *            output of this BN.
	 * @throws FileNotFoundException
	 *             if the file exists but is a directory rather than a regular
	 *             file, does not exist but cannot be created, or cannot be
	 *             opened for any other reason.
	 * @throws UnsupportedEncodingException
	 */
	public void save(String file) throws FileNotFoundException, UnsupportedEncodingException {
		PrintWriter out = new PrintWriter(new BufferedWriter(
				new OutputStreamWriter(new FileOutputStream(file), "UTF8")));

		// outputs header
		out.println("// " + file);
//		out.println("// Produced by org.latlab at "
//				+ ((System.currentTimeMillis())));

		// outputs getName
		out.println("Name: " + _name);
		out.println();

		// outputs getVariables
		out.println("// " + getDimension() + " getVariables");
		for (DiscreteVariable variable : _variables) {
			// variable getName
			out.printf(variable.getName() + ":");

			// state names
			for (String state : variable.getStates()) {
				out.print(" " + state);
			}

			out.println();
		}
		out.println();

		// outputs data cases
		out.println("// " + getNumberOfEntries()
				+ " distinct data cases with total weight " + getTotalWeight());
		for (DiscreteDataCase dataCase : _data) {
			for (int state : dataCase.getStates()) {
				out.print(state + " ");
			}

			// outputs weight
			out.println(dataCase.getWeight());
		}

		out.close();
	}

	/**
	 * Outputs this data set to the specified file in ARFF format.
	 * 
	 * @param file
	 *            output of this BN.
	 *        useStatesName
	 *            use the states getName or the index of states in each datacase
	 * @throws FileNotFoundException
	 *             if the file exists but is a directory rather than a regular
	 *             file, does not exist but cannot be created, or cannot be
	 *             opened for any other reason.
	 * @throws UnsupportedEncodingException
	 */
	public void saveAsArff(String file, boolean useStatesName) throws FileNotFoundException, UnsupportedEncodingException {
		PrintWriter out = new PrintWriter(new BufferedWriter(
				new OutputStreamWriter(new FileOutputStream(file), "UTF8")));

		// output relation
		out.println("@relation " + _name);
		out.println();

		// output attributes
		int dim = getDimension();
		out.println("% " + dim + " getVariables");

		for (DiscreteVariable variable : _variables) {
			// output getName
			out.print("@attribute " + variable.getName());

			// output domain
			out.print(" {");
			for (int k = 0; k < variable.getCardinality(); k++) {
				
				if(useStatesName)
				{
					out.print(variable.getStates().get(k));
				}else
				{
					out.print(k);
				}
				
				if (k < variable.getCardinality() - 1) {
					out.print(",");
				}
			}
			out.println("}");
		}

		out.println();

		// output data cases
		out.println("% " + getNumberOfEntries() + " distinct records, "
				+ getTotalWeight() + " in total");
		out.println("@data");

		for (DiscreteDataCase datum : _data) {
			StringBuffer states = new StringBuffer();

			for (int i = 0; i < dim; i++) {
				int state = datum.getStates()[i];

				if (state == -1) {
					states.append('?');
				} else {
					
					if(useStatesName)
					{
						states.append(_variables[i].getStates().get(state));
					}else
					{
						states.append(state);
					}
				}

				if (i < dim - 1) {
					states.append(',');
				}
			}

			int nCopies = (int) datum.getWeight();
			for (int i = 0; i < nCopies; i++) {
				out.println(states);
			}
		}

		out.close();
	}

	/**
	 * Replaces the getName of this data set.
	 * 
	 * @param name
	 *            new getName of this data set.
	 */
	public void setName(String name) {
		name = name.trim();

		// getName cannot be blank
		asser name.length() > 0;

		_name = name;
	}

	/**
	 * Returns a data set of the common getVariables shared by this data set and
	 * the specified BN. The new data set use the DiscreteVariables in BayesNet.
	 * 
	 * @param bayesNet
	 *            BN on which a data set is to be returned.
	 * @return a data set of the common getVariables shared by this data set and
	 *         the specified BN.
	 * @suppressWarning("Unchecked")
	 */
	public DiscreteDataSet synchronize(BayesNet bayesNet) {

		// common getVariables
		ArrayList<DiscreteVariable> variables = new ArrayList<DiscreteVariable>();
		// The map from a common DiscreteVariable(in BN) to the index in the original
		// DiscreteDataSet.
		HashMap<DiscreteVariable, Integer> map = new HashMap<DiscreteVariable, Integer>();

		for (int i = 0; i < getDimension(); i++) {
			for (DiscreteVariable variable : bayesNet.getVariables()) {
				if (_variables[i].equals(variable)) {
					map.put(variable, i);
					variables.add(variable);
					break;
				}
			}
		}

		DiscreteDataSet dataSet = new DiscreteDataSet((DiscreteVariable[]) variables
				.toArray(new DiscreteVariable[variables.size()]));

		// projection
		int dimension = dataSet.getDimension();
		for (DiscreteDataCase dataCase : _data) {
			int[] projectedStates = new int[dimension];
			int[] states = dataCase.getStates();
			for (int i = 0; i < dimension; i++) {
				projectedStates[i] = states[map.get(dataSet._variables[i])];
			}
			dataSet.addDataCase(projectedStates, dataCase.getWeight());
		}

		return dataSet;
	}

	/**
	 * Generate the training data of the given size. The original data becomes
	 * the testing data.
	 * 
	 * @param nOfTraining
	 * @return
	 */
	public DiscreteDataSet splitIntoTrainingAndTesting(int nOfTraining) {
		DiscreteDataSet training = new DiscreteDataSet(_variables);
		for (int i = 0; i < nOfTraining; i++) {
			double total = getTotalWeight();
			double random = Uniform.staticNextDoubleFromTo(0.0, total);
			double accumulator = 0.0;
			// Find the datacase to be moved
			for (DiscreteDataCase data : getData()) {
				double weight = data.getWeight();
				accumulator += weight;
				if (accumulator > random) {
					training.addDataCase(data.getStates(), 1.0);
					if (weight == 1.0) {
						getData().remove(data);
					} else {
						data.setWeight(weight - 1.0);
					}
					_totalWeight = _totalWeight - 1.0;
					break;
				}
			}
		}
		return training;
	}

	/**
	 * Generate the training data of the given size from the current data. The
	 * method is sample from the current data with replacement. No side effect
	 * on the input.
	 * 
	 * @param nOfTraining
	 * @return
	 */
	public DiscreteDataSet sampleWithReplacement(int nOfTraining) {
		DiscreteDataSet training = new DiscreteDataSet(_variables);
		for (int i = 0; i < nOfTraining; i++) {
			double total = getTotalWeight();
			double random = Uniform.staticNextDoubleFromTo(0.0, total);
			double accumulator = 0.0;
			// Find the datacase to be moved
			for (DiscreteDataCase data : getData()) {
				double weight = data.getWeight();
				accumulator += weight;
				if (accumulator > random) {
					training.addDataCase(data.getStates(), 1.0);
					break;
				}
			}
		}
		return training;
	}

	/**
	 * Creates a map from variable to its index used in this data set.
	 * 
	 * @return a map from variable to its index
	 */
	public Map<DiscreteVariable, Integer> createVariableToIndexMap() {
		return Algorithm.createIndexMap(_variables);
	}
	
	public DiscreteDataSet SampleWithOverSampling(DiscreteVariable Lable) {
		DiscreteDataSet AfterSample = new DiscreteDataSet(getVariables());
		
		ArrayList<DiscreteVariable> TargetVar = new ArrayList<DiscreteVariable>(1);
		TargetVar.add(Lable);
		
		DiscreteDataSet TargetSet = project(TargetVar);
		
		int Car= Lable.getCardinality();
			
		int[] Count = new int[Car];
		int MaxWeight = 0;
		int MaxState = 0;
		
		for(DiscreteDataCase d : TargetSet.getData())
		{
			int state = d.getStates()[0];
			int weight = (int)d.getWeight();
			
			Count[state]= weight; 
			
			if(weight > MaxWeight)
			{
				MaxWeight = weight;
				MaxState = state;
			}
		}
		
		for(DiscreteDataCase d : getData())
		{
			AfterSample.addDataCase(d.getStates(), d.getWeight());		
		}
		
		for(int i=0; i < Car; i++)
		{
			if(i == MaxState) continue;
			
			int[] map = new int[Count[i]];
			
			int size = getData().size();
			
			int index = 0;
			
			
			for(int j = 0; j < size; j++)
			{
				int LableIndex = Arrays.binarySearch(getVariables(), Lable);
				
				DiscreteDataCase d = getData().get(j);
				
				if(d.getStates()[LableIndex] == i)
				{
					for(int k = 0; k < (int)d.getWeight(); k++)
					{
						map[index++] = j;
					}
				}
			}
			
			Random random = new Random();
			
			for(int j = 0; j < (MaxWeight-Count[i]); j++)
			{
				int randomIndex = random.nextInt(Count[i]);
				DiscreteDataCase d = getData().get(map[randomIndex]);
				AfterSample.addDataCase(d.getStates(), 1.0);
			}
		}
			
		return AfterSample;		
	}

	public DiscreteVariable getVariable(String name){
        return variableNameMap.get(name);
    }
}