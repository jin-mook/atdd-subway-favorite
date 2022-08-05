package nextstep.subway.unit;

import com.fasterxml.jackson.databind.ObjectMapper;
import nextstep.auth.authentication.AuthenticationToken;
import nextstep.auth.context.Authentication;
import nextstep.auth.token.JwtTokenProvider;
import nextstep.auth.token.TokenAuthenticationInterceptor;
import nextstep.auth.token.TokenRequest;
import nextstep.member.application.MemberDetailsService;
import nextstep.member.domain.Member;
import nextstep.member.domain.MemberDetails;
import nextstep.member.domain.RoleType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import java.io.IOException;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TokenAuthenticationInterceptorTest {
    private static final String EMAIL = "email@email.com";
    private static final String PASSWORD = "password";
    public static final String JWT_TOKEN = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIiLCJuYW1lIjoiSm9obiBEb2UiLCJpYXQiOjE1MTYyMzkwMjJ9.ih1aovtQShabQ7l0cINw4k1fagApg3qLWiB8Kt59Lno";

    MockHttpServletRequest request;
    MockHttpServletResponse response;

    @InjectMocks
    private TokenAuthenticationInterceptor tokenAuthenticationInterceptor;

    @Mock
    private MemberDetailsService memberDetailsService;

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    @BeforeEach
    void setUp() throws IOException {
        jwtTokenProvider = new JwtTokenProvider();
        jwtTokenProvider.setSecretKey("atdd-secret-key");
        jwtTokenProvider.setValidityInMilliseconds(3600000);

        tokenAuthenticationInterceptor = new TokenAuthenticationInterceptor(memberDetailsService, jwtTokenProvider);
        request = createMockRequest();
        response = new MockHttpServletResponse();


    }

    @Test
    void convert() throws Exception {
        //when
        AuthenticationToken token = tokenAuthenticationInterceptor.convert(request);

        //then
        assertThat(token.getPrincipal()).isEqualTo(EMAIL);
        assertThat(token.getCredentials()).isEqualTo(PASSWORD);
    }

    @Test
    void authenticate() {
        //given
        MemberDetails memberDetails = MemberDetails.of(new Member(EMAIL, PASSWORD, 17, List.of(RoleType.ROLE_MEMBER.name())));
        when(memberDetailsService.loadUserByUsername(EMAIL))
                .thenReturn(memberDetails);
        AuthenticationToken token = new AuthenticationToken(EMAIL, PASSWORD);

        //when
        Authentication authentication = tokenAuthenticationInterceptor.authenticate(token);

        //then
        assertThat(authentication.getPrincipal()).isEqualTo(EMAIL);
        assertThat(authentication.getAuthorities()).isEqualTo(List.of(RoleType.ROLE_MEMBER.name()));
    }

    @Test
    void preHandle() throws IOException {
    }

    private MockHttpServletRequest createMockRequest() throws IOException {
        MockHttpServletRequest request = new MockHttpServletRequest();
        TokenRequest tokenRequest = new TokenRequest(EMAIL, PASSWORD);
        request.setContent(new ObjectMapper().writeValueAsString(tokenRequest).getBytes());
        return request;
    }

}
