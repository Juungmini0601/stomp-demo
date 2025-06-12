package io.jungmini.integration

import io.jungmini.domain.test.StompDummyController
import io.jungmini.util.StompUtil
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.server.LocalServerPort
import org.springframework.lang.Nullable
import org.springframework.messaging.simp.stomp.StompFrameHandler
import org.springframework.messaging.simp.stomp.StompHeaders
import spock.lang.Specification

import java.lang.reflect.Type
import java.util.concurrent.TimeUnit

/**
 *  1. 웹소켓 연결
 *  2. 서버에서 웹소켓 커넥션을 들고 있음
 *
 *  - 클라이언트가 원할때 데이터 푸쉬
 *  - 서버가 원할대 데이터 푸쉬
 *
 *  웹소켓 연결해서 들고 있어야함
 */
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
                return StompDummyController.PongResponse.class
            }

            @Override
            void handleFrame(StompHeaders headers, @Nullable Object payload) {
                client.queue.put(payload)
            }
        })

        when:
        client.session.send("/app/ping", new byte[0]);

        then:
        def result = client.queue.poll(3, TimeUnit.SECONDS) as StompDummyController.PongResponse
        result.message() == "pong";
    }
}
