package com.chatapp.chatapp;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.messaging.converter.MappingJackson2MessageConverter;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompFrameHandler;
import org.springframework.messaging.simp.stomp.StompHeaders;
import org.springframework.messaging.simp.stomp.StompSession;
import org.springframework.messaging.simp.stomp.StompSessionHandler;
import org.springframework.messaging.simp.stomp.StompSessionHandlerAdapter;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.socket.WebSocketHttpHeaders;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.messaging.WebSocketStompClient;
import org.springframework.web.socket.sockjs.client.SockJsClient;
import org.springframework.web.socket.sockjs.client.Transport;
import org.springframework.web.socket.sockjs.client.WebSocketTransport;

import com.model.ChatInMessage;
import com.model.ChatOutMessage;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class ChatServerIntegrationTests {

    @LocalServerPort
    private int port;
    private SockJsClient sockJsClient;
    private WebSocketStompClient stompClient;
    private final WebSocketHttpHeaders headers = new WebSocketHttpHeaders();

    @Before
    public void setup(){

        List<Transport> transports = new ArrayList<>();
        transports.add(new WebSocketTransport(new StandardWebSocketClient()));
        this.sockJsClient = new SockJsClient(transports);
        this.stompClient = new WebSocketStompClient(sockJsClient); // emulate JS client

        // convert JSON into Java objects and vice versa
        this.stompClient.setMessageConverter(new MappingJackson2MessageConverter());

    }

    @SuppressWarnings("removal") //TODO: check how to update this later
    @Test
    public void getChatMessage() throws Exception {

        final CountDownLatch latch = new CountDownLatch(1);
        final AtomicReference<Throwable> failure = new AtomicReference<>();

        StompSessionHandler handler = new TestSessionHandler(failure){

            @Override
            public void afterConnected(final StompSession session, StompHeaders headers){
                
                // simulate client subscribing to a topic
                // this actually happens AFTER the connection simulated below this method
                session.subscribe("/topic/guestchats", new StompFrameHandler() {
                    
                    @Override
                    public Type getPayloadType(StompHeaders headers){
                        return ChatOutMessage.class;
                    }

                    @Override
                    public void handleFrame(StompHeaders headers, Object payload){
                        ChatOutMessage greeting = (ChatOutMessage) payload;
                        try {
                            assertEquals("Hello Spring", greeting.getMessage());
                        } catch (Throwable t) {
                            failure.set(t);
                        } finally {
                            session.disconnect();
                            latch.countDown();
                        }
                    }

                });

                try {
                    ChatInMessage myMessage = new ChatInMessage("Hello Spring");
                    session.send("/app/guestchat", myMessage);                    
                } catch (Throwable t) {
                    failure.set(t);
                    latch.countDown();
                }

            }

        };

        // simulate a connection
        // this actually happens first
        this.stompClient.connect(
            "ws://localhost:{port}/chatapp", 
            this.headers,
            handler,
            this.port);

        if (latch.await(3, TimeUnit.SECONDS)){
            if (failure.get() != null) {
                throw new AssertionError("", failure.get());
            }
        } else {
            fail("Greeting not received.");
        }

    }

    private class TestSessionHandler extends StompSessionHandlerAdapter{

        private final AtomicReference<Throwable> failure;

        public TestSessionHandler(AtomicReference<Throwable> failure) {
            this.failure = failure;
        }

        @Override
        public void handleFrame(StompHeaders headers, Object payload) {
            this.failure.set(new Exception(headers.toString()));
        }

        @Override
        public void handleException(StompSession s, StompCommand c, StompHeaders h, byte[] p, Throwable ex) {
            this.failure.set(ex);
        }

        @Override
        public void handleTransportError(StompSession session, Throwable ex) {
            this.failure.set(ex);
        }

    }

}
