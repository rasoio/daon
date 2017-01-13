package daon.analysis.ko.dict.reader;

import java.io.IOException;

import daon.analysis.ko.dict.config.Config;

public interface Reader<T> {
	
	public void read(Config config) throws IOException;
	
	public boolean hasNext();
	
	public T next() throws IOException;
	
	public int getCusor();
	
	public void close();
	
}
