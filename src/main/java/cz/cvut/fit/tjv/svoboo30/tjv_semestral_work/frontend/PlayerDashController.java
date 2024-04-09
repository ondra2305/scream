package cz.cvut.fit.tjv.svoboo30.tjv_semestral_work.frontend;

import cz.cvut.fit.tjv.svoboo30.tjv_semestral_work.auth.JwtTokenUtil;
import cz.cvut.fit.tjv.svoboo30.tjv_semestral_work.domain.Player;
import cz.cvut.fit.tjv.svoboo30.tjv_semestral_work.service.GameService;
import cz.cvut.fit.tjv.svoboo30.tjv_semestral_work.service.PlayerService;
import cz.cvut.fit.tjv.svoboo30.tjv_semestral_work.service.ReviewService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/player")
public class PlayerDashController {
    private final PlayerService playerService;

    private final GameService gameService;

    private final ReviewService reviewService;

    @Autowired
    private JwtTokenUtil tokenProvider;

    public PlayerDashController(PlayerService playerService, GameService gameService, ReviewService reviewService) {
        this.playerService = playerService;
        this.gameService = gameService;
        this.reviewService = reviewService;
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

    @GetMapping
    public String viewPlayerDashboard(@CookieValue(name = "JWT-TOKEN", required = false) String jwtToken,
                                      Model model, RedirectAttributes redirectAttributes) {
        if (!checkToken(jwtToken, model, redirectAttributes)) {
            return "redirect:/login";
        }

        var ownedGames = activePlayer.getOwnedGames();
        var writtenReviews = activePlayer.getWrittenReviews();

        model.addAttribute("ownedGames", ownedGames);
        model.addAttribute("player", activePlayer);
        model.addAttribute("writtenReviews", writtenReviews);

        return "player";
    }

    @PostMapping("/edit-account")
    public String addGame(@CookieValue(name = "JWT-TOKEN", required = false) String jwtToken,
                          Model model, RedirectAttributes redirectAttributes,
                          @RequestParam String username,
                          @RequestParam String password,
                          @RequestParam String hoursPlayed) {
        if (!checkToken(jwtToken, model, redirectAttributes)) {
            return "redirect:/login";
        }

        try {
            activePlayer.setUsername(username);
            activePlayer.setHoursPlayed(Integer.parseInt(hoursPlayed));

            playerService.update(activePlayer.getId(), activePlayer);

            if (!password.equals("***")) {
                playerService.changePassword(activePlayer.getUsername(), password);
                return "redirect:/logout-player";
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Backend error! " + e.getMessage());
            return "redirect:/player";
        }

        return "redirect:/player";
    }

    @GetMapping("/edit-review/{id}")
    public String viewEditReview(@CookieValue(name = "JWT-TOKEN", required = false) String jwtToken,
                                 Model model, RedirectAttributes redirectAttributes, @PathVariable String id) {
        if (!checkToken(jwtToken, model, redirectAttributes)) {
            return "redirect:/login";
        }

        var review = reviewService.readById(Long.parseLong(id));
        if (review.isEmpty()) {
            return "redirect:/player";
        }

        var game = review.get().getBelongsTo();

        model.addAttribute("game", game);
        model.addAttribute("review", review.get());
        model.addAttribute("player", activePlayer);
        return "pl-edit-review";
    }

    @PostMapping("/edit-review/{id}")
    public String editReview(@CookieValue(name = "JWT-TOKEN", required = false) String jwtToken,
                             Model model, RedirectAttributes redirectAttributes,
                             @RequestParam String points,
                             @RequestParam String reviewText, @PathVariable String id) {
        if (!checkToken(jwtToken, model, redirectAttributes)) {
            return "redirect:/login";
        }

        var review = reviewService.readById(Long.parseLong(id));
        if (review.isEmpty()) {
            return "redirect:/player";
        }

        try {
            review.get().setPoints(Integer.parseInt(points));
            review.get().setReviewText(reviewText);

            reviewService.update(review.get().getId(), review.get());
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Backend error! " + e.getMessage());
            return "redirect:/player";
        }

        return "redirect:/player";
    }

    @GetMapping("/delete-review/{id}")
    public String deleteReview(@CookieValue(name = "JWT-TOKEN", required = false) String jwtToken,
                               Model model, RedirectAttributes redirectAttributes,
                               @PathVariable String id) {
        if (!checkToken(jwtToken, model, redirectAttributes)) {
            return "redirect:/login";
        }

        var review = reviewService.readById(Long.parseLong(id));
        if (review.isEmpty()) {
            return "redirect:/player";
        }

        try {
            reviewService.deleteById(review.get().getId());
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Backend error! " + e.getMessage());
            return "redirect:/player";
        }

        return "redirect:/player";
    }

}
