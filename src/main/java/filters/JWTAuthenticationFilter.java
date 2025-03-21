package filters;

import com.auth0.jwt.JWT;
import com.fasterxml.jackson.databind.ObjectMapper;
import domain.entities.User;
import dtos.UserDto;
import facades.api.UserFacade;
import mappers.UserMapper;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import javax.servlet.FilterChain;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;

import static com.auth0.jwt.algorithms.Algorithm.HMAC512;
import static configuration.SecurityConstraints.EXPIRATION_TIME;
import static configuration.SecurityConstraints.SECRET;
import static configuration.SecurityConstraints.TOKEN_PREFIX;
import static configuration.SecurityConstraints.TOKEN_STRING;
import static configuration.SecurityConstraints.USER_STRING;
import static org.apache.commons.codec.CharEncoding.UTF_8;

public class JWTAuthenticationFilter extends UsernamePasswordAuthenticationFilter {
    private final AuthenticationManager authenticationManager;
    private final UserFacade userFacade;

    public JWTAuthenticationFilter(AuthenticationManager authenticationManager, UserFacade userFacade) {
        this.authenticationManager = authenticationManager;
        this.userFacade = userFacade;
    }

    @Override
    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response) throws AuthenticationException {
        try {
            final User user = new ObjectMapper().readValue(request.getInputStream(), User.class);

            return authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            user.getEmail(),
                            user.getPassword(),
                            new ArrayList<>())
            );
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    protected void successfulAuthentication(HttpServletRequest request, HttpServletResponse response, FilterChain chain, Authentication authentication) {
        org.springframework.security.core.userdetails.User authenticatedUser = (org.springframework.security.core.userdetails.User) authentication.getPrincipal();
        final UserDto userDto = userFacade.findUserByEmail(authenticatedUser.getUsername());
        final String token = JWT.create()
                .withSubject(authenticatedUser.getUsername())
                .withClaim("userId", userDto.getId())
                .withExpiresAt(new Date(System.currentTimeMillis() + EXPIRATION_TIME))
                .sign(HMAC512(SECRET.getBytes()));
        response.setCharacterEncoding(UTF_8);

        final String responseBody = "{" +
                "\"" + TOKEN_STRING + "\":\"" + TOKEN_PREFIX + token + "\"," +
                "\"" + USER_STRING + "\":" + UserMapper.userDtoToJson(userDto).toString() +
                "}";

        try {
            response.getWriter().write(responseBody);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
