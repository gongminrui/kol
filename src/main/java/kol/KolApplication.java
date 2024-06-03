package kol;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Bean;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.web.filter.CommonsRequestLoggingFilter;

import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.simpleemail.AmazonSimpleEmailService;
import com.amazonaws.services.simpleemail.AmazonSimpleEmailServiceClientBuilder;

import lombok.extern.slf4j.Slf4j;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

/**
 * @author guan
 */
@SpringBootApplication
@EnableJpaAuditing
@EnableSwagger2
@EnableFeignClients
@Slf4j
public class KolApplication {


    public static void main(String[] args) {
        SpringApplication.run(KolApplication.class, args);
    }

    @Bean
    public CommonsRequestLoggingFilter logFilter() {
        CommonsRequestLoggingFilter filter = new CommonsRequestLoggingFilter();
        filter.setIncludeQueryString(true);
        filter.setIncludePayload(true);
        filter.setMaxPayloadLength(10000);
        filter.setIncludeHeaders(false);
        filter.setAfterMessagePrefix("REQUEST DATA : ");
        return filter;
    }

    @Bean
    public AmazonSimpleEmailService amazonSimpleEmailService() {
        AWSCredentialsProvider credentialsProvider = new AWSStaticCredentialsProvider(new BasicAWSCredentials("AKIAS4KMRKJTXXEJWNWZ", "DWIQHcdGexiCOgOS9W5hLn4YQSzmhwOiS41X0HL2"));
        return AmazonSimpleEmailServiceClientBuilder.standard().withCredentials(credentialsProvider)
                .withRegion(Regions.AP_NORTHEAST_1).build();
    }
}
