package kol.account.service;

import com.alibaba.fastjson.JSON;
import kol.account.dto.vo.AccountRebateVo;
import kol.account.model.AccountRelation;
import kol.account.repo.AccountRelationRepo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;
import java.util.Optional;

/**
 * @author Gongminrui
 * @date 2023-03-04 18:49
 */
@Service
@Slf4j
public class AccountRelationService {
    @Resource
    private AccountRelationRepo accountRelationRepo;

    /**
     * 获得所有下级
     *
     * @param pid
     * @return
     */
    public List<AccountRebateVo> listNext(Long pid, String email) {
        List<AccountRebateVo> list = accountRelationRepo.listByPid(pid, "");
        return list;
    }

    public int getNextCount(Long pid) {
        return accountRelationRepo.countByPId(pid);
    }

    /**
     * 创建关系
     *
     * @param accountId
     * @param pid
     * @return
     */
    public int createRelation(Long accountId, Long pid) {
        Optional<AccountRelation> optional = accountRelationRepo.findByAccountId(accountId);
        if (optional.isPresent()) {
            return -1;
        }

        AccountRelation accountRelation = new AccountRelation();
        accountRelation.setAccountId(accountId);
        accountRelation.setPId(pid);

        String relationTree = "/";
        if (pid != null) {
            Optional<AccountRelation> optionalPid = accountRelationRepo.findByAccountId(pid);
            if (optionalPid.isPresent()) {
                relationTree = optionalPid.get().getRelationTree();
                relationTree = relationTree.endsWith("/") ? relationTree + pid : relationTree + "/" + pid;
            }
        }
        accountRelation.setRelationTree(relationTree);

        accountRelationRepo.save(accountRelation);
        return 1;
    }

    /**
     * 获得关系
     *
     * @param accountId
     * @return
     */
    public Optional<AccountRelation> findByAccountId(Long accountId) {
        return accountRelationRepo.findByAccountId(accountId);
    }

    /**
     * 获得下级会员数量
     *
     * @param accountId
     * @return
     */
    public int getNextVipCount(Long accountId) {
        List<AccountRebateVo> list = accountRelationRepo.listByPid(accountId, "");
        return (int) list.stream().filter(v -> Optional.ofNullable(v.getVipCount()).orElse(0) > 0).count();
    }
}
