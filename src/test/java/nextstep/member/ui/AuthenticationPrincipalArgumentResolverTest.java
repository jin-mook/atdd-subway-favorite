package nextstep.member.ui;

import nextstep.member.AuthenticationException;
import nextstep.member.application.JwtTokenProvider;
import nextstep.member.domain.LoginMember;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.MethodParameter;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.ModelAndViewContainer;

@ExtendWith(MockitoExtension.class)
class AuthenticationPrincipalArgumentResolverTest {

    @Mock
    private JwtTokenProvider jwtTokenProvider;
    @Mock
    private MethodParameter methodParameter;
    @Mock
    private ModelAndViewContainer modelAndViewContainer;
    @Mock
    private NativeWebRequest nativeWebRequest;
    @Mock
    private WebDataBinderFactory binderFactory;

    @DisplayName("토큰 정보가 없으면 인증 에러가 발생합니다.")
    @Test
    void noToken() throws Exception {
        // given
        AuthenticationPrincipalArgumentResolver argumentResolver = new AuthenticationPrincipalArgumentResolver(jwtTokenProvider);
        Mockito.doReturn(null).when(nativeWebRequest).getHeader("Authorization");
        // when
        // then
        Assertions.assertThatThrownBy(() -> argumentResolver.resolveArgument(methodParameter, modelAndViewContainer, nativeWebRequest, binderFactory))
                .isInstanceOf(AuthenticationException.class);
    }

    @DisplayName("Bearer 토큰이 아닌 Basic 토큰이라면 인증 에러가 발생합니다.")
    @Test
    void basicToken() {
        // given
        String basic = "Basic accessToken";
        AuthenticationPrincipalArgumentResolver argumentResolver = new AuthenticationPrincipalArgumentResolver(jwtTokenProvider);
        Mockito.doReturn(basic).when(nativeWebRequest).getHeader("Authorization");
        // when
        // then
        Assertions.assertThatThrownBy(() -> argumentResolver.resolveArgument(methodParameter, modelAndViewContainer, nativeWebRequest, binderFactory))
                .isInstanceOf(AuthenticationException.class);
    }

    @DisplayName("Bearer 토큰이지만 뒤에 accessToken 정보가 없으면 인증 에러가 발생합니다.")
    @Test
    void noAccessToken() {
        // given
        String bearer = "Bearer ";
        AuthenticationPrincipalArgumentResolver argumentResolver = new AuthenticationPrincipalArgumentResolver(jwtTokenProvider);
        Mockito.doReturn(bearer).when(nativeWebRequest).getHeader("Authorization");
        // when
        // then
        Assertions.assertThatThrownBy(() -> argumentResolver.resolveArgument(methodParameter, modelAndViewContainer, nativeWebRequest, binderFactory))
                .isInstanceOf(AuthenticationException.class);
    }

    @ParameterizedTest(name = "토큰 공백이 정상적이지 않다면 에러가 발생합니다.")
    @ValueSource(strings = {"Bearer ", "bearer   ", " bearer   "})
    void invalidToken(String prefix) {
        // given
        String accessToken = "accessToken";
        String bearer = prefix + accessToken;
        Mockito.doReturn(bearer).when(nativeWebRequest).getHeader("Authorization");
        AuthenticationPrincipalArgumentResolver argumentResolver = new AuthenticationPrincipalArgumentResolver(jwtTokenProvider);
        // when
        // then
        Assertions.assertThatThrownBy(() -> argumentResolver.resolveArgument(methodParameter, modelAndViewContainer, nativeWebRequest, binderFactory))
                .isInstanceOf(AuthenticationException.class);
    }

    @DisplayName("토큰이 정상이라면 LoginMember 객체를 응답합니다.")
    @Test
    void successAuth() throws Exception {
        // given
        String token = "accessToken";
        String bearer = "Bearer " + token;
        String email = "email@test.com";
        AuthenticationPrincipalArgumentResolver argumentResolver = new AuthenticationPrincipalArgumentResolver(jwtTokenProvider);
        Mockito.doReturn(bearer).when(nativeWebRequest).getHeader("Authorization");
        Mockito.doReturn(true).when(jwtTokenProvider).validateToken(token);
        Mockito.doReturn(email).when(jwtTokenProvider).getPrincipal(token);

        // when
        LoginMember loginMember = (LoginMember) argumentResolver.resolveArgument(methodParameter, modelAndViewContainer, nativeWebRequest, binderFactory);
        // then
        Assertions.assertThat(loginMember).isEqualTo(new LoginMember(email));
    }
}