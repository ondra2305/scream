package cz.cvut.fit.tjv.svoboo30.tjv_semestral_work.frontend;

import cz.cvut.fit.tjv.svoboo30.tjv_semestral_work.auth.JwtTokenUtil;
import cz.cvut.fit.tjv.svoboo30.tjv_semestral_work.service.GameService;
import cz.cvut.fit.tjv.svoboo30.tjv_semestral_work.service.PlayerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/")
public class HomeController {
    private final GameService gameService;

    private final PlayerService playerService;

    @Autowired
    private JwtTokenUtil tokenProvider;

    private String activeUsername;

    public HomeController(GameService gameService, PlayerService playerService) {
        this.gameService = gameService;
        this.playerService = playerService;
    }

    @GetMapping
    public String home(@CookieValue(name = "JWT-TOKEN", required = false) String jwtToken,
                       Model model, RedirectAttributes redirectAttributes) {
        var games = gameService.readAll();
        if (jwtToken != null && !jwtToken.isEmpty()) {
            // Decode the JWT token to get user information
            try {
                activeUsername = tokenProvider.getUsernameFromToken(jwtToken);
                model.addAttribute("user", activeUsername);
            } catch (Exception e) {
                // Invalid JWT token
                redirectAttributes.addFlashAttribute("loginError", "Session expired. Please log in again.");
                return "redirect:/login";
            }

            var ownedGames = playerService.findByUsername(activeUsername).get().getOwnedGames();
            var player = playerService.findByUsername(activeUsername).get().getUsername();
            model.addAttribute("games", games);
            model.addAttribute("ownedGames", ownedGames);
            model.addAttribute("player", player);
        } else {
            model.addAttribute("games", games);
        }
        return "home";
    }
}