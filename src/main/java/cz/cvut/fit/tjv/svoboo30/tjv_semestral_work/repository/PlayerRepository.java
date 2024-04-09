package cz.cvut.fit.tjv.svoboo30.tjv_semestral_work.repository;

import cz.cvut.fit.tjv.svoboo30.tjv_semestral_work.domain.Player;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PlayerRepository extends CrudRepository<Player, Long> {
    @Query("SELECT p FROM Player p WHERE p.username = :username")
    Player findByUsername(String username);
}
