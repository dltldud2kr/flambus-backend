package flambus.app.dto.member;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;


@Builder
@Data
@AllArgsConstructor
public class PwChangeDto {

    private String email;
    private String password;

}
