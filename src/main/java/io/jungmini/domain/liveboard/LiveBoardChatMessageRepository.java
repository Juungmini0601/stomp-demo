package io.jungmini.domain.liveboard;

import org.springframework.data.jpa.repository.JpaRepository;

import io.jungmini.domain.liveboard.model.LiveBoardChatMessageEntity;

public interface LiveBoardChatMessageRepository extends JpaRepository<LiveBoardChatMessageEntity, Long> {
}
