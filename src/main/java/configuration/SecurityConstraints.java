package configuration;

public abstract class SecurityConstraints {
    public static final String SECRET = "899 o găină să se ouă";
    public static final long EXPIRATION_TIME = 864_000_000; // 10 days
    public static final String TOKEN_PREFIX = "Bearer ";
    public static final String TOKEN_STRING = "Authorization";
    public static final String USER_STRING = "User";
    static final String REGISTER_URL = "/users";
    static final String CHAT_GUEST_URL = "/chat/guest";
    static final String CHAT_SAMPLE_URL = "/chat/sample";
    static final String USER_GET_URL = "/users/{userId}";
}
