package de.sovity.extension.clearinghouse.ids.multipart.sender;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.fraunhofer.iais.eis.DynamicAttributeToken;
import de.fraunhofer.iais.eis.LogMessage;
import de.sovity.extension.clearinghouse.ids.multipart.sender.response.MultipartResponse;
import okhttp3.ResponseBody;
import okio.BufferedSource;
import org.eclipse.edc.spi.http.EdcHttpClient;
import org.eclipse.edc.spi.iam.IdentityService;
import org.eclipse.edc.spi.iam.TokenRepresentation;
import org.eclipse.edc.spi.monitor.Monitor;
import org.eclipse.edc.spi.result.Result;
import org.eclipse.edc.spi.types.domain.message.RemoteMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import util.TestUtil;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

class IdsMultipartSenderTest extends TestUtil {

    private IdsMultipartSender multipartSender;
    @Mock
    private Monitor monitor;
    @Mock
    private EdcHttpClient httpClient;

    @Mock
    private IdentityService identityService;

    @Mock
    private MultipartSenderDelegate senderDelegate;

    @Mock
    private ResponseBody responseBody;

    @Mock
    private BufferedSource source;

    @Mock
    private RemoteMessage remoteMessage;

    private final ObjectMapper mapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        multipartSender = new IdsMultipartSender(monitor,httpClient, identityService, mapper);
    }

    @Test
    void sendSuccessfull() {
        doReturn(Result.success(mock(TokenRepresentation.class))).when(identityService).obtainClientCredentials(any());
        doReturn("1").when(remoteMessage).getCounterPartyAddress();
        var response = multipartSender.send(remoteMessage, senderDelegate);

        assertNotNull(response);
    }

    @Test
    void sendObtainingTokenError() {
        doReturn(Result.failure("Fail")).when(identityService).obtainClientCredentials(any());
        doReturn("1").when(remoteMessage).getCounterPartyAddress();

        CompletableFuture compatibleResponse = multipartSender.send(remoteMessage, senderDelegate);

        ExecutionException exception = assertThrows(ExecutionException.class, () -> compatibleResponse.get());
        assertEquals("org.eclipse.edc.spi.EdcException: Failed to obtain token: Fail", exception.getMessage());
    }

    @Test
    void sendMissingConnectorAddressError() {
        doReturn("1").when(remoteMessage).getCounterPartyAddress();
        IdsMultipartSender sender1 = spy(multipartSender);
        doReturn(Result.success(mock(DynamicAttributeToken.class))).when(sender1).obtainDynamicAttributeToken(null);
        doReturn(Result.success(mock(TokenRepresentation.class))).when(identityService).obtainClientCredentials(any());

        CompletableFuture compatibleResponse = multipartSender.send(remoteMessage, senderDelegate);

        ExecutionException exception = assertThrows(ExecutionException.class, () -> compatibleResponse.get());
        assertEquals("java.lang.IllegalArgumentException: Connector address not specified", exception.getMessage());
    }

    @Test
    void obtainDynamicAttributeTokensSuccessfull() {
        doReturn(Result.success(mock(TokenRepresentation.class))).when(identityService).obtainClientCredentials(any());
        var response = multipartSender.obtainDynamicAttributeToken(anyString());

        assertNotNull(response);
    }

    @Test
    void checkResponseTypeSuccessful() throws IOException {
        IdsMultipartSender sender1 = spy(multipartSender);
        MultipartResponse response = mock(MultipartResponse.class);
        var message = mapper.readValue(getHeaderInputStream(), LogMessage.class);
        doReturn(List.of(message.getClass())).when(senderDelegate).getAllowedResponseTypes();
        doReturn(message).when(response).getHeader();

        sender1.checkResponseType(response, senderDelegate);
        verify(sender1, atLeast(1)).checkResponseType(response, senderDelegate);
    }
}
