package voltric.data;

import voltric.variables.VariableCollection;

/**
 * Created by Fernando on 2/15/2017.
 */
public class Data {

    private String name;

    private DataInstanceCollection instances = new DataInstanceCollection();

    private VariableCollection variables = new VariableCollection();

    public Data(String name, VariableCollection variables, DataInstanceCollection instances) {
        this.variables = variables;
        this.instances = instances;
    }

    public Data(VariableCollection variables, DataInstanceCollection instances) {
        this.name = "data";
        this.variables = variables;
        this.instances = instances;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    /**
     * @return Returns the variables.
     */
    public VariableCollection getVariables() {
        return variables;
    }

    /**
     * @return Returns the instances.
     */
    public DataInstanceCollection getInstances() {
        return instances;
    }
}
