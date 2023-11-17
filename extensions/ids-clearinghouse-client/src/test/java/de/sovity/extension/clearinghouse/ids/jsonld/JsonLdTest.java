package de.sovity.extension.clearinghouse.ids.jsonld;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.fraunhofer.iais.eis.LogMessage;
import org.junit.jupiter.api.Test;
import util.TestUtil;

import javax.xml.datatype.XMLGregorianCalendar;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

import static org.junit.jupiter.api.Assertions.assertNotNull;

public class JsonLdTest extends TestUtil {
    @Test
    void getInfoModelMessageParserSuccessful() throws IOException, URISyntaxException {
        ObjectMapper mapper = JsonLd.getObjectMapper();
        var jsonContents =  getJsonContent("request-message.json");

        LogMessage msg = mapper.readValue(jsonContents, LogMessage.class);
        assertNotNull(msg);
    }

    @Test
    void getXmlGregorianCalendarParserSuccessful() throws IOException, URISyntaxException {
        ObjectMapper mapper = JsonLd.getObjectMapper();
        var jsonContents = getJsonContent("xml-gregorian-calendar.json");

        var response = mapper.readValue(jsonContents,XMLGregorianCalendar.class);
        assertNotNull(response);
    }

    @Test
    void getUriParserSuccessful() throws IOException {
        ObjectMapper mapper = JsonLd.getObjectMapper();
        var jsonContents = getJsonContent("uri.json");

        var response = mapper.readValue(jsonContents, URI.class);
        assertNotNull(response);
    }


}
