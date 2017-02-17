package voltric.io.data;

import voltric.data.Data;

import java.io.IOException;

/**
 * Created by equipo on 16/02/2017.
 */
public interface DataFileReader {

    Data readData(String filePathString) throws IOException;
}
