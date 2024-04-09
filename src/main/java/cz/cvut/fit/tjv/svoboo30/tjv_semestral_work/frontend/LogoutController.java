package cz.cvut.fit.tjv.svoboo30.tjv_semestral_work.frontend;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/logout-player")
public class LogoutController {
    @GetMapping
    public String logout(HttpServletResponse response) {
        // Create a cookie to overwrite the existing JWT token cookie
        Cookie cookie = new Cookie("JWT-TOKEN", "null");
        cookie.setHttpOnly(true);
        cookie.setMaxAge(0); // Set age to 0 to delete the cookie
        cookie.setPath("/");

        response.addCookie(cookie);

        return "redirect:/login";
    }
}
