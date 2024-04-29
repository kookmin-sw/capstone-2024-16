package com.dodo.fcm;

import com.dodo.exception.NotFoundException;
import com.dodo.user.UserRepository;
import com.dodo.user.domain.User;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.Notification;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
@Slf4j
public class FCMNotificationService {

    private final FirebaseMessaging firebaseMessaging;
    private final UserRepository userRepository;

    public void sendNotificationByToken(FCMNotificationRequestDto requestDto){

        User user = userRepository.findById(requestDto.getTargetUserId()).orElseThrow(NotFoundException::new);

        if (user.getFirebaseToken() != null) {
            Notification notification = Notification.builder()
                    .setTitle(requestDto.getTitle())
                    .setBody(requestDto.getBody())
                    .build();

            Message message = Message.builder()
                    .setToken(user.getFirebaseToken())
                    .setNotification(notification)
                    .build();

            try {
                firebaseMessaging.send(message);
            } catch (FirebaseMessagingException e) {
                e.printStackTrace();
            }

        } else {
            log.info("유저의 fcm 토큰이 없습니다.");
        }
    }
}
