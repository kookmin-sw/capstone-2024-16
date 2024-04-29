package com.dodo.user;

import com.dodo.exception.NotFoundException;
import com.dodo.image.ImageRepository;
import com.dodo.image.domain.Image;
import com.dodo.token.TokenService;
import com.dodo.user.domain.AuthenticationType;
import com.dodo.user.domain.PasswordAuthentication;
import com.dodo.user.domain.User;
import com.dodo.user.domain.UserContext;
import com.dodo.user.dto.UserCreateRequestData;
import com.dodo.user.dto.UserData;
import com.dodo.user.dto.UserLoginRequestData;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
@Slf4j
public class UserService {

    private final UserRepository userRepository;
    private final PasswordAuthenticationRepository passwordAuthenticationRepository;
    private final PasswordEncoder passwordEncoder;
    private final TokenService tokenService;
    private final ImageRepository imageRepository;


    @Transactional
    public User register(UserCreateRequestData request) {
        if(userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("이메일이 이미 존재합니다");
        }

        // TODO
        // 기본 이미지 설정
        // 나중에 바꿔야 함
        Image image = imageRepository.findById(1L).get();


        log.info("{}", request.getType());
        User user = User.builder()
                .authenticationType(request.getType())
                .email(request.getEmail())
                .name(request.getEmail().split("@")[0])
                .mileage(0)
                .image(image)
                .introduceMessage("")
                .firebaseToken(request.getFirebaseToken())
                .build();

        userRepository.save(user);

        if(request instanceof UserCreateRequestData.PasswordUserCreateRequestData) {
            // 비밀번호 로그인

            var req = (UserCreateRequestData.PasswordUserCreateRequestData)request;
            String password = passwordEncoder.encode(req.getPassword());
            passwordAuthenticationRepository.save(new PasswordAuthentication(user, password));
        } else {
            // 소셜로그인

        }

        return user;
    }

    public String login(UserLoginRequestData request) {
        log.info("{}", request.getAuthenticationType());
        Long userId = getUserId(request);
        return tokenService.makeToken(userId);
    }


    private Long getUserId(UserLoginRequestData request) {
        if(request.getAuthenticationType() == AuthenticationType.PASSWORD) {
            // 비밀번호 로그인
            UserLoginRequestData.PasswordLoginRequestData req = (UserLoginRequestData.PasswordLoginRequestData)request;
            User user = userRepository.findByEmail(req.getEmail())
                    .orElseThrow(() -> new NotFoundException("유저를 찾을 수 없습니다"));
            PasswordAuthentication passwordAuthentication = passwordAuthenticationRepository.findByUser(user)
                    .orElseThrow(() -> new NotFoundException("비밀번호 인증 정보를 찾을 수 없습니다"));
            if (passwordEncoder.matches(
                    req.getPassword(),
                    passwordAuthentication.getPassword())
            ) {
                return user.getId();
            }
            throw new NotFoundException();
        } else {

        }


        return 0L;
    }

    public UserData getMyData(UserContext userContext) {
        User user = userRepository.findById(userContext.getUserId())
                .orElseThrow(() -> new NotFoundException("유저를 찾을 수 없습니다"));

        return new UserData(user);
    }

    @PostConstruct
    public void makeInitialData() {
        Image image = imageRepository.findById(1L).get();
        User user = User.builder()
                .authenticationType(AuthenticationType.PASSWORD)
                .email("hello@hello.com")
                .name("hello")
                .mileage(0)
                .image(image)
                .introduceMessage("")
                .build();

        userRepository.save(user);
        String password = passwordEncoder.encode("123");

        passwordAuthenticationRepository.save(new PasswordAuthentication(user, password));
    }
}
