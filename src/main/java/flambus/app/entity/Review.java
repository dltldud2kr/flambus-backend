package flambus.app.entity;//package flambus.flambus_v10.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Table(name = "review")
public class Review {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idx;//리뷰 idx
    private Long storeIdx;//가게 idx
    private Long memberIdx;//리뷰 작성자 idx
    private String content; //리뷰내용
    private Long representIdx; //대표 사진 인덱스 번호
    private LocalDateTime created; //작성 시간
    private LocalDateTime modified; //수정 시간



}
