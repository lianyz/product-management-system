package by.kraskovski.pms.application.controller;

import by.kraskovski.pms.application.controller.config.ControllerTestConfig;
import by.kraskovski.pms.domain.model.Authority;
import by.kraskovski.pms.domain.model.enums.AuthorityEnum;
import by.kraskovski.pms.domain.service.AuthorityService;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import static by.kraskovski.pms.domain.model.enums.AuthorityEnum.ROLE_ADMIN;
import static org.hamcrest.Matchers.is;
import static org.springframework.http.MediaType.APPLICATION_JSON_UTF8;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Transactional
public class AuthorityControllerIT extends ControllerTestConfig {

    private static final String BASE_AUTHORITY_URL = "/authority";

    @Autowired
    private AuthorityService authorityService;

    @Test
    public void loadAuthoritiesIfPresentTest() throws Exception {
        authenticateUserWithAuthority(ROLE_ADMIN);
        final Authority authority = authorityService.create(new Authority(AuthorityEnum.ROLE_USER));
        mvc.perform(get(BASE_AUTHORITY_URL)
                .header(authHeaderName, token))
                .andExpect(status().isOk())
                .andExpect(content().contentType(APPLICATION_JSON_UTF8))
                .andExpect(jsonPath("$[1].id", is(authority.getId())))
                .andExpect(jsonPath("$[1].authority", is(authority.getAuthority())));
    }
}