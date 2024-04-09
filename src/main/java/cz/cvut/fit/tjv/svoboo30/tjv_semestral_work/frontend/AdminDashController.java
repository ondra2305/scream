package cz.cvut.fit.tjv.svoboo30.tjv_semestral_work.frontend;

import cz.cvut.fit.tjv.svoboo30.tjv_semestral_work.auth.JwtTokenUtil;
import cz.cvut.fit.tjv.svoboo30.tjv_semestral_work.domain.Game;
import cz.cvut.fit.tjv.svoboo30.tjv_semestral_work.domain.Player;
import cz.cvut.fit.tjv.svoboo30.tjv_semestral_work.service.GameService;
import cz.cvut.fit.tjv.svoboo30.tjv_semestral_work.service.PlayerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;

@Controller
@RequestMapping("/admin")
public class AdminDashController {
    private final GameService gameService;

    private final PlayerService playerService;

    @Autowired
    private JwtTokenUtil tokenProvider;

    public AdminDashController(PlayerService playerService, GameService gameService) {
        this.playerService = playerService;
        this.gameService = gameService;
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

            activePlayer = playerService.findByUsername(activeUsername).get();

            if (!activePlayer.getAdmin()) {
                redirectAttributes.addFlashAttribute("loginError", "You must be admin to view this page");
                return false;
            }

        } catch (Exception e) {
            // Invalid JWT token
            redirectAttributes.addFlashAttribute("loginError", "You must be logged to view this page");
            return false;
        }

        return true;
    }

    @GetMapping
    public String viewAdminDashboard(@CookieValue(name = "JWT-TOKEN", required = false) String jwtToken,
                                     Model model, RedirectAttributes redirectAttributes) {
        if (!checkToken(jwtToken, model, redirectAttributes)) {
            return "redirect:/login";
        }

        var games = gameService.readAll();
        model.addAttribute("games", games);
        model.addAttribute("player", activePlayer);
        return "admin";
    }

    @PostMapping("/add-game")
    public String addGame(@CookieValue(name = "JWT-TOKEN", required = false) String jwtToken,
                          Model model, RedirectAttributes redirectAttributes,
                          @RequestParam String name,
                          @RequestParam(required = false) String price, @RequestParam String dateReleased) {
        if (!checkToken(jwtToken, model, redirectAttributes)) {
            return "redirect:/login";
        }

        if (price.isEmpty()) {
            price = "0";
        }

        Game newGame = new Game(name, LocalDate.parse(dateReleased), Integer.parseInt(price));

        try {
            gameService.create(newGame);
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Backend error! " + e.getMessage());
            return "redirect:/admin";
        }

        return "redirect:/admin";
    }

    @GetMapping("/edit-game/{id}")
    public String viewEditGame(@CookieValue(name = "JWT-TOKEN", required = false) String jwtToken,
                               Model model, RedirectAttributes redirectAttributes, @PathVariable String id) {
        if (!checkToken(jwtToken, model, redirectAttributes)) {
            return "redirect:/login";
        }

        var game = gameService.readById(Long.parseLong(id));
        if (game.isEmpty()) {
            return "redirect:/admin";
        }

        model.addAttribute("game", game.get());
        model.addAttribute("player", activePlayer);
        return "adm-edit-game";
    }

    @PostMapping("/edit-game/{id}")
    public String editGame(@CookieValue(name = "JWT-TOKEN", required = false) String jwtToken,
                           Model model, RedirectAttributes redirectAttributes,
                           @RequestParam String name,
                           @RequestParam String price, @RequestParam String dateReleased, @PathVariable String id) {
        if (!checkToken(jwtToken, model, redirectAttributes)) {
            return "redirect:/login";
        }

        var game = gameService.readById(Long.parseLong(id));
        if (game.isEmpty()) {
            return "redirect:/admin";
        }

        try {
            game.get().setName(name);
            game.get().setPrice(Integer.parseInt(price));
            game.get().setDateReleased(LocalDate.parse(dateReleased));
            gameService.update(game.get().getId(), game.get());
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Backend error! " + e.getMessage());
            return "redirect:/admin";
        }

        return "redirect:/admin";
    }

    @GetMapping("/delete-game/{id}")
    public String deleteGame(@CookieValue(name = "JWT-TOKEN", required = false) String jwtToken,
                             Model model, RedirectAttributes redirectAttributes,
                             @PathVariable String id) {
        if (!checkToken(jwtToken, model, redirectAttributes)) {
            return "redirect:/login";
        }

        var game = gameService.readById(Long.parseLong(id));
        if (game.isEmpty()) {
            return "redirect:/admin";
        }

        try {
            gameService.deleteById(game.get().getId());
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Backend error! " + e.getMessage());
            return "redirect:/admin";
        }

        return "redirect:/admin";
    }
}
