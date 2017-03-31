package by.intexsoft.controller;

import by.intexsoft.model.User;
import by.intexsoft.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.*;

/**
 * Controller for the {@link UserService}
 */
@RestController
@RequestMapping("/user")
public class UserController {

    private static final Logger LOGGER = LoggerFactory.getLogger(UserController.class);

    private final UserService userService;

    @Autowired
    public UserController(UserService userService) {
        this.userService = userService;
    }

    /**
     * Return json-information about all users in database
     *
     * @return list of {@link User}s
     */
    @RequestMapping
    public ResponseEntity<?> loadAllUsers() {
        LOGGER.info("Start loadAllUsers");
        try {
            return new ResponseEntity<>(userService.findAll(), HttpStatus.OK);
        } catch (DataAccessException e) {
            LOGGER.error("Exception in loadAllUsers. " + e.getLocalizedMessage());
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }

    /**
     * Return json-information about all users in database
     *
     * @return list of {@link User}s
     */
    @RequestMapping(value = "/{id}", method = RequestMethod.GET)
    public ResponseEntity<?> loadUserById(@PathVariable("id") Integer id) {
        LOGGER.info("Start loadUserById");
        try {
            User user = userService.find(id);
            Assert.notNull(user, "Unable to find user with id: " + id);
            return new ResponseEntity<>(user, HttpStatus.OK);
        } catch (IllegalArgumentException e) {
            LOGGER.error(e.getLocalizedMessage());
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }

    /**
     * Find user in database with setting name in browser
     *
     * @return entity of {@link User}
     */
    @RequestMapping(value = "/username/{username}", method = RequestMethod.GET)
    public ResponseEntity<?> loadUserByUsername(@PathVariable String username) {
        LOGGER.info("Start loadUserByUsername: " + username);
        try {
            User user = userService.findByUsername(username);
            Assert.notNull(user, "Unable to find user with username: " + username);
            return new ResponseEntity<>(user, HttpStatus.OK);
        } catch (IllegalArgumentException e) {
            LOGGER.error(e.getLocalizedMessage());
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }

    /**
     * Creating {@link User} from client form
     *
     * @param user model
     */
    @RequestMapping(method = RequestMethod.POST)
    public ResponseEntity<?> createUser(@RequestBody User user) {
        LOGGER.info("Start createUser: " + user.username);
        try {
            return new ResponseEntity<>(userService.create(user), HttpStatus.CREATED);
        } catch (DataAccessException e) {
            LOGGER.error("Exception in createUser. " + e.getLocalizedMessage());
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }

    /**
     * Update {@link User} entity in database
     *
     * @param user model
     */
    @RequestMapping(method = RequestMethod.PUT)
    public ResponseEntity<?> updateUser(@RequestBody User user) {
        LOGGER.info("Start updateUser: " + user.username);
        try {
            return new ResponseEntity<>(userService.update(user), HttpStatus.OK);
        } catch (DataAccessException e) {
            LOGGER.error("Exception while updating user with id" + user.id + ". " + e.getLocalizedMessage());
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }

    /**
     * Delete {@link User} from database by identifier
     *
     * @param id
     */
    @RequestMapping(value = "/{id}", method = RequestMethod.DELETE)
    public ResponseEntity<?> deleteUser(@PathVariable("id") Integer id) {
        LOGGER.info("Start deleteUser with id: " + id);
        try {
            userService.delete(id);
            return new ResponseEntity<>(HttpStatus.OK);
        } catch (DataAccessException e) {
            LOGGER.error("Exception while delete user with id: " + id + ". " + e.getLocalizedMessage());
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }
}
