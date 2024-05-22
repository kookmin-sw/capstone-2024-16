package com.dodo.chat.domain;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class MessageDTO {
    private Long memberId;
    private String message;
    private LocalDateTime time;

    public MessageDTO(Message message) {
        this.memberId = message.getMemberId();
        this.message = message.getMessage();
        this.time = message.getTime();
    }
}
