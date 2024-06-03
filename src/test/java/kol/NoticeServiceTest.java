package kol;

import kol.message.model.Notice;
import kol.message.service.NoticeService;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;
import java.util.List;

/**
 * @author Gongminrui
 * @date 2023-03-17 21:13
 */
@SpringBootTest
public class NoticeServiceTest {
    @Resource
    private NoticeService noticeService;

    @Test
    public void getNotices() {
        List<Notice> notices = noticeService.getNotices();
        System.out.println();
    }
}
