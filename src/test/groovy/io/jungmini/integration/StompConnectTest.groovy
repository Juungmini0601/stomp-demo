package io.jungmini.integration

import io.jungmini.controller.MessageController
import io.jungmini.util.StompUtil
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.server.LocalServerPort
import org.springframework.lang.Nullable
import org.springframework.messaging.simp.stomp.StompFrameHandler
import org.springframework.messaging.simp.stomp.StompHeaders
import spock.lang.Specification

import java.lang.reflect.Type
import java.util.concurrent.TimeUnit

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class StompConnectTest extends Specification {
    @LocalServerPort
    int port;

    def "STOMP 연결 테스트"() {
        given:
        def url = "ws://localhost:${port}/ws"

        when:
        def client = StompUtil.createStompClient(url)

        then:
        client.session.isConnected()
    }

    def "STOMP 연결 해제 테스트"() {
        given:
        def url = "ws://localhost:${port}/ws"
        def client = StompUtil.createStompClient(url)

        when:
        client.session.disconnect()

        then:
        client.session.isConnected() == false
    }

    def "Ping Pong 테스트"() {
        given:
        def url = "ws://localhost:${port}/ws"
        def client = StompUtil.createStompClient(url)

        client.session.subscribe("/topic/pong", new StompFrameHandler() {
            @Override
            Type getPayloadType(StompHeaders headers) {
                return MessageController.PongResponse.class
            }

            @Override
            void handleFrame(StompHeaders headers, @Nullable Object payload) {
                client.queue.put(payload)
            }
        })

        when:
        client.session.send("/app/ping", new byte[0]);

        then:
        def result = client.queue.poll(3, TimeUnit.SECONDS) as MessageController.PongResponse
        result.message() == "pong";
    }
}
