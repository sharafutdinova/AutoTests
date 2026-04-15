package iteration2.changeusername;

import java.util.Objects;

public class CustomerProfile {
    private int id;
    private String username;
    private String password;
    private String name;
    private String role;

    public CustomerProfile(String role, String name, String password, String username, int id) {
        this.role = role;
        this.name = name;
        this.password = password;
        this.username = username;
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public String getName() {
        return name;
    }

    public String getRole() {
        return role;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        CustomerProfile that = (CustomerProfile) o;
        return id == that.id && Objects.equals(username, that.username) && Objects.equals(password, that.password) && Objects.equals(name, that.name) && Objects.equals(role, that.role);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, username, password, name, role);
    }
}
