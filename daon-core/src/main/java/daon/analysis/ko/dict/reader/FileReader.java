package daon.analysis.ko.dict.reader;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.Charsets;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;

import daon.analysis.ko.dict.config.Config;

public class FileReader<T> implements Reader<T> {

    private Logger logger = LoggerFactory.getLogger(FileReader.class);

    private String encoding = Charset.defaultCharset().name();

    private List<T> lines = new ArrayList<T>();

    private int size = 0;

    private int cursor = 0; // index of next element to return

    private static ObjectMapper mapper = new ObjectMapper();

    private InputStream inputStream;

    /**
     * 파일 라인을 읽어 list로 준비함.
     * - 사전 단어 기준 오름차순(asc) 정렬
     */
    @SuppressWarnings("unchecked")
    public void read(Config config) throws IOException {

        String fileName = config.get(Config.FILE_NAME, String.class);

        Class<T> valueType = config.get(Config.VALUE_TYPE, Class.class);

        inputStream = this.getClass().getResourceAsStream(fileName);
        if (inputStream == null)
            throw new FileNotFoundException("Not in classpath: " + fileName);

        try (final InputStreamReader reader = new InputStreamReader(inputStream, Charsets.toCharset(encoding))) {

            final BufferedReader bufferedReader = new BufferedReader(reader);
            String line = bufferedReader.readLine();
            while (line != null) {

                T keyword = mapper.readValue(line, valueType);

                lines.add(keyword);
                line = bufferedReader.readLine();
            }

            size = lines.size();
        } finally {
            IOUtils.closeQuietly(inputStream);
        }

    }

    public boolean hasNext() {
        return cursor != size;
    }

    public T next() throws IOException {

        if (cursor >= size) {
            return null;
        }

        T term = lines.get(cursor);

        cursor++;

        return term;
    }

    public int getCusor() {
        return cursor;
    }

    @Override
    public void close() {

        if (inputStream != null) {
            try {
                inputStream.close();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }

}
