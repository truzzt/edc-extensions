package de.sovity.extension.clearinghouse.ids.multipart.sender.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.sovity.extension.clearinghouse.ids.multipart.sender.response.IdsMultipartParts;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import util.TestUtil;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.doReturn;

class ResponseUtilTest extends TestUtil  {

    private ResponseUtil responseUtil;

    @Mock
    private IdsMultipartParts idsMultipartParts;

    private final ObjectMapper mapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void parseMultipartStringResponse() throws IOException {

        doReturn(getHeaderInputStream()).when(idsMultipartParts).getHeader();
        var response = ResponseUtil.parseMultipartStringResponse(idsMultipartParts, mapper);
        assertNotNull(response);
    }
}
