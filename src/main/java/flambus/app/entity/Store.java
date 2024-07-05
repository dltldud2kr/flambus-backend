package flambus.app.entity;//package flambus.flambus_v10.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Type;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Table(name = "store")
public class Store {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idx;//가게 Pk
    private String name; //가게명
    private String address;//도로명 가게 주소
    private Double latitude; //위도
    private Double longitude; //경도
    private String contactNumber; //연락처
    private Long memberIdx;

    @Lob
    @Type(type = "text")
    private String introduce; // 가게 소개

    private LocalDateTime created;
    private LocalDateTime updated;

}
