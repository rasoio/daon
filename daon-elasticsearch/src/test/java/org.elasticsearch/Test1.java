package org.elasticsearch;

import org.elasticsearch.common.io.PathUtils;
import org.elasticsearch.test.rest.yaml.ESClientYamlSuiteTestCase;
import org.junit.Test;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;

public class Test1 {
    private static final String TESTS_PATH = "/rest-api-spec/test";

    public void testPath() throws URISyntaxException, IOException {
        Path root = PathUtils.get(ESClientYamlSuiteTestCase.class.getResource(TESTS_PATH).toURI());

//        System.out.println(root);

        String[] paths = new String[]{"daon"};

        for (String strPath : paths) {
            Path path = root.resolve(strPath);

//            System.out.println(path.toString());
            if (Files.isDirectory(path)) {
                Files.walk(path).forEach(file -> {
//                    System.out.println(file);
                    if (file.toString().endsWith(".yaml")) {

//                        System.out.println(file.toString());
//                        addYamlSuite(root, file, files);
                    }
                });
            } else {
                path = root.resolve(strPath + ".yaml");
                assert Files.exists(path);

//                System.out.println(path);
            }
        }
    }
}
