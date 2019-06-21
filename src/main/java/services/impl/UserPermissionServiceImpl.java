package services.impl;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import repositories.UserRepository;
import services.api.UserPermissionService;

import javax.transaction.Transactional;
import java.util.Map;

@Service
public class UserPermissionServiceImpl implements UserPermissionService {
    private final UserRepository userRepository;

    public UserPermissionServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public boolean hasUserAccess(long userId) {
        long authenticatedUserId = getAuthenticatedUserId();
        return authenticatedUserId == userId;
    }

    @Override
    @Transactional
    public boolean isAdministrator() {
        long authenticatedUserId = getAuthenticatedUserId();
        return userRepository.findAllByIsAdministrator(true).stream()
                .anyMatch(user -> user.getId().equals(authenticatedUserId));
    }

    /**
     * @return the logged user id or -1 if no token has provided in request
     */
    private long getAuthenticatedUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        try {
            final Map<String, Object> details = (Map<String, Object>) authentication.getDetails();
            return (long) details.get("userId");
        }
        catch (ClassCastException e) {
            return -1;
        }
    }
}
