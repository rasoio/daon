package daon.analysis.ko.dict.reader;

import java.io.IOException;

import daon.analysis.ko.Keyword;
import daon.analysis.ko.dict.config.Config;

public interface DictionaryReader {
	
	public void read(Config config) throws IOException;
	
	public boolean hasNext();
	
	public Keyword next() throws IOException;
	
	public int getCusor();
	
	public void close();
	
}