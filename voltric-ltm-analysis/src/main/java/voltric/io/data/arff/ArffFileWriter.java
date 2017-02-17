package voltric.io.data.arff;

import voltric.data.Data;
import voltric.data.DataInstance;
import voltric.io.data.DataFileWriter;
import voltric.variables.DiscreteVariable;
import voltric.variables.StateSpaceType;
import voltric.variables.Variable;
import voltric.variables.VariableCollection;

import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

// En comparacion con el reader donde no podemos devolver una clase a modo de Object en Scala, para el escribir no
// tenemos que seleccionar el output con un loader como al leer, por lo que si es facil de hacer los métodos estaticos.
public class ArffFileWriter implements DataFileWriter {

    public void writeToFile(Data data, String filePathString) throws IOException{
        FileWriter fw = new FileWriter(filePathString);

        // Writes the ARFF @relation line that identifies the Data
        fw.write("@relation " + data.getName()+ "\n\n");

        // Writes the ARFF attributes
        for (Variable att : data.getVariables()){
            fw.write(attributeToArffString(att)+"\n");
        }

        // Writes the ARFF data instances
        fw.write("\n\n@data\n\n");

        for(DataInstance instance :data.getInstances())
            fw.write(dataInstanceToARFFString(data.getVariables(), instance));

        // Closes the file
        fw.close();
    }

    private String attributeToArffString(Variable attribute){
        if(attribute.getStateSpaceType() == StateSpaceType.REAL)
            return "@attribute " + attribute.getName() + " real";
        else if(attribute.getStateSpaceType() == StateSpaceType.FINITE) {
            StringBuilder stringBuilder = new StringBuilder("@attribute " + attribute.getName() + " {");
            DiscreteVariable discreteAttribute = (DiscreteVariable) attribute;
            List<String> attributeStates = discreteAttribute.getStates();

            // Append all the variable states minus the last one
            attributeStates
                    .stream()
                    .limit(discreteAttribute.getStates().size() - 1)
                    .forEach(e -> stringBuilder.append(e + ", "));

            // Append the last state
            stringBuilder.append(attributeStates.get(attributeStates.size() - 1) + "}");

            return stringBuilder.toString();
        }
        else
            throw new IllegalArgumentException("Unknown SateSapaceType");
    }

    private String dataInstanceToARFFString(VariableCollection atts, DataInstance dataInstance){
        StringBuilder builder = new StringBuilder();

        // Append all the columns of the DataInstance with  the separator except the last one
        for(int i=0; i<atts.size()-1;i++) {
            Variable att = atts.get(i);
            builder.append(dataInstanceToARFFString(att,dataInstance, atts, ","));
        }

        // Append the last column of the data instance
        Variable att = atts.get(atts.size()-1);
        builder.append(dataInstanceToARFFString(att,dataInstance, atts, ""));

        return builder.toString();
    }

    // TODO: quizas es mejor hacerlo todo en el mismo metodo en vez de llmar a un metodo extra con varios appends???
    private String dataInstanceToARFFString(Variable att, DataInstance dataInstance, VariableCollection variables, String separator) {
        StringBuilder builder = new StringBuilder();
        if(dataInstance.getNumericValue(variables.indexOf(att)) == Double.NaN)
            // Value is MISSING
            builder.append("?" + separator);
        else if (att.getStateSpaceType() == StateSpaceType.FINITE){
            builder.append(dataInstance.getTextualValue(variables.indexOf(att)) + separator);
        } else if (att.getStateSpaceType() == StateSpaceType.REAL){
            builder.append(dataInstance.getTextualValue(variables.indexOf(att)) + separator);
        } else
            throw new IllegalArgumentException("Illegal state space type of Attribute: " + att.getStateSpaceType());

        return builder.toString();
    }


}
