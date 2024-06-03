package kol.message.repo;

import kol.message.model.Message;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * @author Gongminrui
 * @date 2023-03-03 10:01
 */
@Repository
public interface MessageRepo extends CrudRepository<Message, Long>, JpaSpecificationExecutor<Message> {

    int countByAccountIdAndUnread(Long accountId, boolean unread);

    List<Message> findByAccountIdAndUnread(Long accountId, boolean unread);

    @Query(value = "select * from message where account_id=:accountId order by unread desc,created_at desc limit :limit", nativeQuery = true)
    List<Message> findByAccountIdLimit(Long accountId, int limit);
}
