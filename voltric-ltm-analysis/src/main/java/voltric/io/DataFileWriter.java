package voltric.io;

import voltric.data.Data;

import java.io.IOException;

/**
 * Created by Fernando on 2/16/2017.
 */
public interface DataFileWriter {

    void writeToFile(Data data, String filePathString) throws IOException;
}
