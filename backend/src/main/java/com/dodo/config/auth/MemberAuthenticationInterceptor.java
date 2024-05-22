package com.dodo.config.auth;


import com.dodo.config.auth.service.AuthService;
import com.dodo.exception.UnauthorizedException;
import com.dodo.member.domain.MemberContext;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
@Slf4j
@RequiredArgsConstructor
public class MemberAuthenticationInterceptor implements HandlerInterceptor {

    private final AuthService authService;

    private final String MEMBER_CONTEXT = "memberContext";
    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final String BEARER_PREFIX = "Bearer ";

    /**
     * 메서드에 CustomAuthentication 어노테이션이 있다면 가로챈다.
     * JWT토큰을 확인해서 유저정보를 attribute로 넣어준다.
     * https://twer.tistory.com/entry/Spring-Interceptor-%EC%BB%A4%EC%8A%A4%ED%85%80-%EC%96%B4%EB%85%B8%ED%85%8C%EC%9D%B4%EC%85%98%EA%B3%BC-Intercepter-%EA%B5%AC%ED%98%84
     *
     * TODO
     * 현재는 MemberContext에 유저아이디만 들어가는데 룸아이디까지 들어가게 하면 편할듯
     */
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        if (!(handler instanceof HandlerMethod)) {
            return HandlerInterceptor.super.preHandle(request, response, handler);
        }


        // CustomAuthentication 어노테이션이 있는지 확인함
        // 아니면 이미 MEMBER_CONTEXT가 들어가 있는 경우를 처리함
        HandlerMethod handlerMethod = (HandlerMethod) handler;
        if ((handlerMethod.getMethodAnnotation(CustomAuthentication.class) == null
            && handlerMethod.getBeanType().getAnnotation(CustomAuthentication.class) == null)
        || handlerMethod.getMethodAnnotation(NotAuth.class) != null) {
            return HandlerInterceptor.super.preHandle(request,response,handler);
        }


        if(request.getAttribute(MEMBER_CONTEXT) != null) {
            return HandlerInterceptor.super.preHandle(request,response,handler);
        }

        String authorizationToken = getAuthorizationToken(request);
        log.info("");
        log.info("요청 URI = {}", request.getRequestURI());
        log.info("메서드 = {}, 클래스 = {}", handlerMethod.getMethod().getName(), handlerMethod.getBean());
        log.info("Token = {}", authorizationToken);
        if (authorizationToken == null) {
            // 인증 실패
            throw new UnauthorizedException("인증 헤더가 필요합니다");
        }

        MemberContext context = authService.authenticate(authorizationToken);
        request.setAttribute(MEMBER_CONTEXT, context);

        return true;
    }

    private String getAuthorizationToken(HttpServletRequest request) {
        String authorizationHeader = request.getHeader(AUTHORIZATION_HEADER);
        if (authorizationHeader != null && authorizationHeader.startsWith(BEARER_PREFIX)) {
            return authorizationHeader.substring(BEARER_PREFIX.length());
        }
        return null;
    }
}
