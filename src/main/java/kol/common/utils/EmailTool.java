package kol.common.utils;

import kol.config.service.ConfigService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.amazonaws.services.simpleemail.AmazonSimpleEmailService;
import com.amazonaws.services.simpleemail.model.Body;
import com.amazonaws.services.simpleemail.model.Content;
import com.amazonaws.services.simpleemail.model.Destination;
import com.amazonaws.services.simpleemail.model.Message;
import com.amazonaws.services.simpleemail.model.SendEmailRequest;

@Component
@Slf4j
public class EmailTool {

    @Autowired
    private AmazonSimpleEmailService mailService;
    @Autowired
    private ConfigService configService;
//    private String from = "noreply@glodfox.com";

    public void sendMessage(String to, String subject, String msg) {
        subject = StringUtil.replace("{1} {2}", configService.getPlatformName(), subject);
        Destination destination = new Destination().withToAddresses(to);
        Content subj = new Content().withData(subject);
        Content textBody = new Content().withData(msg);
        Body body = new Body().withHtml(textBody);
        Message message = new Message().withSubject(subj).withBody(body);
        SendEmailRequest req = new SendEmailRequest().withSource(configService.getEmailFrom())

                .withDestination(destination).withMessage(message);

        try {
            mailService.sendEmail(req);
        } catch (Exception e) {
            e.printStackTrace();
            log.error("发送邮件失败: ", e);
        }
        log.info("{} 验证码: {}", to, msg);
    }
}
