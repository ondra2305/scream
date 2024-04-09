package cz.cvut.fit.tjv.svoboo30.tjv_semestral_work.repository;

import cz.cvut.fit.tjv.svoboo30.tjv_semestral_work.domain.Review;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ReviewRepository extends CrudRepository<Review, Long> {

}