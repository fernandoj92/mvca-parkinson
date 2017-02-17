package voltric.data.dataset;

import voltric.data.DataInstance;
import voltric.variables.VariableCollection;

/**
 * This class provides an implementation for data cases.
 *
 * @author Yi Wang
 *
 */
public class DiscreteDataCase implements Comparable<DiscreteDataCase> {
    /**
     * the data set to which this data case belongs.
     */
    private DiscreteDataSet _dataSet;

    /**
     * the array of states of this data case.
     */
    private int[] _states;

    /**
     * the weight of this data case.
     */
    private double _weight;

    /**
     * Constructs a data case with the specified data set that contains it,
     * and the specified states and weight.
     *
     * @param dataSet
     *            data set that contains this data case.
     * @param states
     *            states of this data case.
     * @param weight
     *            weight of this data case.
     */
    public DiscreteDataCase(DiscreteDataSet dataSet, int[] states, double weight) {
        _dataSet = dataSet;
        _states = states;
        _weight = weight;
    }

    /**
     * <p>
     * Compares this data case with the specified object for order.
     * </p>
     *
     * <p>
     * If the specified object is not a data case, this method will throw a
     * <code>ClassCastException</code> (as data cases are comparable only
     * to other data cases). Otherwise, the comparison is carried out based
     * on the states of two data cases.
     * </p>
     *
     * @param object
     *            the object to be compared.
     * @return a negative or a positive integer if the states of this data
     *         case procedes or succeeds that of the specified data case;
     *         zero if their states are identical.
     */
    public int compareTo(DiscreteDataCase object) {
        DiscreteDataCase dataCase = object;

        // two data cases must belong to the same data set
        assert _dataSet == dataCase.getDataSet();

        for (int i = 0; i < _states.length; i++) {
            if (_states[i] < dataCase._states[i]) {
                return -1;
            } else if (_states[i] > dataCase._states[i]) {
                return 1;
            }
        }

        return 0;
    }

    /**
     * Returns the states of this data case.
     *
     * @return the states of this data case
     */
    public int[] getStates() {
        return _states;
    }

    /**
     * Returns the weight of this data case.
     *
     * @return the weight of this data case.
     */
    public double getWeight() {
        return _weight;
    }

    /**
     * Updates the weight of this data case.
     *
     * @param weight
     *            new weight of this data case.
     */
    public void setWeight(double weight) {
        // weight must be positive
        assert weight > 0.0;

        _weight = weight;
    }

    /**
     * Returns its corresponding DataSet.
     *
     * @return
     */
    public DiscreteDataSet getDataSet(){
        return _dataSet;
    }

    public DataInstance toDataInstance(){
        double[] instanceValues = new double[_states.length];
        for(int i=0; i<instanceValues.length;i++)
            instanceValues[i] = (double) _states[i];

        return new DataInstance(new VariableCollection(this._dataSet.getVariableList()), instanceValues);
    }

    /**
     * Simply return a line containing the states of the DiscreteDataCase
     *
     * @return
     */
    public String toString(){
        String s = "";

        for(int index = 0; index < _states.length; index++) {
            if(index < _states.length - 1)
                s += _states[index];
            else
                s += _states[index] + ", ";
        }

        return s;
    }
}