package voltric.io.data;

import voltric.data.Data;
import voltric.variables.IVariable;

import java.io.IOException;

/**
 * Created by Fernando on 2/16/2017.
 */
public interface DataFileWriter {

    <V extends IVariable> void writeToFile(Data<V> data, String filePathString) throws IOException;
}
