package cz.cvut.fit.tjv.svoboo30.tjv_semestral_work.frontend;

import cz.cvut.fit.tjv.svoboo30.tjv_semestral_work.auth.JwtTokenUtil;
import cz.cvut.fit.tjv.svoboo30.tjv_semestral_work.domain.Player;
import cz.cvut.fit.tjv.svoboo30.tjv_semestral_work.dtos.ReviewDTO;
import cz.cvut.fit.tjv.svoboo30.tjv_semestral_work.service.GameService;
import cz.cvut.fit.tjv.svoboo30.tjv_semestral_work.service.PlayerService;
import cz.cvut.fit.tjv.svoboo30.tjv_semestral_work.service.ReviewService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;

@Controller
@RequestMapping("/write-review")
public class WriteReviewController {

    private final ReviewService reviewService;

    private final GameService gameService;

    private final PlayerService playerService;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private JwtTokenUtil tokenProvider;

    private Player activePlayer;

    public WriteReviewController(ReviewService reviewService, GameService gameService, PlayerService playerService) {
        this.reviewService = reviewService;
        this.gameService = gameService;
        this.playerService = playerService;
    }

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

    @GetMapping("/{id}")
    public String showWriteReview(@CookieValue(name = "JWT-TOKEN", required = false) String jwtToken,
                                  @PathVariable String id, Model model,
                                  RedirectAttributes redirectAttributes) {

        var game = gameService.readById(Long.parseLong(id));
        if (game.isEmpty()) {
            return "redirect:/";
        }

        model.addAttribute("game", game.get());
        return "write-review";
    }

    @PostMapping("/{id}")
    public String writeReview(@CookieValue(name = "JWT-TOKEN", required = false) String jwtToken, Model model,
                              @PathVariable String id,
                              @RequestParam String reviewText, @RequestParam String points,
                              RedirectAttributes redirectAttributes) {
        if (!checkToken(jwtToken, model, redirectAttributes)) {
            return "redirect:/login";
        }

        if (gameService.readById(Long.parseLong(id)).isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "Game not found");
            return "redirect:/";
        }

        if (!playerService.findByUsername(activePlayer.getUsername()).get().getOwnedGames().contains(gameService.readById(Long.parseLong(id)).get())) {
            redirectAttributes.addFlashAttribute("error", "You must own the game to write a review");
            return "redirect:/";
        }

        try {
            ReviewDTO review = new ReviewDTO(Integer.parseInt(points), reviewText, activePlayer.getUsername(), Long.parseLong(id), LocalDate.now());
            reviewService.writeReview(review);
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/games/{id}";
        }

        return "redirect:/games/{id}";
    }

}
