package cz.cvut.fit.tjv.svoboo30.tjv_semestral_work.frontend;

import cz.cvut.fit.tjv.svoboo30.tjv_semestral_work.auth.JwtTokenUtil;
import cz.cvut.fit.tjv.svoboo30.tjv_semestral_work.domain.Player;
import cz.cvut.fit.tjv.svoboo30.tjv_semestral_work.dtos.GameLeaderboardDTO;
import cz.cvut.fit.tjv.svoboo30.tjv_semestral_work.service.GameService;
import cz.cvut.fit.tjv.svoboo30.tjv_semestral_work.service.PlayerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.util.List;

@Controller
@RequestMapping("/games")
public class GamesController {
    private final GameService gameService;

    private final PlayerService playerService;

    @Autowired
    private JwtTokenUtil tokenProvider;

    public GamesController(GameService gameService, PlayerService playerService) {
        this.gameService = gameService;
        this.playerService = playerService;
    }

    private Player activePlayer;

    private boolean checkToken(String jwtToken, Model model, RedirectAttributes redirectAttributes) {
        if (jwtToken == null) {
            redirectAttributes.addFlashAttribute("loginError", "You must be logged to view this page");
            return false;
        }

        try {
            // Decode the JWT token to get user information
            var activeUsername = tokenProvider.getUsernameFromToken(jwtToken);
            model.addAttribute("user", activeUsername);

            if (playerService.findByUsername(activeUsername).isEmpty()) {
                redirectAttributes.addFlashAttribute("loginError", "You must be logged to view this page");
                return false;
            }

            activePlayer = playerService.findByUsername(activeUsername).get();

        } catch (Exception e) {
            // Invalid JWT token
            redirectAttributes.addFlashAttribute("loginError", "You must be logged to view this page");
            return false;
        }

        return true;
    }

    @GetMapping(value = {"", "/leaderboard"})
    public String viewLeaderboard(@CookieValue(name = "JWT-TOKEN", required = false) String jwtToken,
                                  Model model, RedirectAttributes redirectAttributes,
                                  @RequestParam(required = false) Integer year, @RequestParam(required = false) Integer month) {
        if (checkToken(jwtToken, model, redirectAttributes)) {
            model.addAttribute("player", activePlayer.getUsername());
        }

        var games = gameService.readAll();

        List<GameLeaderboardDTO> leaderboard;
        if (year != null && month != null) {
            leaderboard = gameService.calculateLeaderboard(month, year);
            model.addAttribute("selectedYear", year);
            model.addAttribute("selectedMonth", month);
        } else {
            leaderboard = gameService.calculateLeaderboard(LocalDate.now().getMonthValue(), LocalDate.now().getYear());
            model.addAttribute("selectedYear", LocalDate.now().getYear());
            model.addAttribute("selectedMonth", LocalDate.now().getMonthValue());
        }

        String[] months = new String[]{"January", "February", "March", "April", "May", "June", "July", "August", "September", "October", "November", "December"};
        model.addAttribute("months", months);
        model.addAttribute("leaderboard", leaderboard);
        model.addAttribute("games", games);
        return "games";
    }

    @PostMapping("/discount")
    public String discountTopGames(@CookieValue(name = "JWT-TOKEN", required = false) String jwtToken,
                                   Model model, RedirectAttributes redirectAttributes,
                                   @RequestParam Integer year, @RequestParam Integer month,
                                   @RequestParam Integer topN) {
        if (checkToken(jwtToken, model, redirectAttributes)) {
            model.addAttribute("player", activePlayer.getUsername());
        }

        if (!activePlayer.getAdmin()) {
            redirectAttributes.addFlashAttribute("error", "You must be logged in as admin to perform this action");
            return "redirect:/games/leaderboard?year=" + year + "&month=" + month;
        }

        try {
            gameService.applyDiscountToTopGames(year, month, topN);
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("error", "Top N cannot be greater than the number of games");
            return "redirect:/games/leaderboard?year=" + year + "&month=" + month;
        }
        return "redirect:/";
    }

    @GetMapping("/{id}")
    public String gameReviews(@CookieValue(name = "JWT-TOKEN", required = false) String jwtToken,
                              Model model, RedirectAttributes redirectAttributes,
                              @PathVariable Long id) {
        if (checkToken(jwtToken, model, redirectAttributes)) {
            model.addAttribute("player", activePlayer.getUsername());
        }

        var game = gameService.readById(id);
        if (game.isEmpty()) {
            return "redirect:/";
        }

        var reviews = game.get().getGameReviews();
        model.addAttribute("game", game.get());
        model.addAttribute("reviews", reviews);

        return "games";
    }
}
