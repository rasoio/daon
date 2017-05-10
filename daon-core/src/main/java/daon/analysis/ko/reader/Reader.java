package daon.analysis.ko.reader;

import java.io.IOException;
import java.util.List;

import daon.analysis.ko.config.Config;

public interface Reader<T>{

    public List<T> read(Config config) throws IOException;
}
