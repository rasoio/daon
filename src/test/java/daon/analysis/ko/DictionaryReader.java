package daon.analysis.ko;

import java.io.IOException;

import daon.analysis.ko.dict.config.Config;

public interface DictionaryReader<T> {
	
	public void read(Config config) throws IOException;
	
	public boolean hasNext();
	
	public T next(Class<T> clazz) throws IOException;
	
	public int getCusor();
	
	public void close();
	
}
