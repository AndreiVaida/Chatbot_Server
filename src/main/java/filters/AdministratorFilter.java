package filters;

import org.springframework.security.access.AuthorizationServiceException;
import services.api.UserPermissionService;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import java.io.IOException;

public class AdministratorFilter implements Filter {
    private final UserPermissionService userPermissionService;

    public AdministratorFilter(UserPermissionService userPermissionService) {
        this.userPermissionService = userPermissionService;
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        if (!userPermissionService.isAdministrator()) {
            throw new AuthorizationServiceException("Not an administrator");
        }

        chain.doFilter(request, response);
    }
}
