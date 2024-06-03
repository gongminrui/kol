package kol;

import kol.auth.model.TokenAuthentication;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.ArrayList;

/**
 * @author Gongminrui
 * @date 2023-05-28 21:24
 */
public class BaseTest {

    public void setLogin(Long accountId) {
        Authentication authentication = new TokenAuthentication(accountId, new ArrayList<>());
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }
}
