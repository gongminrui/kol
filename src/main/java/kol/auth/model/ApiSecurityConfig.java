package kol.auth.model;

import kol.common.model.Response;
import kol.trade.controller.TradingController;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.firewall.StrictHttpFirewall;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;


/**
 * 这里尝试用最新版的SpringSecurity配置，但是不知道怎么获取AuthenticationManager
 *
 * @author admin
 */
@SuppressWarnings("deprecation")
@Configuration
@EnableGlobalMethodSecurity(securedEnabled = true, jsr250Enabled = true, prePostEnabled = true)
public class ApiSecurityConfig extends WebSecurityConfigurerAdapter {

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http
                .antMatcher("/api/**")
                .authorizeRequests()
                .antMatchers(HttpMethod.GET, "/api/auth/encrypt-token")
                .permitAll()
                .antMatchers("/api/auth/login"
                        , "/api/account/register"
                        , "/api/notice/getNotices"
                        , "/api/vcode/send"
                        , "/api/trading/market/postOrder"
                        ,"/api/position/modify"
                        ,"/api/recharge"
//                        , TradingController.API_URL + TradingController.MOTHED_DISPENSE_ORDER_URL
                        , "/druid/**")
                .permitAll()
                .anyRequest()
                .authenticated()
                .and()
                .formLogin().disable()
                .httpBasic().disable()
                .cors()
                .and()
                .csrf()
                .disable()
                .sessionManagement()
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                .and()
                .exceptionHandling()
                .accessDeniedHandler((req, resp, ex) -> outputAccessDeniedMessage(resp))
                .authenticationEntryPoint((req, resp, ex) -> outputAccessDeniedMessage(resp)
                );
        http.addFilterBefore(new TokenAuthenticationFilter(authenticationManagerBean()), UsernamePasswordAuthenticationFilter.class);

    }

    @Override
    public void configure(WebSecurity web) throws Exception {
        StrictHttpFirewall firewall = new StrictHttpFirewall();
        //去掉";"黑名单
        firewall.setAllowSemicolon(true);
        //加入自定义的防火墙
        web.httpFirewall(firewall);
        super.configure(web);
    }

    @Bean
    @Override
    public AuthenticationManager authenticationManagerBean() throws Exception {
        // 解决authenticationManager 无法注入
        return super.authenticationManagerBean();
    }

    @Bean
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    private void outputAccessDeniedMessage(HttpServletResponse resp) throws IOException {
        resp.setContentType("application/json;charset=UTF-8");
        resp.setCharacterEncoding("UTF-8");
        Response message = Response.buildFailure("ACCESS_DENIED", "无权访问");
        resp.getWriter().println(message.toJsonStr());
        resp.flushBuffer();
    }
}
