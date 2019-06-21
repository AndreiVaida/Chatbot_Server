package services.api;

public interface UserPermissionService {
    /**
     * @return true if the given ID is the ID from the token and false otherwise
     */
    boolean hasUserAccess(long userId);

    /**
     * @return true if the logged user (from token) and false otherwise
     */
    boolean isAdministrator();
}
