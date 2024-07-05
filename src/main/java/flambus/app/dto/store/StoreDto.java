package flambus.app.dto.store;


import flambus.app.dto.member.MemberDto;
import flambus.app.dto.review.ReviewResponse;
import lombok.*;

import java.util.Map;

/**
 * @title 메인화면에서 마커를 클릭했을때 나오는 가게 정보
 */

@Data
@Builder
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class StoreDto {
    private Long storeIdx; //가게 IDX
    private String storeName; //가게 이름
    private String storeAddress; //가게 도로명 주소
    private String contactNumber; //가게 전화번호
    private long expJournalsCount; //가게 탐험일지 개수
    private long ownExpSiteCount; //사용자들이 지정한 나만의 탐험지 개수(찜)
    private Map<String, Object> representTag; //대표 태그
    private ReviewResponse.MostLikeReviewDto representJournal; //대표 일지, 대표이미지,작성자 정보 등

}
