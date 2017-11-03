package by.kraskovski.pms.domain.service;

import by.kraskovski.pms.application.security.exception.UserNotFoundException;
import by.kraskovski.pms.domain.model.User;
import by.kraskovski.pms.domain.model.enums.AuthorityEnum;
import by.kraskovski.pms.domain.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.time.LocalDateTime;
import java.util.List;

import static java.lang.String.format;
import static java.util.Collections.singletonList;
import static java.util.Optional.ofNullable;

@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class DefaultUserService implements UserService {

    private final UserRepository userRepository;
    private final AuthorityService authorityService;
    private final BCryptPasswordEncoder encoder;

    @Override
    public User create(final User createUser) {
        createUser.setCreateDate(LocalDateTime.now());
        createUser.setPassword(encoder.encode(createUser.getPassword()));

        processAuthorities(createUser);

        return userRepository.save(createUser);
    }

    @Override
    public User find(final String id) {
        return ofNullable(userRepository.findOne(id))
                .orElseThrow(() -> new UserNotFoundException(format("User with id: \"%s\" doesn't exists in db!", id)));
    }

    @Override
    public User findByUsername(final String username) {
        return ofNullable(userRepository.findByUsername(username))
                .orElseThrow(() -> new UserNotFoundException(format("\"%s\" doesn't exists in db!", username)));
    }

    @Override
    public List<User> findAll() {
        return userRepository.findAll();
    }

    @Override
    public User update(final User updateUser) {
        final User oldUser = find(updateUser.getId());

        if (!encoder.matches(updateUser.getPassword(), oldUser.getPassword())
                && !oldUser.getPassword().equals(updateUser.getPassword())) {
            updateUser.setPassword(encoder.encode(updateUser.getPassword()));
        }

        processAuthorities(updateUser);
        return userRepository.save(updateUser);
    }

    @Override
    public void delete(final String id) {
        try {
            userRepository.delete(id);
        } catch (EmptyResultDataAccessException e) {
            throw new UserNotFoundException(e.getLocalizedMessage());
        }
    }

    @Override
    public void deleteAll() {
        userRepository.deleteAll();
    }

    private void processAuthorities(final User object) {
        if (CollectionUtils.isEmpty(object.getAuthorities())) {
            object.setAuthorities(singletonList(authorityService.findByName(AuthorityEnum.ROLE_USER)));
        }
    }
}
