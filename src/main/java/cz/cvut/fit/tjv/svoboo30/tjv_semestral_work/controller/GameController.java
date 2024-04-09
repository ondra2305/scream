package cz.cvut.fit.tjv.svoboo30.tjv_semestral_work.controller;

import cz.cvut.fit.tjv.svoboo30.tjv_semestral_work.auth.JwtTokenFilter;
import cz.cvut.fit.tjv.svoboo30.tjv_semestral_work.auth.JwtTokenUtil;
import cz.cvut.fit.tjv.svoboo30.tjv_semestral_work.auth.SecurityConfig;
import cz.cvut.fit.tjv.svoboo30.tjv_semestral_work.domain.Game;
import cz.cvut.fit.tjv.svoboo30.tjv_semestral_work.domain.PlayerDetails;
import cz.cvut.fit.tjv.svoboo30.tjv_semestral_work.dtos.GameLeaderboardDTO;
import cz.cvut.fit.tjv.svoboo30.tjv_semestral_work.service.GameService;
import cz.cvut.fit.tjv.svoboo30.tjv_semestral_work.service.PlayerService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("/api/game")
public class GameController {
    private final GameService gameService;

    private PlayerService playerService;

    @Autowired
    private JwtTokenUtil jwtTokenUtil;

    @Autowired
    private JwtTokenFilter jwtTokenFilter;

    @Autowired
    SecurityConfig securityConfig;

    public GameController(GameService gameService, PlayerService playerService) {
        this.gameService = gameService;
        this.playerService = playerService;
    }

    private void validateRequest(HttpServletRequest request) {
        String token = jwtTokenFilter.resolveToken(request);
        var tokenUsername = jwtTokenUtil.getUsernameFromToken(token);

        if ((token == null || !playerService.isAdmin(tokenUsername))) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        }
    }

    @Operation(summary = "Find all games")
    @ApiResponse(responseCode = "200", description = "Found all games")
    @GetMapping
    public Iterable<Game> readAll() {
        return gameService.readAll();
    }

    @Operation(summary = "Find a game by id")
    @ApiResponse(responseCode = "200", description = "Found the game")
    @ApiResponse(responseCode = "404", description = "Game not found")
    @GetMapping("/{id}")
    public Game readById(@PathVariable Long id) {
        var game = gameService.readById(id);
        if (game.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        } else {
            return game.get();
        }
    }

    @Operation(summary = "Add a a new game")
    @ApiResponse(responseCode = "201", description = "Added a new game")
    @ApiResponse(responseCode = "409", description = "This game already exists")
    @ApiResponse(responseCode = "401", description = "Unauthorized")
    @SecurityRequirement(name = "bearerAuth")
    @PostMapping
    @PreAuthorize("isAuthenticated()")
    @ResponseStatus(HttpStatus.CREATED)
    public Game create(@RequestBody Game data, HttpServletRequest request) {
        validateRequest(request);

        try {
            return gameService.create(data);
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.CONFLICT);
        }
    }

    @Operation(summary = "Update a game by id")
    @ApiResponse(responseCode = "204", description = "Updated the game")
    @ApiResponse(responseCode = "404", description = "Game not found")
    @ApiResponse(responseCode = "401", description = "Unauthorized")
    @SecurityRequirement(name = "bearerAuth")
    @PutMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void update(@PathVariable Long id, @RequestBody Game data, HttpServletRequest request) {
        validateRequest(request);

        try {
            gameService.update(id, data);
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }
    }

    @Operation(summary = "Delete a game by id")
    @ApiResponse(responseCode = "204", description = "Deleted the game")
    @ApiResponse(responseCode = "404", description = "Game not found")
    @ApiResponse(responseCode = "401", description = "Unauthorized")
    @SecurityRequirement(name = "bearerAuth")
    @DeleteMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteById(@PathVariable Long id, HttpServletRequest request) {
        validateRequest(request);

        try {
            gameService.deleteById(id);
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }
    }

    // Calculate leaderboard for a given month and year
    @Operation(summary = "Calculate leaderboard for a given month and year")
    @ApiResponse(responseCode = "200", description = "Calculated leaderboard")
    @ApiResponse(responseCode = "401", description = "Unauthorized")
    @GetMapping("/leaderboard/{year}/{month}")
    public Iterable<GameLeaderboardDTO> calculateLeaderboard(@PathVariable int month, @PathVariable int year) {
        return gameService.calculateLeaderboard(month, year);
    }

    // Apply discount to top N games in a given month and year
    @Operation(summary = "Apply discount to top N games in a given month and year")
    @ApiResponse(responseCode = "200", description = "Applied discount to top N games")
    @ApiResponse(responseCode = "401", description = "Unauthorized")
    @ApiResponse(responseCode = "403", description = "You must be admin to perform this action")
    @SecurityRequirement(name = "bearerAuth")
    @PostMapping("/discount/{year}/{month}/{topN}")
    @PreAuthorize("isAuthenticated()")
    public List<Game> applyDiscountToTopGames(@PathVariable int year, @PathVariable int month, @PathVariable int topN, HttpServletRequest request) {
        validateRequest(request);

        try {
            return gameService.applyDiscountToTopGames(year, month, topN);
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
        }
    }
}
