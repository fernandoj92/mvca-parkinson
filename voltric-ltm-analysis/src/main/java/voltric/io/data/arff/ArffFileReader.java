package voltric.io.data.arff;

import voltric.data.Data;
import voltric.data.DataInstanceCollection;
import voltric.data.DataInstanceFactory;
import voltric.io.data.DataFileReader;
import voltric.variables.DiscreteVariable;
import voltric.variables.SingularContinuousVariable;
import voltric.variables.Variable;
import voltric.variables.VariableCollection;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

/**
 * Dado que es imposible devolver una clase statica, la clase tendrá un unico constructor (el de por defecto) y resemblará
 * de la forma más parecida posible a un Object en Scala (no usará campos privados)
 */
// TODO: Algun dia implementarlo tambien con InputStream (de momento no merece la pena)
    // TODO: pasarle el Path object a todos los metodos privados (evitar repeticion)
public class ArffFileReader implements DataFileReader {

    public Data readData(String filePathString){

        // Get the Path object from the provided string
        Path pathFile = Paths.get(filePathString);

        try {
            //
            String relationName = getRelationName(pathFile);
            //
            VariableCollection attributes = getAttributes(pathFile);
            //
            DataInstanceCollection instances = getDataInstances(pathFile, attributes);
            //
            return new Data(relationName, attributes, instances);

        } catch (IOException ex) {
            throw new UncheckedIOException(ex);
        }
    }

    private String getRelationName(Path pathFile) throws IOException{

        // Read the relation getName
        Optional<String> atRelation = Files.lines(pathFile)
                .map(String::trim)
                .filter(w -> !w.isEmpty())
                .filter(w -> !w.startsWith("%"))
                .limit(1)
                .filter(line -> line.startsWith("@relation"))
                .findFirst();

        if (!atRelation.isPresent())
            throw new IllegalArgumentException("ARFF file does not start with a @relation line.");

        // Returns the relation
        return atRelation.get();
    }

    private VariableCollection getAttributes(Path pathFile) throws IOException{

        int dataLineCount = getDataLineCount(pathFile);

        List<String> attLines = Files.lines(pathFile)
                .map(String::trim)
                .filter(w -> !w.isEmpty())
                .filter(w -> !w.startsWith("%"))
                .limit(dataLineCount)
                .filter(line -> line.startsWith("@attribute"))
                .collect(Collectors.toList());

        List<Variable> atts = IntStream.range(0,attLines.size())
                .mapToObj( i -> createAttributeFromLine(i, attLines.get(i)))
                .collect(Collectors.toList());

        return new VariableCollection(atts);
    }

    private DataInstanceCollection getDataInstances(Path pathFile, VariableCollection variables) throws IOException{

        int dataLineCount = getDataLineCount(pathFile);

        Stream<String> dataLines =  Files.lines(pathFile)
                .filter(w -> !w.isEmpty())
                .filter(w -> !w.startsWith("%"))
                .skip(dataLineCount)
                .filter(w -> !w.isEmpty());

        int dataLineIndex = 1;
        DataInstanceCollection dataInstances = new DataInstanceCollection();
        Iterator<String> dataLinesIterator = dataLines.iterator();
        while(dataLinesIterator.hasNext()){
            dataInstances.add(DataInstanceFactory.fromArffDataLine(dataLinesIterator.next(), variables, dataLineIndex));
            dataLineIndex++;
        }
        return dataInstances;
    }

    private int getDataLineCount(Path pathFile) throws IOException{

        // Find the @data line
        final int[] count = {0};
        Optional<String> atData = Files.lines(pathFile)
                .map(String::trim)
                .filter(w -> !w.isEmpty())
                .filter(w -> !w.startsWith("%"))
                .peek(line -> count[0]++)
                .filter(line -> line.startsWith("@data"))
                .findFirst();

        if (!atData.isPresent())
            throw new IllegalArgumentException("ARFF file does not contain @data line.");

        return count[0];
    }

    // TODO: El index no se utiliza
    private Variable createAttributeFromLine(int index, String line){
        String[] parts = line.split("\\s+|\t+");

        if (!parts[0].trim().startsWith("@attribute"))
            throw new IllegalArgumentException("Attribute line does not start with @attribute");

        String name = parts[1].trim();
        //getName = StringUtils.strip(getName,"'");

        name = name.replaceAll("^'+", "");
        name = name.replaceAll("'+$", "");

        parts[2]=parts[2].trim();

        if (parts[2].equals("real") || parts[2].equals("numeric")){
            if(parts.length>3 && parts[3].startsWith("[")){
                parts[3]=line.substring(line.indexOf("[")).replaceAll("\t", "");
                double min = Double.parseDouble(parts[3].substring(parts[3].indexOf("[")+1,parts[3].indexOf(",")));
                double max = Double.parseDouble(parts[3].substring(parts[3].indexOf(",")+1,parts[3].indexOf("]")));
                //return new Attribute(index, getName, new RealStateSpace(min,max));
                return new SingularContinuousVariable(name);
            }else
                //return new Attribute(index, getName, new RealStateSpace());
                return new SingularContinuousVariable(name);
        }else if (parts[2].startsWith("{")){
            parts[2]=line.substring(line.indexOf("{")).replaceAll("\t", "");
            String[] states = parts[2].substring(1,parts[2].length()-1).split(",");

            List<String> statesNames = Arrays.stream(states).map(String::trim).collect(Collectors.toList());

            //return new Attribute(index, getName, new FiniteStateSpace(statesNames));
            return new DiscreteVariable(name, statesNames);
        }else{
            throw new UnsupportedOperationException("We can not create an attribute from this line: "+line);
        }

    }
}
