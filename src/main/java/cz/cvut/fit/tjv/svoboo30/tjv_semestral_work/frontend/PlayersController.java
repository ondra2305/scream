package cz.cvut.fit.tjv.svoboo30.tjv_semestral_work.frontend;

import cz.cvut.fit.tjv.svoboo30.tjv_semestral_work.auth.JwtTokenUtil;
import cz.cvut.fit.tjv.svoboo30.tjv_semestral_work.domain.Player;
import cz.cvut.fit.tjv.svoboo30.tjv_semestral_work.service.PlayerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/players")
public class PlayersController {
    private final PlayerService playerService;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private JwtTokenUtil tokenProvider;

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

    public PlayersController(PlayerService playerService) {
        this.playerService = playerService;
    }

    @GetMapping
    public String viewPlayers(@CookieValue(name = "JWT-TOKEN", required = false) String jwtToken,
                              Model model, RedirectAttributes redirectAttributes) {
        var players = playerService.readAll();
        model.addAttribute("players", players);

        if (checkToken(jwtToken, model, redirectAttributes)) {
            model.addAttribute("player", activePlayer);
        }

        return "players";
    }

    @GetMapping("/delete/{username}")
    public String deletePlayer(@CookieValue(name = "JWT-TOKEN", required = false) String jwtToken,
                               Model model, RedirectAttributes redirectAttributes,
                               @PathVariable String username) {
        if (!checkToken(jwtToken, model, redirectAttributes)) {
            redirectAttributes.addFlashAttribute("loginError", "You must be logged to view this page");
            return "redirect:/login";
        }

        if (!activePlayer.getAdmin()) {
            redirectAttributes.addFlashAttribute("loginError", "You must be admin to view this page");
            return "redirect:/login";
        }

        playerService.deleteByUsername(username);
        return "redirect:/players";
    }

    @GetMapping("/{username}")
    public String playerReviews(@PathVariable String username, Model model,
                                @CookieValue(name = "JWT-TOKEN", required = false) String jwtToken,
                                RedirectAttributes redirectAttributes) {
        var player = playerService.findByUsername(username);
        if (player.isEmpty()) {
            return "redirect:/";
        }

        var reviews = player.get().getWrittenReviews();

        if (checkToken(jwtToken, model, redirectAttributes)) {
            model.addAttribute("player", activePlayer);
        }

        model.addAttribute("reviewAuthor", player.get());
        model.addAttribute("reviews", reviews);

        return "players";
    }
}
