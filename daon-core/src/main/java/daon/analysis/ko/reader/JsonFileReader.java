package daon.analysis.ko.reader;

import java.io.*;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.Charsets;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;

import daon.analysis.ko.config.Config;

public class JsonFileReader {

    private Logger logger = LoggerFactory.getLogger(JsonFileReader.class);

    private String encoding = Charset.defaultCharset().name();

    private static ObjectMapper mapper = new ObjectMapper();

    /**
     * 파일 라인을 읽어 list로 준비함.
     */
    @SuppressWarnings("unchecked")
    public <T> List<T> read(String fileName, Class<T> valueType) throws IOException {

        List<T> lines = new ArrayList<T>();

        InputStream inputStream = this.getClass().getResourceAsStream(fileName);
        if (inputStream == null){
            logger.warn("Not in classpath: " + fileName);

            inputStream = new FileInputStream(fileName);
        }

        try (final InputStreamReader reader = new InputStreamReader(inputStream, Charsets.toCharset(encoding))) {

            final BufferedReader bufferedReader = new BufferedReader(reader);
            String line = bufferedReader.readLine();
            while (line != null) {

                T keyword = mapper.readValue(line, valueType);

                lines.add(keyword);
                line = bufferedReader.readLine();
            }
        } finally {
            IOUtils.closeQuietly(inputStream);
        }

        return lines;
    }

}
