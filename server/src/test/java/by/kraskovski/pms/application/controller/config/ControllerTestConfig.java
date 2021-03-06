package by.kraskovski.pms.application.controller.config;

import by.kraskovski.pms.application.controller.dto.TokenDto;
import by.kraskovski.pms.application.security.model.JwtAuthenticationFactory;
import by.kraskovski.pms.domain.model.enums.AuthorityEnum;
import by.kraskovski.pms.domain.model.Authority;
import by.kraskovski.pms.domain.model.User;
import by.kraskovski.pms.application.security.service.TokenService;
import by.kraskovski.pms.domain.service.AuthorityService;
import by.kraskovski.pms.domain.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.dozer.DozerBeanMapper;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.stream.Collectors;

import static by.kraskovski.pms.utils.TestUtils.prepareUserWithRole;

/**
 * Abstract configuration class for all controllers integration tests
 */
@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(locations = "classpath:application-test.properties")
public abstract class ControllerTestConfig {

    @Autowired
    protected MockMvc mvc;

    @Autowired
    private AuthorityService authorityService;

    @Autowired
    protected UserService userService;

    @Autowired
    private TokenService tokenService;

    @Autowired
    private DozerBeanMapper dozerBeanMapper;

    @Autowired
    protected ObjectMapper objectMapper;

    @Value("${auth.header.name:x-auth-token}")
    protected String authHeaderName;

    private User user;

    protected String token;

    protected void authenticateUserWithAuthority(final List<AuthorityEnum> authoritiesEnum) {
        final List<Authority> authorities = authoritiesEnum.stream()
                .map(authorityEnum -> authorityService.create(new Authority(authorityEnum)))
                .collect(Collectors.toList());
        user = userService.create(prepareUserWithRole(authorities));
        final TokenDto tokenDto = tokenService.generate(user.getUsername(), user.getPassword());
        token = tokenDto.getToken();

        final Authentication auth = JwtAuthenticationFactory.create(dozerBeanMapper.map(tokenDto.getUserDto(), User.class));
        SecurityContextHolder.getContext().setAuthentication(auth);
    }

    protected void cleanup() {
        userService.delete(user.getId());
        user.getAuthorities().forEach(authority -> authorityService.delete(authority.getId()));
    }
}
