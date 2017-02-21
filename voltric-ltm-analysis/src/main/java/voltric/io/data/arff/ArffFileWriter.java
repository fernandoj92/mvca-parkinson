package voltric.io.data.arff;

import voltric.data.Data;
import voltric.data.DataInstance;
import voltric.io.data.DataFileWriter;
import voltric.variables.*;

import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

// En comparacion con el reader donde no podemos devolver una clase a modo de Object en Scala, para el escribir no
// tenemos que seleccionar el output con un loader como al leer, por lo que si es facil de hacer los métodos estaticos.
public class ArffFileWriter implements DataFileWriter {

    public <V extends IVariable> void writeToFile(Data<V> data, String filePathString) throws IOException{
        FileWriter fw = new FileWriter(filePathString);

        // Writes the ARFF @relation line that identifies the Data
        fw.write("@relation " + data.getName()+ "\n\n");

        // Writes the ARFF attributes
        for (V att : data.getVariables()){
            fw.write(attributeToArffString(att)+"\n");
        }

        // Writes the ARFF data instances
        fw.write("\n\n@data\n\n");

        for(DataInstance<V> instance :data.getInstances())
            fw.write(dataInstanceToARFFString(data.getVariables(), instance));

        // Closes the file
        fw.close();
    }

    private <V extends IVariable> String attributeToArffString(V attribute){
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

    private <V extends IVariable> String dataInstanceToARFFString(VariableCollection<V> atts, DataInstance<V> dataInstance){
        StringBuilder builder = new StringBuilder();

        // Append all the columns of the DataInstance with  the separator except the last one
        for(int i=0; i<atts.size()-1;i++) {
            V att = atts.get(i);
            builder.append(dataInstanceToARFFString(att,dataInstance, atts, ","));
        }

        // Append the last column of the data instance
        V att = atts.get(atts.size()-1);
        builder.append(dataInstanceToARFFString(att,dataInstance, atts, ""));

        return builder.toString();
    }

    // TODO: quizas es mejor hacerlo todo en el mismo metodo en vez de llmar a un metodo extra con varios appends???
    private <V extends IVariable> String dataInstanceToARFFString(V att, DataInstance<V> dataInstance, VariableCollection<V> variables, String separator) {
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
