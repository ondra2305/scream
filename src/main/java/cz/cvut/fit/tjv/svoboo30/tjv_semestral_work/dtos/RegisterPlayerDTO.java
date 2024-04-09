package cz.cvut.fit.tjv.svoboo30.tjv_semestral_work.dtos;

public class RegisterPlayerDTO {
    private String username;
    private String password;

    public RegisterPlayerDTO(String username, String password) {
        this.username = username;
        this.password = password;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
