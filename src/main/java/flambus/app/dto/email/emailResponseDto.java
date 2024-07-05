package flambus.app.dto.email;


import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class emailResponseDto {

    private String email;
    private String confirm;
}
