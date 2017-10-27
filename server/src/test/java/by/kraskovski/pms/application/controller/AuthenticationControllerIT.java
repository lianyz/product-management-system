package by.kraskovski.pms.application.controller;

import by.kraskovski.pms.application.controller.config.ControllerTestConfig;
import by.kraskovski.pms.application.controller.dto.LoginDto;
import by.kraskovski.pms.domain.model.User;
import org.junit.After;
import org.junit.Test;
import org.springframework.http.MediaType;
import org.springframework.transaction.annotation.Transactional;

import static by.kraskovski.pms.utils.TestUtils.prepareUser;
import static org.apache.commons.lang3.RandomStringUtils.randomAlphabetic;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.isEmptyString;
import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Transactional
public class AuthenticationControllerIT extends ControllerTestConfig {

    private static final String BASE_AUTH_URL = "/auth";

    @After
    public void cleanUp() {
        userService.deleteAll();
    }

    @Test
    public void loginWithFullValidDataTest() throws Exception {
        final User user = userService.create(prepareUser());
        mvc.perform(post(BASE_AUTH_URL + "/login")
                .contentType(MediaType.APPLICATION_JSON_UTF8)
                .content(objectMapper.writeValueAsString(new LoginDto(user.getUsername(), user.getPassword()))))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(jsonPath("$.token", notNullValue(String.class)))
                .andExpect(jsonPath("$.userDto.username", is(user.getUsername())))
                .andExpect(jsonPath("$.userDto.password", isEmptyString()));
    }

    @Test
    public void loginWithFullInvalidDataTest() throws Exception {
        mvc.perform(post(BASE_AUTH_URL + "/login")
                .contentType(MediaType.APPLICATION_JSON_UTF8)
                .content(objectMapper.writeValueAsString(new LoginDto(randomAlphabetic(10), randomAlphabetic(10)))))
                .andExpect(status().isNotFound());
    }

    @Test
    public void loginWithInvalidPasswordTest() throws Exception {
        final User user = userService.create(prepareUser());
        mvc.perform(post(BASE_AUTH_URL + "/login")
                .contentType(MediaType.APPLICATION_JSON_UTF8)
                .content(objectMapper.writeValueAsString(new LoginDto(user.getUsername(), randomAlphabetic(10)))))
                .andExpect(status().isUnauthorized());
    }
}
