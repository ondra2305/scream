package cz.cvut.fit.tjv.svoboo30.tjv_semestral_work.frontend;

import cz.cvut.fit.tjv.svoboo30.tjv_semestral_work.dtos.RegisterPlayerDTO;
import cz.cvut.fit.tjv.svoboo30.tjv_semestral_work.service.PlayerService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/register")
public class RegisterController {
    private final PlayerService playerService;

    public RegisterController(PlayerService playerService) {
        this.playerService = playerService;
    }

    @GetMapping
    public String viewRegisterForm(Model model) {
        return "register";
    }

    @PostMapping
    public String register(@RequestParam String username, @RequestParam String password, Model model) {
        RegisterPlayerDTO registerPlayerDTO = new RegisterPlayerDTO(username, password);

        try {
            playerService.registerNewPlayer(registerPlayerDTO);
        } catch (IllegalArgumentException e) {
            model.addAttribute("registerError", "Player with this username already exists");
            return "register";
        }

        return "redirect:/login";
    }
}
