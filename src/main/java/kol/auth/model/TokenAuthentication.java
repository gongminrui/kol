package kol.auth.model;

import lombok.Getter;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;

import java.util.Collection;

/**
 * 自定义token
 *
 * @author guanzhenggang@gmail.com
 */
public class TokenAuthentication implements Authentication {

    /**
     *
     */
    private static final long serialVersionUID = 1L;
    @Getter
    private Object token;
    private Collection<? extends GrantedAuthority> grantedAuthorities;

    public TokenAuthentication(Object token, Collection<? extends GrantedAuthority> grantedAuthorities) {
        this.token = token;
        this.grantedAuthorities = grantedAuthorities;
    }

    @Override
    public String getName() {
        return token.toString();
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return grantedAuthorities;
    }

    @Override
    public Object getCredentials() {
        return token;
    }

    @Override
    public Object getDetails() {
        return null;
    }

    @Override
    public Object getPrincipal() {
        return token;
    }

    @Override
    public boolean isAuthenticated() {
        return grantedAuthorities != null;
    }

    @Override
    public void setAuthenticated(boolean isAuthenticated) throws IllegalArgumentException {

    }

}
