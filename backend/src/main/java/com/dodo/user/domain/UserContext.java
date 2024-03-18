package com.dodo.user.domain;

import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@NoArgsConstructor
public class UserContext {
    private Long userId;
    private Long roomId;

    public UserContext(Long userId, Long roomId) {
        this.userId = userId;
        this.roomId = roomId;
    }

    public UserContext(Long userId) {
        this.userId = userId;
    }
}