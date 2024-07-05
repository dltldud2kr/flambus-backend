package flambus.app.entity;//package flambus.flambus_v10.model;

import flambus.app._enum.AttachmentType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.GeneratorType;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Table(name = "uploadImage")
public class UploadImage {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idx;
    private long uploaderIdx;//파일 이름
    private String fileName;//파일 이름
    private String uniqueFileName;//적재된 파일명
    private String imageUrl; //파일 이미지 URL
    private long fileSize; //파일 용량
    private String attachmentType; //리뷰,피드
    private long mappedId;//연결된 리뷰,피드 게시글
    private LocalDateTime created;
    private LocalDateTime updated;

}
