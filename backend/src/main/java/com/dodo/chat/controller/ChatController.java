package com.dodo.chat.controller;

import com.dodo.chat.domain.Message;
import com.dodo.chat.domain.MessageDTO;

import com.dodo.chat.domain.MessageRepository;
import com.dodo.exception.NotFoundException;
import com.dodo.room.RoomRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@RequiredArgsConstructor
@Controller
public class ChatController {

    private final SimpMessageSendingOperations template;
    private final MessageRepository messageRepository;
    private final RoomRepository roomRepository;

    // 메세지 전송
    @MessageMapping("/chat/sendMessage/{roomId}")
    public void sendMessage(@Payload Message chat, @DestinationVariable Long roomId) {
        log.info("CHAT {}", chat);
        Message message = Message.builder()
                .roomId(roomId)
                .memberId(chat.getMemberId())
                .message(chat.getMessage())
                .time(LocalDateTime.now())
                .build();

        messageRepository.save(message);

        template.convertAndSend("/sub/chat/room/" + roomId, new MessageDTO(message));

    }

    @GetMapping("/chat/room-messages/{roomId}")
    public List<Message> getRoomMessages(@PathVariable Long roomId) {
        return messageRepository.findAllByRoomId(roomId).orElse(null);
    }
}
