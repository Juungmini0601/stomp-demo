package io.jungmini.util


import org.springframework.messaging.converter.MappingJackson2MessageConverter
import org.springframework.messaging.simp.stomp.StompCommand
import org.springframework.messaging.simp.stomp.StompHeaders
import org.springframework.messaging.simp.stomp.StompSession
import org.springframework.messaging.simp.stomp.StompSessionHandlerAdapter
import org.springframework.web.socket.client.standard.StandardWebSocketClient
import org.springframework.web.socket.messaging.WebSocketStompClient
import org.springframework.web.socket.sockjs.client.SockJsClient
import org.springframework.web.socket.sockjs.client.WebSocketTransport

import java.util.concurrent.ArrayBlockingQueue
import java.util.concurrent.BlockingQueue

class StompUtil {
    static def createStompClient(String url) {
        BlockingQueue<Object> blockingQueue = new ArrayBlockingQueue<>(5)
        def webSocketClient = new StandardWebSocketClient()
        def sockJsClient = new SockJsClient(Collections.singletonList(new WebSocketTransport(webSocketClient)))
        def stompClient = new WebSocketStompClient(sockJsClient)
        stompClient.setMessageConverter(new MappingJackson2MessageConverter())

        def connectFuture = stompClient.connectAsync(url, new StompSessionHandlerAdapter() {
            @Override
            void handleException(StompSession session, StompCommand command, StompHeaders headers, byte[] payload, Throwable exception) {
                println "Got an exception ${exception.getMessage()}"
                println exception
            }

            @Override
            void handleTransportError(StompSession session, Throwable exception) {
                println "Got a transport error ${exception.getMessage()}"
            }
        })

        def stompSession = connectFuture.get()
        [queue: blockingQueue, session: stompSession, stompClient: stompClient]
    }
}
