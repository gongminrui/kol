package kol.auth.model;

import kol.account.model.Account;
import kol.account.model.AccountStatusEnum;
import kol.account.repo.AccountRepo;
import kol.common.cache.GlobalCache;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.time.Duration;
import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

/**
 * 真正token验证的地方
 *
 * @author admin
 */
@Component
@Slf4j
public class TokenAuthenticationProvider implements AuthenticationProvider {

    @Resource
    private AuthService authService;
    @Resource
    private AccountRepo accountRepo;

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {

        Object header = authentication.getPrincipal();
        if (header == null || StringUtils.isEmpty(header.toString())) {
            throw new AccessDeniedException("无权访问");
        }

        String encryptToken = header.toString().trim();
//        String token = encryptToken;

        String[] tokenAndTime = authService.decryptToken(encryptToken).split(":");

        String token = tokenAndTime[0];
        long time = Long.valueOf(tokenAndTime[1]);
        long timeDiff = Duration.between(Instant.ofEpochMilli(time), Instant.now()).toMinutes();
        log.info("token {} timstamp {} timeDiff {}", token, time, timeDiff);
        if (timeDiff > 1000) {
            throw new BadCredentialsException("无权访问");
        }

        Account account = GlobalCache.getAccount(token);
        if (ObjectUtils.isEmpty(account)) {
            throw new BadCredentialsException("无权访问");
        }

        List<GrantedAuthority> authorities = Arrays.asList(() -> account.getRole().name());
        return new TokenAuthentication(account.getId(), authorities);
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return TokenAuthentication.class.isAssignableFrom(authentication);
    }

}
