package api.dao;

import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@EqualsAndHashCode(callSuper = false)
public class UserDao {
    private Long id;
    private String username;
    private String password;
    private String role;
    private String name;
}
