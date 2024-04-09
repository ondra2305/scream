package cz.cvut.fit.tjv.svoboo30.tjv_semestral_work.service;

import cz.cvut.fit.tjv.svoboo30.tjv_semestral_work.domain.Review;
import cz.cvut.fit.tjv.svoboo30.tjv_semestral_work.dtos.ReviewDTO;

public interface ReviewService extends CrudService<Review, Long> {
    @Override
    void deleteById(Long id);

    Review writeReview(ReviewDTO reviewDTO);

    void updateById(Long id, ReviewDTO reviewDTO);
}