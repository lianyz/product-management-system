package by.kraskovski.pms.application.security.service;

import by.kraskovski.pms.application.controller.dto.TokenDto;
import by.kraskovski.pms.application.controller.dto.UserDto;
import by.kraskovski.pms.domain.model.User;
import by.kraskovski.pms.domain.service.UserService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.JwtBuilder;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.SignatureException;
import org.apache.commons.lang3.StringUtils;
import org.dozer.Mapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

@Service
public class JwtService implements TokenService {

    @Value("${secret.key:JKGuhygvuh2v}")
    private String secretKey;

    @Value("${auth.header.name:x-auth-token}")
    private String authHeaderName;

    @Value("${token.expiration.time:60}")
    private int expirationTime;

    private final UserService userService;
    private final Mapper mapper;

    @Autowired
    public JwtService(final UserService userService, final Mapper mapper) {
        this.userService = userService;
        this.mapper = mapper;
    }

    @Override
    public TokenDto generate(final String username, final String password) {
        if (StringUtils.isBlank(username) || StringUtils.isBlank(password)) {
            throw new BadCredentialsException("Input data can't be empty.");
        }
        final User user = userService.findByUsername(username);

        validateInputPassword(user.getPassword(), password);

        final Map<String, Object> tokenData = new HashMap<>();
        tokenData.put("username", user.getUsername());
        tokenData.put("password", user.getPassword());
        tokenData.put("create_date", LocalDateTime.now());
        final JwtBuilder jwtBuilder = Jwts.builder();
        jwtBuilder.setClaims(tokenData);
        final Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.MINUTE, expirationTime);
        jwtBuilder.setExpiration(calendar.getTime());
        final String token = jwtBuilder.signWith(SignatureAlgorithm.HS512, secretKey).compact();
        return new TokenDto(token, mapper.map(user, UserDto.class));
    }

    private void validateInputPassword(final String userPassword, final String inputPassword) {
        final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
        if (!passwordEncoder.matches(inputPassword, userPassword) && !inputPassword.equals(userPassword)) {
            throw new BadCredentialsException("Incorrect password");
        }
    }

    @Override
    public Authentication authenticate(final HttpServletRequest request) throws ExpiredJwtException, SignatureException {
        final String token = request.getHeader(authHeaderName);
        return StringUtils.isNotBlank(token) ? parseToken(token) : null;
    }

    private User parseToken(final String token) {
        final Jws<Claims> tokenData = Jwts.parser().setSigningKey(secretKey).parseClaimsJws(token);
        final User user = getUserFromToken(tokenData);
        if (validatePasswordFromToken(tokenData, user.getPassword())) {
            user.setAuthenticated(true);
            return user;
        }
        return null;
    }

    private User getUserFromToken(final Jws<Claims> tokenData) throws UsernameNotFoundException {
        final String username = tokenData.getBody().get("username").toString();
        return userService.findByUsername(username);
    }

    private boolean validatePasswordFromToken(final Jws<Claims> tokenData, final String userPassword) {
        final String tokenPassword = tokenData.getBody().get("password").toString();
        return tokenPassword.equals(userPassword);
    }
}
