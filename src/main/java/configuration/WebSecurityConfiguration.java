package configuration;

import com.google.common.collect.ImmutableList;
import facades.api.UserFacade;
import filters.AdministratorFilter;
import filters.JWTAuthenticationFilter;
import filters.JWTAuthorizationFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import services.api.UserPermissionService;
import services.impl.UserDetailsServiceImpl;

import static configuration.SecurityConstraints.CHAT_GUEST_URL;
import static configuration.SecurityConstraints.CHAT_SAMPLE_URL;
import static configuration.SecurityConstraints.REGISTER_URL;
import static configuration.SecurityConstraints.USER_GET_URL;

@EnableWebSecurity
@ComponentScan(basePackages = {"services", "filters"})
public class WebSecurityConfiguration extends WebSecurityConfigurerAdapter {
    private final UserFacade userFacade;
    private final UserDetailsServiceImpl userDetailsService;
    private final UserPermissionService userPermissionService;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;

    @Autowired
    public WebSecurityConfiguration(UserFacade userFacade, UserDetailsServiceImpl userDetailsService, UserPermissionService userPermissionService, BCryptPasswordEncoder bCryptPasswordEncoder) {
        this.userFacade = userFacade;
        this.userDetailsService = userDetailsService;
        this.userPermissionService = userPermissionService;
        this.bCryptPasswordEncoder = bCryptPasswordEncoder;
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.cors().and().csrf().disable().authorizeRequests()
                .antMatchers(HttpMethod.GET, CHAT_SAMPLE_URL, USER_GET_URL).permitAll()
                .antMatchers(HttpMethod.POST, "/login", REGISTER_URL, CHAT_GUEST_URL).permitAll()
                .antMatchers(HttpMethod.PUT).authenticated()
                .antMatchers(HttpMethod.DELETE).authenticated()
                .anyRequest().authenticated()
                .and()
                .addFilter(new JWTAuthenticationFilter(authenticationManager(), userFacade))
                .addFilter(new JWTAuthorizationFilter(authenticationManager()))
                // this disables session creation on Spring Security
                .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS);
    }

    @Override
    public void configure(AuthenticationManagerBuilder auth) throws Exception {
        auth.userDetailsService(userDetailsService).passwordEncoder(bCryptPasswordEncoder);
    }

    @Bean
    CorsConfigurationSource corsConfigurationSource() {
        final CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(ImmutableList.of("*"));
        configuration.setAllowedMethods(ImmutableList.of("HEAD",
                "GET", "POST", "PUT", "DELETE", "PATCH"));
        // setAllowCredentials(true) is important, otherwise:
        // The value of the 'Access-Control-Allow-Origin' header in the response must not be the wildcard '*' when the request's credentials mode is 'include'.
        configuration.setAllowCredentials(true);
        // setAllowedHeaders is important! Without it, OPTIONS preflight request
        // will fail with 403 Invalid CORS request
        configuration.setAllowedHeaders(ImmutableList.of("Authorization", "Cache-Control", "Content-Type", "X-Requested-With", "Origin", "Accept"));

        final UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
//        final UrlBasedCorsConfigurationSource messageSource = new UrlBasedCorsConfigurationSource();
//        messageSource.registerCorsConfiguration("/**", new CorsConfiguration().applyPermitDefaultValues());
        return source;
    }

    @Bean
    public FilterRegistrationBean<AdministratorFilter> loggingFilter(){
        FilterRegistrationBean<AdministratorFilter> registrationBean = new FilterRegistrationBean<>();

        registrationBean.setFilter(new AdministratorFilter(userPermissionService));
        registrationBean.addUrlPatterns("/admin/*");

        return registrationBean;
    }
}
