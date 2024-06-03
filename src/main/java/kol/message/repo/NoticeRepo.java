package kol.message.repo;

import kol.message.model.Notice;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

/**
 * @author Gongminrui
 * @date 2023-03-03 10:01
 */
@Repository
public interface NoticeRepo extends CrudRepository<Notice, Long>, JpaSpecificationExecutor<Notice> {
}
