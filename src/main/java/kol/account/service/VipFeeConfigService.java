package kol.account.service;

import kol.account.model.VipFeeConfig;
import kol.account.repo.VipFeeConfigRepo;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

/**
 * @author Gongminrui
 * @date 2023-05-26 21:24
 */
@Service
public class VipFeeConfigService {
    @Resource
    private VipFeeConfigRepo vipFeeConfigRepo;

    public List<VipFeeConfig> listAll() {
        return vipFeeConfigRepo.findAll(Sort.by("nextCount").descending());
    }
}
