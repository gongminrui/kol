package kol.message.repo;

import kol.message.model.Message;
import kol.message.model.MessageTemplate;
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
public interface MessageTemplateRepo extends CrudRepository<MessageTemplate, Long>, JpaSpecificationExecutor<MessageTemplate> {
    MessageTemplate findByMsgType(int msgType);
}
