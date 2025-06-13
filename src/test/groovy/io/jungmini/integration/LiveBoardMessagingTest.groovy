package io.jungmini.integration

import io.jungmini.domain.liveboard.dto.LiveBoardConnectionCountResponse
import io.jungmini.util.StompUtil
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.server.LocalServerPort
import org.springframework.data.redis.core.RedisTemplate
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
class LiveBoardMessagingTest extends Specification {
    @LocalServerPort
    int port;

    @Autowired
    RedisTemplate<String, Object> redisTemplate

    // 각 테스트 메서드 실행 전에 Redis 초기화
    def setup() {
        redisTemplate.connectionFactory.connection.flushAll()
    }

    def "라이브 보드 입장 테스트"() {
        given: '2개의 클라이언트가 1번방의 세션 개수를 구독하고'
        def url = "ws://localhost:${port}/ws"
        def liveBoardId = 1L
        def client1 = StompUtil.createStompClient(url)
        def client2 = StompUtil.createStompClient(url)

        subScribeLiveBoardCount(client1, liveBoardId)
        subScribeLiveBoardCount(client2, liveBoardId)

        when: '1개의 클라이언트가 1번방에 입장하면'
        client1.session.send("/app/liveboard/${liveBoardId}/join", new byte[0])

        then: '2개의 클라이언트 모두 1개의 세션을 가진다는 사실을 알고 있다.'
        def result1 = client1.queue.poll(3, TimeUnit.SECONDS) as LiveBoardConnectionCountResponse
        def result2 = client2.queue.poll(3, TimeUnit.SECONDS) as LiveBoardConnectionCountResponse

        result1.connectionCount() == 1L
        result2.connectionCount() == 1L
    }

    static def subScribeLiveBoardCount(client, liveBoardId) {
        client.session.subscribe("/topic/liveboard/${liveBoardId}/users/count", new StompFrameHandler() {
            @Override
            Type getPayloadType(StompHeaders headers) {
                return LiveBoardConnectionCountResponse.class
            }

            @Override
            void handleFrame(StompHeaders headers, @Nullable Object payload) {
                client.queue.put(payload)
            }
        })
    }
}
