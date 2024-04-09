package cz.cvut.fit.tjv.svoboo30.tjv_semestral_work.service;

import cz.cvut.fit.tjv.svoboo30.tjv_semestral_work.domain.Review;
import cz.cvut.fit.tjv.svoboo30.tjv_semestral_work.dtos.ReviewDTO;
import cz.cvut.fit.tjv.svoboo30.tjv_semestral_work.exception.EntityNotFoundException;
import cz.cvut.fit.tjv.svoboo30.tjv_semestral_work.exception.MultipleReviewsException;
import cz.cvut.fit.tjv.svoboo30.tjv_semestral_work.repository.GameRepository;
import cz.cvut.fit.tjv.svoboo30.tjv_semestral_work.repository.PlayerRepository;
import cz.cvut.fit.tjv.svoboo30.tjv_semestral_work.repository.ReviewRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ReviewServiceImpl extends CrudServiceImpl<Review, Long> implements ReviewService {
    private final ReviewRepository reviewRepository;

    @Autowired
    private PlayerRepository playerRepository;

    @Autowired
    private GameRepository gameRepository;

    public ReviewServiceImpl(ReviewRepository reviewRepository) {
        this.reviewRepository = reviewRepository;
    }

    protected CrudRepository<Review, Long> getRepository() {
        return reviewRepository;
    }

    @Override
    @Transactional
    public void deleteById(Long id) {
        var review = reviewRepository.findById(id).orElseThrow(IllegalArgumentException::new);
        var player = review.getWrittenBy();
        var game = review.getBelongsTo();

        player.getWrittenReviews().remove(review);
        playerRepository.save(player);

        game.getGameReviews().remove(review);
        gameRepository.save(game);

        reviewRepository.deleteById(id);
    }

    @Transactional
    public Review writeReview(ReviewDTO reviewDTO) {
        Review review = new Review();
        review.setPoints(reviewDTO.getPoints());
        review.setReviewText(reviewDTO.getReviewText());
        review.setDateAdded(reviewDTO.getDateAdded());

        var player = playerRepository.findByUsername(reviewDTO.getWrittenBy());
        if (player == null) {
            throw new EntityNotFoundException();
        }
        review.setWrittenBy(player);

        var game = gameRepository.findById(reviewDTO.getBelongsTo())
                .orElseThrow(EntityNotFoundException::new);
        review.setBelongsTo(game);

        if (gameRepository.findById(reviewDTO.getBelongsTo()).get().getGameReviews().stream()
                .anyMatch(review1 -> review1.getWrittenBy().getUsername().equals(reviewDTO.getWrittenBy()))) {
            throw new MultipleReviewsException("Player already wrote a review for this game");
        }

        player.getWrittenReviews().add(review);
        game.getGameReviews().add(review);

        reviewRepository.save(review);
        playerRepository.save(player);
        gameRepository.save(game);

        return review;
    }

    @Transactional
    public void updateById(Long id, ReviewDTO reviewDTO) {
        var review = reviewRepository.findById(id).orElseThrow(IllegalArgumentException::new);

        var game = gameRepository.findById(reviewDTO.getBelongsTo())
                .orElseThrow(EntityNotFoundException::new);
        review.setBelongsTo(game);


        review.setPoints(reviewDTO.getPoints());
        review.setReviewText(reviewDTO.getReviewText());
        review.setDateAdded(reviewDTO.getDateAdded());
        review.setBelongsTo(game);

        reviewRepository.save(review);
    }
}
