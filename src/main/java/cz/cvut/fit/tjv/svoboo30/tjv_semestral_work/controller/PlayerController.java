package cz.cvut.fit.tjv.svoboo30.tjv_semestral_work.controller;

import cz.cvut.fit.tjv.svoboo30.tjv_semestral_work.auth.JwtTokenFilter;
import cz.cvut.fit.tjv.svoboo30.tjv_semestral_work.auth.JwtTokenUtil;
import cz.cvut.fit.tjv.svoboo30.tjv_semestral_work.auth.SecurityConfig;
import cz.cvut.fit.tjv.svoboo30.tjv_semestral_work.domain.Player;
import cz.cvut.fit.tjv.svoboo30.tjv_semestral_work.dtos.PlayerDTO;
import cz.cvut.fit.tjv.svoboo30.tjv_semestral_work.dtos.RegisterPlayerDTO;
import cz.cvut.fit.tjv.svoboo30.tjv_semestral_work.exception.EntityNotFoundException;
import cz.cvut.fit.tjv.svoboo30.tjv_semestral_work.exception.GameOwnershipException;
import cz.cvut.fit.tjv.svoboo30.tjv_semestral_work.service.PlayerService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/player")
public class PlayerController {
    private final PlayerService playerService;

    @Autowired
    private JwtTokenUtil jwtTokenUtil;

    @Autowired
    private JwtTokenFilter jwtTokenFilter;

    @Autowired
    SecurityConfig securityConfig;

    public PlayerController(PlayerService playerService) {
        this.playerService = playerService;
    }

    private void validateUsername(HttpServletRequest request, String username) {
        String token = jwtTokenFilter.resolveToken(request);
        var tokenUsername = jwtTokenUtil.getUsernameFromToken(token);

        if ((token == null || !tokenUsername.equals(username)) && !playerService.isAdmin(tokenUsername)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You can only update your own information");
        }
    }

    @Operation(summary = "Find all players")
    @ApiResponse(responseCode = "200", description = "Found all players")
    @GetMapping
    public Iterable<Player> readAll() {
        return playerService.readAll();
    }

    @Operation(summary = "Find a player by username")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Found the player"),
            @ApiResponse(responseCode = "404", description = "Player not found")
    })
    @GetMapping("/{username}")
    public Player findByUsername(@PathVariable String username) {
        var result = playerService.findByUsername(username);

        if (result.isPresent()) {
            return result.get();
        } else {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }
    }

    @Operation(summary = "Register a new player")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Registered a new player"),
            @ApiResponse(responseCode = "409", description = "Player with this username already exists")
    })
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Player registerUser(@RequestBody RegisterPlayerDTO registerRequest) {
        try {
            return playerService.registerNewPlayer(registerRequest);
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.CONFLICT);
        }
    }

    @Operation(summary = "Update a player")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Updated the player"),
            @ApiResponse(responseCode = "401", description = "Not authorized to access the resource"),
            @ApiResponse(responseCode = "403", description = "You can only update your own information"),
            @ApiResponse(responseCode = "404", description = "Player not found")
    })
    @SecurityRequirement(name = "bearerAuth")
    @PutMapping("/{username}")
    @PreAuthorize("isAuthenticated()")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void update(@PathVariable String username, @RequestBody PlayerDTO data, HttpServletRequest request) {
        validateUsername(request, username);

        try {
            playerService.updateByUsername(username, data);
        } catch (EntityNotFoundException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }
    }

    @Operation(summary = "Delete a player")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Successfully deleted the player"),
            @ApiResponse(responseCode = "401", description = "Not authorized to access the resource"),
            @ApiResponse(responseCode = "403", description = "You can only update your own information"),
            @ApiResponse(responseCode = "404", description = "Player not found")
    })
    @SecurityRequirement(name = "bearerAuth")
    @DeleteMapping("/{username}")
    @PreAuthorize("isAuthenticated()")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteByUsername(@PathVariable String username, HttpServletRequest request) {
        validateUsername(request, username);

        try {
            playerService.deleteByUsername(username);
        } catch (EntityNotFoundException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }
    }

    @Operation(summary = "Buy a game")
    @ApiResponse(responseCode = "201", description = "Successfully bought the game")
    @ApiResponse(responseCode = "401", description = "Not authorized to access the resource")
    @ApiResponse(responseCode = "409", description = "Player already owns the game")
    @ApiResponse(responseCode = "404", description = "Player or game not found")
    @SecurityRequirement(name = "bearerAuth")
    @PostMapping("/{username}/purchaseGame/{gameId}")
    @PreAuthorize("isAuthenticated()")
    @ResponseStatus(HttpStatus.CREATED)
    public String buyGame(@PathVariable String username, @PathVariable Long gameId, HttpServletRequest request) {
        validateUsername(request, username);

        try {
            playerService.buyGame(username, gameId);
            return "Successfully purchased the game.";
        } catch (EntityNotFoundException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        } catch (GameOwnershipException e) {
            throw new ResponseStatusException(HttpStatus.CONFLICT);
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
        }
    }
}
