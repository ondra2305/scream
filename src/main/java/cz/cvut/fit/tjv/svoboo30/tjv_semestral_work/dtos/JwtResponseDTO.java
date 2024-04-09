package cz.cvut.fit.tjv.svoboo30.tjv_semestral_work.dtos;

public class JwtResponseDTO {
    private final String token;

    public String getToken() {
        return token;
    }

    public JwtResponseDTO(String token) {
        this.token = token;
    }
}
