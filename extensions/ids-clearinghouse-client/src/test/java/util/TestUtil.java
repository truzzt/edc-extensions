package util;

import org.eclipse.edc.spi.EdcException;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;

public class TestUtil {

    protected static InputStream getHeaderInputStream(){
        var jsonContents =  getJsonContent("valid-header.json");
        return new ByteArrayInputStream(jsonContents);
    }

    protected static Path getFile(String path) {

        ClassLoader classLoader = TestUtil.class.getClassLoader();
        var jsonResource = classLoader.getResource(path);

        if (jsonResource == null) {
            throw new EdcException("Header json file not found: " + path);
        }

        URI jsonUrl;
        try {
            jsonUrl = jsonResource.toURI();
        } catch (URISyntaxException e) {
            throw new EdcException("Error finding json file on classpath", e);
        }

        Path filePath = Path.of(jsonUrl);
        if (!Files.exists(filePath)) {
            throw new EdcException("Header json file not found: " + path);
        }

        return filePath;
    }

    protected static String readFile(String path) {
        var file = getFile(path);

        try {
            return Files.readString(file);
        } catch (IOException e) {
            throw new EdcException("Error reading file contents", e);
        }
    }

    protected static byte[] getJsonContent(String path){
        try {
            return Files.readAllBytes(getFile(path));
        } catch (IOException e){
            throw new EdcException("Error parsing json file", e);
        }
    }
}
