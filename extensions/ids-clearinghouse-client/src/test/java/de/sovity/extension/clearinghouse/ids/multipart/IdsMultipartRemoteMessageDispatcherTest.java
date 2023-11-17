package de.sovity.extension.clearinghouse.ids.multipart;

import de.sovity.extension.clearinghouse.ids.multipart.sender.IdsMultipartSender;
import de.sovity.extension.clearinghouse.ids.multipart.sender.MultipartSenderDelegate;
import org.eclipse.edc.connector.transfer.spi.types.protocol.TransferStartMessage;
import org.eclipse.edc.spi.EdcException;
import org.eclipse.edc.spi.types.domain.message.RemoteMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

class IdsMultipartRemoteMessageDispatcherTest {


    private IdsMultipartRemoteMessageDispatcher messageDispatcher;

    @Mock
    private MultipartSenderDelegate senderDelegate;

    @Mock
    private IdsMultipartSender idsMultipartSender;

    @Mock
    private RemoteMessage remoteMessage;

    @Mock
    private Map<Class<? extends RemoteMessage>, MultipartSenderDelegate<? extends RemoteMessage, ?>> delegates;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        messageDispatcher = new IdsMultipartRemoteMessageDispatcher(idsMultipartSender);

    }

    @Test
    void dispatchSuccessful() {
        doReturn(new CompletableFuture()).when(idsMultipartSender).send(any(), any());
        doReturn(remoteMessage.getClass()).when(senderDelegate).getMessageType();
        messageDispatcher.register(senderDelegate);

        var response = messageDispatcher.dispatch(Object.class, remoteMessage);
        assertNotNull(response);
    }

    @Test
    void unsuportedMessageError() throws ExecutionException, InterruptedException {
        var transferMessage = mock(TransferStartMessage.class);
        var response = messageDispatcher.dispatch(Object.class, transferMessage);

        assertEquals(CompletableFuture.completedFuture(null).get(), response.get());
    }

    @Test
    void delegateNotFoundError() {
        messageDispatcher.register(senderDelegate);
        EdcException exception = assertThrows(EdcException.class, () -> messageDispatcher.dispatch(Object.class, remoteMessage));

        assertEquals("Message sender not found for message type: ",exception.getMessage().substring(0, 43));
    }
}
