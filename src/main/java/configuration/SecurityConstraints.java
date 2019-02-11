package configuration;

public abstract class SecurityConstraints {
    public static final String SECRET = "399_o_găină_să_se_ouă";
    public static final long EXPIRATION_TIME = 864_000_000; // 10 days
    public static final String TOKEN_PREFIX = "Bearer ";
    public static final String HEADER_STRING = "Authorization";
    public static final String SIGN_UP_URL = "/users";
}
