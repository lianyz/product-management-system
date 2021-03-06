package by.kraskovski.pms.domain.repository;

import by.kraskovski.pms.domain.model.enums.AuthorityEnum;
import by.kraskovski.pms.domain.model.Authority;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.junit4.SpringRunner;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * Integration test for {@link AuthorityRepository}
 */
@RunWith(SpringRunner.class)
@DataJpaTest
public class AuthorityRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private AuthorityRepository authorityRepository;

    @Test
    public void findByAuthorityTest() {
        entityManager.persist(new Authority(AuthorityEnum.ROLE_USER));
        final Authority authority = authorityRepository.findByName(AuthorityEnum.ROLE_USER);
        assertNotNull(authority);
        assertEquals(authority.getName(), AuthorityEnum.ROLE_USER);
    }
}