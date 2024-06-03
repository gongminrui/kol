package kol;

import kol.common.model.PageResponse;
import kol.money.dto.cmd.SelectWithdrawCmd;
import kol.money.model.WithdrawReview;
import kol.money.service.WithdrawReviewService;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;

/**
 * @author Gongminrui
 * @date 2023-03-03 21:30
 */
@SpringBootTest
public class WithdrawReviewServiceTest {
    @Resource
    private WithdrawReviewService withdrawReviewService;

    @Test
    public void pageWithdrawReview() {
        SelectWithdrawCmd selectWithdrawCmd = new SelectWithdrawCmd();
        selectWithdrawCmd.setPageIndex(1);
        selectWithdrawCmd.setPageSize(10);
        selectWithdrawCmd.setReview(false);
//        selectWithdrawCmd.setPass(false);
        selectWithdrawCmd.setEmail("1");
        PageResponse<WithdrawReview> withdrawReviewPageResponse =
                withdrawReviewService.pageWithdrawReview(selectWithdrawCmd);

        System.out.println();
    }
}
