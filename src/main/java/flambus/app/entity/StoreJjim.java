package flambus.app.entity;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.time.LocalDateTime;

/**
 * store 찜하기.(나만의 탐험지로 지정)
 */
@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Table(name = "store_jjim")
public class StoreJjim {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idx;//pk
    private Long storeIdx; //스토어 IDX , storeidx랑 외래키 연결해야함.
    private Long memberIdx; //멤버
    private LocalDateTime created; //찜 한 날짜

}
