package cz.cvut.fit.tjv.svoboo30.tjv_semestral_work.frontend;

import cz.cvut.fit.tjv.svoboo30.tjv_semestral_work.auth.JwtTokenUtil;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/login")
public class LoginController {

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private JwtTokenUtil tokenProvider;

    @GetMapping
    public String showLoginForm(Model model, RedirectAttributes redirectAttributes) {
        if (redirectAttributes.getFlashAttributes().containsKey("loginError")) {
            model.addAttribute("loginError", redirectAttributes.getFlashAttributes().get("loginError"));
        }
        return "login";
    }

    @PostMapping
    public String login(@RequestParam String username,
                        @RequestParam String password,
                        HttpServletResponse response,
                        RedirectAttributes redirectAttributes) {
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(username, password));

            String token = tokenProvider.generateToken(username);

            // Set the JWT as a cookie in the response
            Cookie authCookie = new Cookie("JWT-TOKEN", token);
            authCookie.setHttpOnly(true);
            authCookie.setPath("/");
            // authCookie.setSecure(true); // Uncomment on HTTPS
            response.addCookie(authCookie);

            return "redirect:/";
        } catch (AuthenticationException e) {
            // Handle authentication failure
            redirectAttributes.addFlashAttribute("loginError", "Invalid username or password.");
            return "redirect:/login";
        }
    }
}
