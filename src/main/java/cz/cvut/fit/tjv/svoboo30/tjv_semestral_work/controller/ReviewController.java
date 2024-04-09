package cz.cvut.fit.tjv.svoboo30.tjv_semestral_work.controller;

import cz.cvut.fit.tjv.svoboo30.tjv_semestral_work.auth.JwtTokenFilter;
import cz.cvut.fit.tjv.svoboo30.tjv_semestral_work.auth.JwtTokenUtil;
import cz.cvut.fit.tjv.svoboo30.tjv_semestral_work.auth.SecurityConfig;
import cz.cvut.fit.tjv.svoboo30.tjv_semestral_work.domain.Review;
import cz.cvut.fit.tjv.svoboo30.tjv_semestral_work.dtos.ReviewDTO;
import cz.cvut.fit.tjv.svoboo30.tjv_semestral_work.exception.EntityNotFoundException;
import cz.cvut.fit.tjv.svoboo30.tjv_semestral_work.exception.GameOwnershipException;
import cz.cvut.fit.tjv.svoboo30.tjv_semestral_work.exception.MultipleReviewsException;
import cz.cvut.fit.tjv.svoboo30.tjv_semestral_work.service.PlayerService;
import cz.cvut.fit.tjv.svoboo30.tjv_semestral_work.service.ReviewService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.Objects;

@RestController
@RequestMapping("/api/review")
public class ReviewController {
    private final ReviewService reviewService;

    private PlayerService playerService;

    @Autowired
    private JwtTokenUtil jwtTokenUtil;

    @Autowired
    private JwtTokenFilter jwtTokenFilter;

    @Autowired
    SecurityConfig securityConfig;

    public ReviewController(ReviewService reviewService, PlayerService playerService) {
        this.reviewService = reviewService;
        this.playerService = playerService;
    }

    private void validateUsername(HttpServletRequest request, String username) {
        String token = jwtTokenFilter.resolveToken(request);
        var tokenUsername = jwtTokenUtil.getUsernameFromToken(token);

        if ((token == null || !tokenUsername.equals(username)) && !playerService.isAdmin(tokenUsername)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You can only update your own information");
        }
    }

    @Operation(summary = "Find all reviews")
    @ApiResponse(responseCode = "200", description = "Found all reviews")
    @GetMapping
    public Iterable<Review> readAll() {
        return reviewService.readAll();
    }

    @Operation(summary = "Find a review by id")
    @ApiResponse(responseCode = "200", description = "Found the review")
    @ApiResponse(responseCode = "404", description = "Review not found")
    @GetMapping("/{id}")
    public Review readById(@PathVariable Long id) {
        var review = reviewService.readById(id);
        if (review.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        } else {
            return review.get();
        }
    }

    @Operation(summary = "Add a a new review")
    @ApiResponse(responseCode = "201", description = "Added a new review")
    @ApiResponse(responseCode = "401", description = "Unauthorized")
    @ApiResponse(responseCode = "404", description = "Player or game not found")
    @ApiResponse(responseCode = "400", description = "Player does not own this game / Cannot write multiple reviews for one game")
    @SecurityRequirement(name = "bearerAuth")
    @PostMapping
    @PreAuthorize("isAuthenticated()")
    @ResponseStatus(HttpStatus.CREATED)
    public Review create(@RequestBody ReviewDTO data) {
        try {
            return reviewService.writeReview(data);
        } catch (EntityNotFoundException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        } catch (GameOwnershipException | MultipleReviewsException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
        }
    }

    @Operation(summary = "Update a review by id")
    @ApiResponse(responseCode = "204", description = "Updated the review")
    @ApiResponse(responseCode = "404", description = "Review not found")
    @ApiResponse(responseCode = "401", description = "Unauthorized")
    @ApiResponse(responseCode = "403", description = "You can only update your reviews")
    @SecurityRequirement(name = "bearerAuth")
    @PutMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void update(@PathVariable Long id, @RequestBody ReviewDTO data, HttpServletRequest request) {
        var username = data.getWrittenBy();
        validateUsername(request, username);

        if(reviewService.readById(id).isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }

        if(!Objects.equals(username, reviewService.readById(id).get().getWrittenBy().getUsername())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        }

        try {
            reviewService.updateById(id, data);
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }
    }

    @Operation(summary = "Delete a review by id")
    @ApiResponse(responseCode = "204", description = "Deleted the review")
    @ApiResponse(responseCode = "404", description = "Review not found")
    @ApiResponse(responseCode = "401", description = "Unauthorized")
    @ApiResponse(responseCode = "403", description = "You can only delete your reviews")
    @SecurityRequirement(name = "bearerAuth")
    @DeleteMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteById(@PathVariable Long id, HttpServletRequest request) {
        var username = reviewService.readById(id).get().getWrittenBy().getUsername();
        validateUsername(request, username);

        try {
            reviewService.deleteById(id);
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }
    }
}
