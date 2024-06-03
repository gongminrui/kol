package kol.auth.model;

import io.swagger.annotations.ApiOperation;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.authentication.www.BasicAuthenticationEntryPoint;
import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.service.ApiKey;
import springfox.documentation.service.AuthorizationScope;
import springfox.documentation.service.SecurityReference;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spi.service.contexts.SecurityContext;
import springfox.documentation.spring.web.plugins.Docket;

import javax.annotation.Resource;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

/**
 * 配置swagger文档信息和访问权限
 * @author guanzhenggang@gmail.com
 *
 */
@SuppressWarnings("deprecation")
@Configuration
@Order(2)
public class SwaggerSecurityConfig  extends WebSecurityConfigurerAdapter {
	@Resource
	private PasswordEncoder encoder;

	@Bean
	public Docket createRestApi() {
		return new Docket(DocumentationType.OAS_30).apiInfo(apiInfo())
				.securityContexts(securityContext())
				.securitySchemes(Arrays.asList(new ApiKey("Authorization", "Authorization", "header")))
				.select()
				.apis(RequestHandlerSelectors.withMethodAnnotation(ApiOperation.class))
				.paths(PathSelectors.any())
				.build();
//                .protocols(new HashSet<>(){{
//					add("https");
//					add("http");
//				}});
	}

	private List<SecurityContext> securityContext() {
		return Arrays.asList(SecurityContext.builder()
				.securityReferences(Arrays.asList(SecurityReference.builder()
						.reference("Authorization")
						.scopes(new AuthorizationScope[] { new AuthorizationScope("global", "accessEverything") })
						.build()))
				.build());

	}

	private ApiInfo apiInfo() {
		return new ApiInfoBuilder().title("KOL API")
				.description("带单系统接口文档")
				.version("1.0")
				.build();
	}

	@Override
	protected void configure(AuthenticationManagerBuilder auth) throws Exception {
		auth.inMemoryAuthentication().withUser("swagger").password(encoder.encode("abcd.1234"))

		.authorities(()->"swagger");
	}

	@Override
	public void configure(HttpSecurity http) throws Exception {
		 http.antMatcher("/swagger-ui/**")
         .authorizeRequests()
         .anyRequest().authenticated()
         .and().httpBasic().authenticationEntryPoint(authenticationEntryPoint());
	}

	@Bean
    public AuthenticationEntryPoint authenticationEntryPoint(){
        BasicAuthenticationEntryPoint entryPoint =
          new BasicAuthenticationEntryPoint();
        entryPoint.setRealmName("swagger");
        return entryPoint;
    }
}
