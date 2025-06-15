package io.jungmini.integration

import groovy.util.logging.Slf4j
import io.jungmini.domain.liveboard.LiveBoardService
import io.jungmini.domain.liveboard.dto.LiveBoardChatRequest
import io.jungmini.domain.liveboard.dto.LiveBoardConnectionCountResponse
import io.jungmini.domain.liveboard.model.LiveBoardChatMessageEntity
import io.jungmini.util.CustomStompClient
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
@Slf4j
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class LiveBoardMessagingTest extends Specification {
    @LocalServerPort
    int port;

    @Autowired
    RedisTemplate<String, Object> redisTemplate

    @Autowired
    LiveBoardService liveBoardService;

    // 각 테스트 메서드 실행 전에 Redis 초기화
    def setup() {
        redisTemplate.connectionFactory.connection.serverCommands().flushAll()
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
        def connectionCount = liveBoardService.getConnectionCount(liveBoardId)

        result1.connectionCount() == 1L
        result2.connectionCount() == 1L
        connectionCount == 1L
    }

    def "라이브 보드 채팅 테스트"() {
        given: '3개의 클라이언트가 1번방에 입장한다'
        def url = "ws://localhost:${port}/ws"
        def liveBoardId = 1L
        def client1 = StompUtil.createStompClient(url)
        def client2 = StompUtil.createStompClient(url)
        def client3 = StompUtil.createStompClient(url)
        client1.session.send("/app/liveboard/${liveBoardId}/join", new byte[0])
        client2.session.send("/app/liveboard/${liveBoardId}/join", new byte[0])
        client3.session.send("/app/liveboard/${liveBoardId}/join", new byte[0])

        subScribeLiveBoardChat(client1, liveBoardId)
        subScribeLiveBoardChat(client2, liveBoardId)
        subScribeLiveBoardChat(client3, liveBoardId)

        log.info("1초대기")
        Thread.sleep(1000)

        when: '1번이 메시지를 보내면'
        def chatRequest = new LiveBoardChatRequest("hello!", "client1");
        client1.session.send("/app/liveboard/${liveBoardId}/chat", chatRequest)

        then: '3개의 클라이언트 모두 매시지를 받는다'
        def result1 = client1.queue.poll(3, TimeUnit.SECONDS) as LiveBoardChatMessageEntity
        def result2 = client2.queue.poll(3, TimeUnit.SECONDS) as LiveBoardChatMessageEntity
        def result3 = client3.queue.poll(3, TimeUnit.SECONDS) as LiveBoardChatMessageEntity

        result1 != null
        result2 != null
        result3 != null
    }

    static def subScribeLiveBoardChat(CustomStompClient client, Long liveBoardId) {
        client.session.subscribe("/topic/liveboard/${liveBoardId}/chat", new StompFrameHandler() {
            @Override
            Type getPayloadType(StompHeaders headers) {
                return LiveBoardChatMessageEntity.class
            }

            @Override
            void handleFrame(StompHeaders headers, @Nullable Object payload) {
                client.queue.put(payload)
            }
        })
    }

    static def subScribeLiveBoardCount(CustomStompClient client, Long liveBoardId) {
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
