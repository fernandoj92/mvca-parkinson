package voltric.io.data;

import voltric.data.Data;
import voltric.variables.IVariable;

import java.io.IOException;

/**
 * Created by equipo on 16/02/2017.
 */
public interface DataFileReader {

    <V extends IVariable> Data<V> readData(String filePathString, Class<V> dataType) throws IOException;
}
