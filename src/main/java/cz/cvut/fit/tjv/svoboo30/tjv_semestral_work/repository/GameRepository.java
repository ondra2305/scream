package cz.cvut.fit.tjv.svoboo30.tjv_semestral_work.repository;

import cz.cvut.fit.tjv.svoboo30.tjv_semestral_work.domain.Game;
import cz.cvut.fit.tjv.svoboo30.tjv_semestral_work.domain.Review;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Collection;

@Repository
public interface GameRepository extends CrudRepository<Game, Long> {
    @Query("SELECT r FROM Review r WHERE FUNCTION('MONTH', r.dateAdded) = :month AND FUNCTION('YEAR', r.dateAdded) = :year AND r.belongsTo.id = :gameId")
    Collection<Review> findReviewsByGameAndMonthAndYear(@Param("gameId") Long gameId, @Param("month") int month, @Param("year") int year);

    //@Query(value = "SELECT * FROM review WHERE EXTRACT(MONTH FROM date_added) = :month AND EXTRACT(YEAR FROM date_added) = :year AND belongs_to_id = :gameId", nativeQuery = true)
    //Collection<Review> findReviewsByGameAndMonthAndYear(@Param("gameId") Long gameId, @Param("month") int month, @Param("year") int year);
}
