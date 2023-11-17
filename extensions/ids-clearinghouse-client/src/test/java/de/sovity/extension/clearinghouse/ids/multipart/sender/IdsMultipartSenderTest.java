package de.sovity.extension.clearinghouse.ids.multipart.sender;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.fraunhofer.iais.eis.ContentType;
import de.fraunhofer.iais.eis.DynamicAttributeToken;
import de.fraunhofer.iais.eis.DynamicAttributeTokenImpl;
import de.fraunhofer.iais.eis.LogMessage;
import de.fraunhofer.iais.eis.Message;
import de.fraunhofer.iais.eis.NotificationMessage;
import de.sovity.extension.clearinghouse.ids.multipart.sender.response.MultipartResponse;
import jakarta.ws.rs.core.MediaType;
import okhttp3.HttpUrl;
import okhttp3.ResponseBody;
import org.eclipse.edc.spi.EdcException;
import org.eclipse.edc.spi.http.EdcHttpClient;
import org.eclipse.edc.spi.iam.IdentityService;
import org.eclipse.edc.spi.iam.TokenParameters;
import org.eclipse.edc.spi.iam.TokenRepresentation;
import org.eclipse.edc.spi.monitor.Monitor;
import org.eclipse.edc.spi.result.Result;
import org.eclipse.edc.spi.types.domain.message.RemoteMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

class IdsMultipartSenderTest {

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
    void extractResponsePartsSuccessful() throws Exception {
        ResponseBody responseBody = mock(ResponseBody.class);
        doReturn(okhttp3.MediaType.get(MediaType.APPLICATION_JSON)).when(responseBody).contentType();
        doReturn(okhttp3.MediaType.parse(MediaType.APPLICATION_JSON)).when(responseBody).contentType().parameter(anyString());
        var response = multipartSender.extractResponseParts(responseBody);
    }

    @Test
    void checkResponseTypeSuccessful(){
        doReturn(List.of(mock(NotificationMessage.class))).when(senderDelegate).getAllowedResponseTypes();

        IdsMultipartSender sender1 = spy(multipartSender);
        doNothing().when(sender1).checkResponseType(any(),any());
        verify(sender1).checkResponseType(mock(MultipartResponse.class), senderDelegate);
    }
}
