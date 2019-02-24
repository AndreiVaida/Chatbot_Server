package configuration;

public abstract class SecurityConstraints {
    public static final String SECRET = "899 o găină să se ouă";
    public static final long EXPIRATION_TIME = 864_000_000; // 10 days
    public static final String TOKEN_PREFIX = "Bearer ";
    public static final String TOKEN_STRING = "Authorization";
    public static final String USER_STRING = "User";
}
