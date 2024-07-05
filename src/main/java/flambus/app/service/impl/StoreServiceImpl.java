package flambus.app.service.impl;

import flambus.app._enum.CustomExceptionCode;
import flambus.app.dto.review.ReviewResponse;
import flambus.app.dto.store.CreateStoreDto;
import flambus.app.dto.store.StoreDto;
import flambus.app.entity.*;
import flambus.app.exception.CustomException;
import flambus.app.mapper.StoreMapper;
import flambus.app.repository.MemberRepository;
import flambus.app.repository.StoreRepository;
import flambus.app.service.MemberService;
import flambus.app.service.ReviewService;
import flambus.app.service.StoreService;
import flambus.app.service.UploadService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class StoreServiceImpl implements StoreService {

    @Autowired
    private ReviewService reviewService;
    @Autowired
    private StoreRepository storeRepository;
    @Autowired
    private MemberService memberService;
    @Autowired
    private UploadService uploadService;
    private final StoreMapper storeMapper;
    @Autowired
    private MemberRepository memberRepository;


    /**
     * @return
     * @title 가게 정보를 요청합니다.
     */
    @Override
    public StoreDto getStoreInfo(long storeIdx) {
        try {
            StoreDto storeDto = new StoreDto();
            //요청한 가게 정보 find
            Store store = storeRepository.findById(storeIdx)
                    .orElseThrow(() -> new CustomException(CustomExceptionCode.NOT_FOUND));

            //기본 가게 정보 세팅
            storeDto.setStoreIdx(store.getIdx());
            storeDto.setStoreAddress(store.getAddress());
            storeDto.setStoreName(store.getName());
            storeDto.setContactNumber(store.getContactNumber());

            //해당 가게의 작성된 일지 개수 확인
            long totalReviewCount = reviewService.getTotalReviewCount(storeIdx);
            storeDto.setExpJournalsCount(totalReviewCount);

            //가게에서 작성된 일지중 제일 많은 태그를 찾아옴.
            Map<String, Object> representTag = new HashMap<>();

            //작성된 리뷰 개수가 0 이상일 경우에만 대표와 대표 리뷰 찾기.
            if(totalReviewCount > 0) {

                // 해당 리뷰에 가장 많이 달린 리뷰태그 맵핑      리뷰태그 무결성 문제 조심.
                ReviewTagType reviewType =  reviewService.getReivewTypeByIdx(storeMapper.findMostUsedTagIdx(storeIdx));
                representTag.put("tagIdx",reviewType.getIdx());
                representTag.put("tagName",reviewType.getTagName());
                storeDto.setRepresentTag(representTag);


                //가장 좋아요를 많이 받은 대표 리뷰정보 맵핑
                Map<String, Object> mostLikeReview = reviewService.getMostLikeReview(storeIdx);
                Map<String,Object> creator = new HashMap<>();
                creator.put("memberName",memberService.getMember((long) mostLikeReview.get("member_idx")).getEmail());
                creator.put("memberIdx",mostLikeReview.get("member_idx"));


                ReviewResponse.MostLikeReviewDto likeReviewDto = ReviewResponse.MostLikeReviewDto.builder()
                        .reviewIdx((Long) mostLikeReview.get("idx"))
                        .likeCount((Long) mostLikeReview.get("like_count"))
                        .reviewImage((List<Map<String, Object>>) mostLikeReview.get("uploadImage"))
                        .creator(creator)
                        .build();


                storeDto.setRepresentJournal(likeReviewDto);

            } else {
                //리뷰가 없는 경우 그냥 null 반환해줌.
                storeDto.setRepresentTag(null);
                storeDto.setRepresentJournal(null);
            }

            //대표 일지 이미지 정보 및 작성자 정보 찾기
            System.out.println("storeDto : " + storeDto);

            // 비어있는 경우 예외 처리 또는 기본값을 반환하는 로직 추가
            return storeDto;
        } catch (CustomException e) {
            throw new CustomException(CustomExceptionCode.SERVER_ERROR);
        }
    }

    @Override
    @Transactional
    public boolean createStore(CreateStoreDto dto) {

        try {
             memberRepository.findById(dto.getMemberIdx())
                    .orElseThrow(() -> new CustomException(CustomExceptionCode.NOT_FOUND_USER));

            int affectedRows = storeMapper.createStore(dto);
            if (affectedRows != 1) {

                throw new CustomException(CustomExceptionCode.STORE_CREATION_FAILED);
            }

            return true;
        } catch (Exception e) {

            log.error("Error creating store: {}", e.getMessage());
            return false;
        }

    }

}
