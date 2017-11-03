package by.kraskovski.pms.domain.service;

import by.kraskovski.pms.domain.model.enums.AuthorityEnum;
import by.kraskovski.pms.domain.model.Authority;

/**
 * Service for {@link Authority}.
 */
public interface AuthorityService extends CRUDService<Authority> {

    /**
     * Find authority in database by name.
     */
    Authority findByName(AuthorityEnum name);
}
