package flambus.app.dto.store;

import lombok.*;

import java.time.LocalDate;

@Data
@Builder
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class CreateStoreDto {

    private long memberIdx;
    private String storeName;
    private String address;
    private String contactNumber;
    private String introduce;
    private LocalDate created;
    private LocalDate updated;

}
