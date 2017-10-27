package by.kraskovski.pms.domain.service.impl;

import by.kraskovski.pms.application.security.exception.UserNotFoundException;
import by.kraskovski.pms.domain.model.Authority;
import by.kraskovski.pms.domain.model.User;
import by.kraskovski.pms.domain.model.enums.AuthorityEnum;
import by.kraskovski.pms.domain.repository.UserRepository;
import by.kraskovski.pms.domain.service.AuthorityService;
import by.kraskovski.pms.domain.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.time.LocalDateTime;
import java.util.List;

import static java.util.Collections.singletonList;
import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.toList;

@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final AuthorityService authorityService;
    private static final BCryptPasswordEncoder PASSWORD_ENCODER = new BCryptPasswordEncoder();

    @Override
    public User create(final User object) {
        object.setId(null);
        object.setCreateDate(LocalDateTime.now());
        object.setPassword(PASSWORD_ENCODER.encode(object.getPassword()));
        if (CollectionUtils.isEmpty(object.getAuthorities())) {
            object.setAuthorities(singletonList(authorityService.findByName(AuthorityEnum.ROLE_USER)));
        }

        return userRepository.save(object);
    }

    @Override
    public User find(final String id) {
        return ofNullable(userRepository.findOne(id))
                .orElseThrow(() -> new UserNotFoundException("User with id: \"" + id + "\" doesn't exists in db!"));
    }

    @Override
    public User findByUsername(final String username) {
        return ofNullable(userRepository.findByUsername(username))
                .orElseThrow(() -> new UserNotFoundException("User with username: \"" + username + "\" doesn't exists in db!"));
    }

    @Override
    public List<User> findAll() {
        return userRepository.findAll();
    }

    @Override
    public User update(final User updateUser) {
        final User oldUser = find(updateUser.getId());
        final String encodedPassword = verifyPassword(oldUser.getPassword(), updateUser.getPassword());
        updateUser.setPassword(encodedPassword);
        verifyAuthorities(oldUser, updateUser);
        return userRepository.save(updateUser);
    }

    @Override
    public void delete(final String id) {
        try {
            userRepository.delete(id);
        } catch (EmptyResultDataAccessException e) {
            throw new UserNotFoundException("User with id: \"" + id + "\" doesn't exists in db!");
        }
    }

    @Override
    public void deleteAll() {
        userRepository.deleteAll();
    }

    private String verifyPassword(final String oldPassword, final String newPassword) {
        if (!PASSWORD_ENCODER.matches(newPassword, oldPassword)
                && !oldPassword.equals(newPassword)) {
            return PASSWORD_ENCODER.encode(newPassword);
        }
        return oldPassword;
    }

    private void verifyAuthorities(final User currentUser, final User updateUser) {
        final List<Authority> updateAuthorities = ofNullable(updateUser.getAuthorities())
                .orElseThrow(() -> new IllegalArgumentException("Can't verify user authorities!"));

        if (CollectionUtils.isEmpty(updateAuthorities)) {
            updateUser.setAuthorities(singletonList(authorityService.findByName(AuthorityEnum.ROLE_USER)));
        } else if (!currentUser.getAuthorities().equals(updateAuthorities)){
            updateUser.setAuthorities(updateAuthorities.stream()
                    .map(authority -> authorityService.find(authority.getId()))
                    .collect(toList()));
        }
    }
}
