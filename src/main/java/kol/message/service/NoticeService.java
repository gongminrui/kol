package kol.message.service;

import java.util.Date;

import kol.common.utils.HttpUtil;
import kol.message.model.Notice;
import kol.message.repo.NoticeRepo;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

/**
 * @author Gongminrui
 * @date 2023-03-03 9:59
 */
@Service
public class NoticeService {
    @Resource
    private NoticeRepo noticeRepo;

    public void addNotice(String content) {
        Notice notice = new Notice();
        notice.setNoticeContent(content);

        noticeRepo.save(notice);
    }

    public List<Notice> getNotices() {
        List<Notice> list = noticeRepo.findAll(null, Sort.by("createdAt").descending());
        list.forEach(v -> {
            if (HttpUtil.isEn()) {
                v.setNoticeContent(v.getEnContent());
            }
        });
        return list;
    }
}
