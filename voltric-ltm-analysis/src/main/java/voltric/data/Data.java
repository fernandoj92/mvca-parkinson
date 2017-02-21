package voltric.data;

import voltric.variables.IVariable;
import voltric.variables.VariableCollection;

/**
 * Created by Fernando on 2/15/2017.
 */
public class Data<A extends IVariable> {

    private String name;

    private DataInstanceCollection<A> instances = new DataInstanceCollection<A>();

    private VariableCollection<A> variables = new VariableCollection<A>();

    public Data(String name, VariableCollection<A> variables, DataInstanceCollection<A> instances) {
        this.variables = variables;
        this.instances = instances;
    }

    public Data(VariableCollection<A> variables, DataInstanceCollection<A> instances) {
        this.name = "data";
        this.variables = variables;
        this.instances = instances;
    }

    //TODO just for test
    public Data(){
        this.name = "data";
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
    public VariableCollection<A> getVariables() {
        return variables;
    }

    /**
     * @return Returns the instances.
     */
    public DataInstanceCollection<A> getInstances() {
        return instances;
    }
}
