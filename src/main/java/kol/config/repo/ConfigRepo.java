package kol.config.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import kol.config.model.Config;

import java.util.List;

@Repository
public interface ConfigRepo extends JpaRepository<Config, Config.KeyEnum> {

    @Query("select  c from config c where k=:k")
    String get(Config.KeyEnum k);

    List<Config> findByKIn(List<Config.KeyEnum> k);
}
