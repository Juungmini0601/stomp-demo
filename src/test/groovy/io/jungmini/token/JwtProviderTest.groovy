package io.jungmini.token

import spock.lang.Specification

class JwtProviderTest extends Specification {
    def jwtSecret = 'Wmee6jKz8VpGbWnHhw/ksUeZ6vF0z457du2pjx7d7uc='
    def expiration = 86400000
    def jwtProvider = new JwtProvider(jwtSecret, expiration)

    def 'JWT 토큰을 정상적으로 생성 파싱 가능하다.'() {
        given:
        Long userId = 1L

        when:
        def token = jwtProvider.generateAccessToken(userId)
        def parsedUserId = jwtProvider.getUserIdFromToken(token)

        then:
        parsedUserId == userId
    }

}
