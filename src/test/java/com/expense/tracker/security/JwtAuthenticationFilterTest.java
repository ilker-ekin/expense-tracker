package com.expense.tracker.security;

import com.expense.tracker.entity.User;
import com.expense.tracker.service.JwtService;
import com.expense.tracker.service.UserService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class JwtAuthenticationFilterTest {

    @Mock JwtService jwtService;
    @Mock UserService userService;
    @Mock FilterChain filterChain;

    @InjectMocks JwtAuthenticationFilter filter;

    @AfterEach
    void clearContext() {
        SecurityContextHolder.clearContext();
    }

    private User testUser() {
        return User.builder()
                .id(1L).email("user@test.com").password("h").fullName("Test").role("USER").emailVerified(true)
                .build();
    }

    @Test
    void doFilter_noToken_continuesChainWithoutAuth() throws Exception {
        var request = new MockHttpServletRequest();
        var response = new MockHttpServletResponse();

        filter.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
    }

    @Test
    void doFilter_validCookieToken_setsAuthentication() throws Exception {
        var request = new MockHttpServletRequest();
        request.setCookies(new Cookie("vault_token", "valid-jwt"));
        var response = new MockHttpServletResponse();
        User user = testUser();

        when(jwtService.extractEmail("valid-jwt")).thenReturn("user@test.com");
        when(userService.loadUserByUsername("user@test.com")).thenReturn(user);
        when(jwtService.isTokenValid("valid-jwt", user)).thenReturn(true);

        filter.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNotNull();
        assertThat(SecurityContextHolder.getContext().getAuthentication().getPrincipal()).isEqualTo(user);
    }

    @Test
    void doFilter_validBearerToken_setsAuthentication() throws Exception {
        var request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer bearer-jwt");
        var response = new MockHttpServletResponse();
        User user = testUser();

        when(jwtService.extractEmail("bearer-jwt")).thenReturn("user@test.com");
        when(userService.loadUserByUsername("user@test.com")).thenReturn(user);
        when(jwtService.isTokenValid("bearer-jwt", user)).thenReturn(true);

        filter.doFilterInternal(request, response, filterChain);

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNotNull();
    }

    @Test
    void doFilter_cookieTakesPrecedenceOverHeader() throws Exception {
        var request = new MockHttpServletRequest();
        request.setCookies(new Cookie("vault_token", "cookie-jwt"));
        request.addHeader("Authorization", "Bearer header-jwt");
        var response = new MockHttpServletResponse();
        User user = testUser();

        when(jwtService.extractEmail("cookie-jwt")).thenReturn("user@test.com");
        when(userService.loadUserByUsername("user@test.com")).thenReturn(user);
        when(jwtService.isTokenValid("cookie-jwt", user)).thenReturn(true);

        filter.doFilterInternal(request, response, filterChain);

        verify(jwtService).extractEmail("cookie-jwt");
        verify(jwtService, never()).extractEmail("header-jwt");
    }

    @Test
    void doFilter_invalidToken_continuesWithoutAuth() throws Exception {
        var request = new MockHttpServletRequest();
        request.setCookies(new Cookie("vault_token", "bad-jwt"));
        var response = new MockHttpServletResponse();

        when(jwtService.extractEmail("bad-jwt")).thenThrow(new RuntimeException("invalid"));

        filter.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
    }

    @Test
    void doFilter_tokenValidButUserInvalid_continuesWithoutAuth() throws Exception {
        var request = new MockHttpServletRequest();
        request.setCookies(new Cookie("vault_token", "expired-jwt"));
        var response = new MockHttpServletResponse();
        User user = testUser();

        when(jwtService.extractEmail("expired-jwt")).thenReturn("user@test.com");
        when(userService.loadUserByUsername("user@test.com")).thenReturn(user);
        when(jwtService.isTokenValid("expired-jwt", user)).thenReturn(false);

        filter.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
    }

    @Test
    void doFilter_alreadyAuthenticated_skipsProcessing() throws Exception {
        var request = new MockHttpServletRequest();
        request.setCookies(new Cookie("vault_token", "valid-jwt"));
        var response = new MockHttpServletResponse();
        User user = testUser();

        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities()));
        when(jwtService.extractEmail("valid-jwt")).thenReturn("user@test.com");

        filter.doFilterInternal(request, response, filterChain);

        verify(userService, never()).loadUserByUsername(any());
        verify(filterChain).doFilter(request, response);
    }
}