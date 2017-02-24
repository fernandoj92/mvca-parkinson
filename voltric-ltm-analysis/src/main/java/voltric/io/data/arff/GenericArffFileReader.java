package voltric.io.data.arff;

import voltric.data.Data;
import voltric.data.DataInstanceCollection;
import voltric.data.DataInstanceFactory;
import voltric.io.data.DataFileReader;
import voltric.variables.*;
import voltric.variables.util.VariableUtil;
import voltric.variables.util.IllegalVariableCastException;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

/**
 * Created by equipo on 21/02/2017.
 */
// This class returns the specific Data type
public class GenericArffFileReader implements DataFileReader{

    public <V extends IVariable> Data<V> readData(String filePathString, Class<V> dataType){

        // Get the Path object from the provided string
        Path pathFile = Paths.get(filePathString);

        try {
            //
            String relationName = getRelationName(pathFile);
            //
            VariableCollection<V> attributes = getAttributes(pathFile, dataType);
            //
            DataInstanceCollection<V> instances = getDataInstances(pathFile, attributes);
            //
            return new Data<V>(relationName, attributes, instances);

        } catch (IOException ex) {
            throw new UncheckedIOException(ex);
        } catch (IllegalVariableCastException ivcEx){
            throw new IllegalArgumentException("the specified type doesn't coincide with the types of the ARFF attributes.");
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

        // Returns the relation name after removing the '@relation' substring
        return atRelation.get().split(" ")[1];
    }

    private <V extends IVariable> VariableCollection<V> getAttributes(Path pathFile, Class<V> dataType) throws IOException, IllegalVariableCastException {

        int dataLineCount = getDataLineCount(pathFile);

        List<String> attLines = Files.lines(pathFile)
                .map(String::trim)
                .filter(w -> !w.isEmpty())
                .filter(w -> !w.startsWith("%"))
                .limit(dataLineCount)
                .filter(line -> line.startsWith("@attribute"))
                .collect(Collectors.toList());

        // Creamos los atributos con el supertipo Variable
        List<IVariable> atts = IntStream.range(0,attLines.size())
                .mapToObj( i -> createAttributeFromLine(i, attLines.get(i)))
                .collect(Collectors.toList());

        // Primero check pasando el tipo generico que se obtiene a partir de la coleccion
        // Despues hacemos "cast" (solo para el compilador porque la informacion se pierde en runtime) y creamos la colección especifica
        List<V> castedAttributes = VariableUtil.castVariables(atts, dataType);

        return new VariableCollection<V>(castedAttributes);
    }

    private  <V extends IVariable> DataInstanceCollection<V> getDataInstances(Path pathFile, VariableCollection<V> variables) throws IOException{

        int dataLineCount = getDataLineCount(pathFile);

        Stream<String> dataLines =  Files.lines(pathFile)
                .filter(w -> !w.isEmpty())
                .filter(w -> !w.startsWith("%"))
                .skip(dataLineCount)
                .filter(w -> !w.isEmpty());

        int dataLineIndex = 1;
        DataInstanceCollection<V> dataInstances = new DataInstanceCollection<V>();
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
    private IVariable createAttributeFromLine(int index, String line){
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