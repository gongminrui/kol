package kol.auth.model;

import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * 从request请求中取出token,访问spring security的认证上下文中
 * 注意：这里只做了参数提取，并没有做真正的认证，真正的认证在{@link TokenAuthenticationProvider}
 *
 * @author admin
 */
public class TokenAuthenticationFilter extends BasicAuthenticationFilter {

    public TokenAuthenticationFilter(AuthenticationManager authenticationManager) {
        super(authenticationManager);
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        try {
            Object token = request.getHeader(HttpHeaders.AUTHORIZATION);
            if (token == null || StringUtils.isEmpty(token.toString()) || token.toString().contains("Basic")) {
                //让其他过滤器处理
                doFilter(request, response, chain);
                return;
            }
            Authentication authentication = getAuthenticationManager().authenticate(new TokenAuthentication(token, null));
            //认证成功
            SecurityContext context = SecurityContextHolder.createEmptyContext();
            context.setAuthentication(authentication);
            SecurityContextHolder.setContext(context);
            onSuccessfulAuthentication(request, response, authentication);
        } catch (AuthenticationException ex) {
            SecurityContextHolder.clearContext();
            onUnsuccessfulAuthentication(request, response, ex);
        } catch (AccessDeniedException ex) {
            throw ex;
        }

        chain.doFilter(request, response);
    }

}
