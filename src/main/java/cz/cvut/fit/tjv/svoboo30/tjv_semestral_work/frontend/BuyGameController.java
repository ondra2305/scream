package cz.cvut.fit.tjv.svoboo30.tjv_semestral_work.frontend;

import cz.cvut.fit.tjv.svoboo30.tjv_semestral_work.auth.JwtTokenUtil;
import cz.cvut.fit.tjv.svoboo30.tjv_semestral_work.service.GameService;
import cz.cvut.fit.tjv.svoboo30.tjv_semestral_work.service.PlayerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/buy-game")
public class BuyGameController {

    private final PlayerService playerService;

    private final GameService gameService;

    @Autowired
    private JwtTokenUtil tokenProvider;

    public BuyGameController(PlayerService playerService, GameService gameService) {
        this.playerService = playerService;
        this.gameService = gameService;
    }

    @GetMapping("/{id}")
    public String buyGame(@CookieValue(name = "JWT-TOKEN", required = false) String jwtToken,
                          Model model, RedirectAttributes redirectAttributes,
                          @PathVariable Long id) {
        if (jwtToken != null && !jwtToken.isEmpty()) {
            // Decode the JWT token to get user information
            String activeUsername;
            try {
                activeUsername = tokenProvider.getUsernameFromToken(jwtToken);
                model.addAttribute("user", activeUsername);
            } catch (Exception e) {
                // Invalid JWT token
                redirectAttributes.addFlashAttribute("loginError", "Session expired. Please log in again.");
                return "redirect:/login";
            }

            try {
                var game = gameService.readById(id).get();

                playerService.buyGame(activeUsername, game.getId());
            } catch (Exception e) {
                redirectAttributes.addFlashAttribute("error", e.getMessage());
                return "redirect:/";
            }

        } else {
            redirectAttributes.addFlashAttribute("loginError", "Please log in for this action.");
            return "redirect:/login";
        }
        return "redirect:/";
    }
}
