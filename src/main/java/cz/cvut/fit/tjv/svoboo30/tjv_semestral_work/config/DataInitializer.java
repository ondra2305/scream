package cz.cvut.fit.tjv.svoboo30.tjv_semestral_work.config;

import cz.cvut.fit.tjv.svoboo30.tjv_semestral_work.dtos.RegisterPlayerDTO;
import cz.cvut.fit.tjv.svoboo30.tjv_semestral_work.service.PlayerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile("!test")
public class DataInitializer implements CommandLineRunner {

    @Autowired
    private PlayerService playerService;

    @Override
    public void run(String... args) throws Exception {
        // Check if the admin user already exists
        var adminUser = playerService.findByUsername("admin");
        if (adminUser.isEmpty()) {
            // Create and save the new admin user
            playerService.registerNewPlayer(new RegisterPlayerDTO("admin", "admin"));
            playerService.setAdmin("admin", true);
        } else {
            playerService.setAdmin("admin", true);
        }
    }
}
