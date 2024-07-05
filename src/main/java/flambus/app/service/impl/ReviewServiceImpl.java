package flambus.app.service.impl;

import flambus.app._enum.AttachmentType;
import flambus.app._enum.CustomExceptionCode;
import flambus.app.dto.review.ReviewRequest;
import flambus.app.dto.review.ReviewResponse;
import flambus.app.entity.Review;
import flambus.app.entity.ReviewTag;
import flambus.app.entity.ReviewTagType;
import flambus.app.entity.UploadImage;
import flambus.app.exception.CustomException;
import flambus.app.mapper.ReviewMapper;
import flambus.app.repository.ReviewRepository;
import flambus.app.repository.ReviewTagRepository;
import flambus.app.repository.ReviewTagTypeRepository;
import flambus.app.service.MemberService;
import flambus.app.service.ReviewService;
import flambus.app.service.UploadService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReviewServiceImpl implements ReviewService {
    @Autowired
    private ReviewRepository reviewRepository;
    @Autowired
    private ReviewTagTypeRepository reviewTagTypeRepository;
    @Autowired
    private UploadService uploadService;
    @Autowired
    private MemberService memberService;
    @Autowired
    private ReviewMapper reviewMapper;
    @Autowired
    private ReviewTagRepository reviewTagRepository;

    /**
     * @title 리뷰 작성
     * @created 23.10.08
     * @updated 24.02.08  이시영
     * @Author 최성우
     */
    @Override
//    @Transactional
    public void createJournal(ReviewRequest.CreateReviewRequestDto request) throws IOException {

        System.out.println("memberIdx : "  + request.getMemberIdx());
        System.out.println("storeIdx : "  + request.getStoreIdx());
        //업로드한 리뷰 이미지가 존재한다면 리뷰 이미지 업로드를 진행합니다.
        try {
            Review review = new Review();

            review.setContent(request.getContent());
            review.setMemberIdx(request.getMemberIdx());
            review.setStoreIdx(request.getStoreIdx());
            review.setCreated(LocalDateTime.now());
            review.setModified(LocalDateTime.now());

            //리뷰를 먼저 생성하는 이유는 생성된 IDX로 업로드 이미지를 맵핑해주기 위해 1차적으로 먼저 생성
            Review savedReivew = reviewRepository.save(review);

            //리뷰 등록시 입력받은 태그가 String 형태라 파싱 후 리스트로 저장.
            List<Long> numbers = Arrays.stream(request.getTagIdx().split(","))
                    .map(Long::parseLong)
                    .collect(Collectors.toList());

            // 리뷰태그들을 ReviewTag 테이블에 저장
            for (Long tagIdx : numbers) {
                reviewTagTypeRepository.findById(tagIdx)
                        .orElseThrow(() -> new RuntimeException("Tag not found with ID: " + tagIdx));
                ReviewTag reviewTag = new ReviewTag();
                reviewTag.setStoreIdx(request.getStoreIdx());
                reviewTag.setReviewIdx(review.getIdx());
                reviewTag.setTagIdx(tagIdx);
                reviewTag.setModified(null);
                reviewTag.setCreated(LocalDateTime.now());
                reviewTagRepository.save(reviewTag);
            }

            //정상적으로 리뷰가 생성됐는 경우 리뷰에 등록할 이미지를 첨부했는지 확인하고 해당 리뷰 IDX에 이미지 업로드를 실행함
            uploadService.upload(request.getReviewImage(), request.getMemberIdx(), AttachmentType.REVIEW, savedReivew.getIdx());

            //업로드에 성공했다면 해당 리뷰에 업로드된 이미지 중 0번째 이미지를 대표 이미지로 지정함.
            review.setRepresentIdx(uploadService.getImageByAttachmentType(AttachmentType.REVIEW, savedReivew.getIdx()).get(0).getIdx());

            //다시한번 저장.
            reviewRepository.save(review);

            //도토리 1개 지급.
            memberService.addAcorns(memberService.getMember(request.getMemberIdx()),1);
        } catch (CustomException e) {
            System.out.println("create Journal Error : " + e);
            new CustomException(CustomExceptionCode.SERVER_ERROR);
        }
    }

    /**
     * @param request
     * @title 작성된 리뷰 수정
     * @description 리뷰 수정요청시 기존 업로드 되었던 S3 업로드 정보를 모두 삭제합니다.
     * 수정요청에 새로 담겨있는 첨부파일로 새로 업로드하고 DB에 새로 맵핑합니다.
     */
    @Override
//    @Transactional
    public void updateJournal(ReviewRequest.ModifyReviewRequestDto request) {
        try {
            //수정 요청한 리뷰를 확인함
            Review createdReview = reviewRepository.findById(request.getReviewIdx()).get();

            //수정을 요청한 사용자와 작성자가 다른 경우 : (본인인지의 대한 유효성 검사)
            if (createdReview.getMemberIdx() != request.getMemberIdx()) {
                throw new CustomException(CustomExceptionCode.ACCESS_DENIED);
            }

            //유효성 검증에 모두 통과했다면 버킷에 업로드되어있는 리뷰 파일을 모두 삭제합니다.
            //해당 리뷰에 업로드 등록되어있는 이미지를 검색합니다.
            List<UploadImage> imageByAttachmentType = uploadService.getImageByAttachmentType(AttachmentType.REVIEW, request.getReviewIdx());
            String[] removeTarget = new String[imageByAttachmentType.size() + 1];

            int removeCount = 0;
            //업로드된 이미지가 잇는 경우
            try {
                if (imageByAttachmentType.size() > 0) {
                    for (UploadImage file : imageByAttachmentType) {
                        // 문자열에서 ".com/" 다음의 정보를 추출
                        int startIndex = file.getImageUrl().indexOf(".com/") + 5;
                        String result = file.getImageUrl().substring(startIndex);
                        removeTarget[removeCount] = result;
                        removeCount++;
                    }
                    //등록되어있는 파일 정보 삭제 요청.
                    uploadService.removeS3Files(removeTarget);
                    //데이터베이스에 맵핑되어있는 정보삭제
                    uploadService.removeDatabaseByReviewIdx(request.getReviewIdx());
                }
            } catch (CustomException e) {
                throw new CustomException(CustomExceptionCode.SERVER_ERROR);
            }

            //새롭게 요청온 업로드 이미지를  버킷에 업로드함.
            uploadService.upload(request.getReviewImage(), request.getMemberIdx(), AttachmentType.REVIEW, createdReview.getIdx());

            //업로드된 이미지 정보를 데이터베이스
            List<UploadImage> getRepresentIdx = uploadService.getImageByAttachmentType(AttachmentType.REVIEW, createdReview.getIdx());

            createdReview.setContent(request.getContent());
            createdReview.setMemberIdx(request.getMemberIdx());
            createdReview.setStoreIdx(request.getStoreIdx());
            createdReview.setCreated(createdReview.getCreated());//생성일은 그대로.
            createdReview.setModified(LocalDateTime.now());
            createdReview.setRepresentIdx(getRepresentIdx.get(0).getIdx()); //업로드 이미지의 1번째를 리뷰의 대표이미지로 지정함.

            reviewRepository.save(createdReview);
        } catch (CustomException e) {
            System.err.println("modifyJournal Exception : " + e);
        }
    }

    @Override
    public void removeJournal(long reviewIdx) {
        try {
            reviewRepository.deleteById(reviewIdx);

            //유효성 검증에 모두 통과했다면 버킷에 업로드되어있는 리뷰 파일을 모두 삭제합니다.
            //해당 리뷰에 업로드 등록되어있는 이미지를 검색합니다.
            List<UploadImage> imageByAttachmentType = uploadService.getImageByAttachmentType(AttachmentType.REVIEW, reviewIdx);
            String[] removeTarget = new String[imageByAttachmentType.size() + 1];

            int removeCount = 0;
            //업로드된 이미지가 잇는 경우
            try {
                if (imageByAttachmentType.size() > 0) {
                    for (UploadImage file : imageByAttachmentType) {
                        // 문자열에서 ".com/" 다음의 정보를 추출
                        int startIndex = file.getImageUrl().indexOf(".com/") + 5;
                        String result = file.getImageUrl().substring(startIndex);
                        removeTarget[removeCount] = result;
                        removeCount++;
                    }
                    //등록되어있는 파일 정보 삭제 요청.
                    uploadService.removeS3Files(removeTarget);
                    //데이터베이스에 맵핑되어있는 정보삭제
                    uploadService.removeDatabaseByReviewIdx(reviewIdx);
                }
            } catch (CustomException e) {
                throw new CustomException(CustomExceptionCode.SERVER_ERROR);
            }
        } catch (CustomException e) {
            throw new CustomException(CustomExceptionCode.SERVER_ERROR);
        } catch (EmptyResultDataAccessException e) {
            throw new CustomException(CustomExceptionCode.NOT_FOUND);
        }
    }

    /**
     * @title 해당 게시글에 작성된 리뷰 개수 확인.
     * @return 리뷰 개수
     */
    @Override
    public long getTotalReviewCount(long storeIdx) {


        return reviewRepository.countByStoreIdx(storeIdx);
    }

    @Override
    public List<ReviewTagType> getAllReviewTags() {
        return reviewTagTypeRepository.findAll();
    }

    /**
     * @param storeIdx
     * @return
     * @title 좋아요가 가장 많은 리뷰.
     */
    @Override
    public Map<String, Object> getMostLikeReview(long storeIdx) {
        //가장 많이 좋아요를 받은 대표 리뷰정보의 uploadImageList
        Map<String, Object> mostLikeReview = reviewMapper.findMostLikeReview(storeIdx);

        //해당 리뷰 등록된 이미지 맵핑
        List uploadList = new ArrayList<>();
        Map<String,Object> item = new HashMap<>();
        List<UploadImage> uploadImages = uploadService.getImageByAttachmentType(AttachmentType.REVIEW, (Long) mostLikeReview.get("idx"));

        for (UploadImage v : uploadImages) {
            item.put("fileName", v.getFileName ());
            item.put("fileSize", v.getFileSize());
            item.put("imageUrl", v.getImageUrl());
            uploadList.add(item);
        }
        mostLikeReview.put("uploadImage", uploadList);


        return mostLikeReview;
    }


    /**
     * @title 가게 탐험일지 리스트 요청
     * @param storeIdx
     * @param pageNum 페이지 넘버
     * @param pageSize 각 페이지 표시 개수.
     * @return
     */
    @Override
    public List<ReviewResponse.StoreJounalDto> getStoreJounalList(Long storeIdx, int pageNum,int pageSize) {
        Pageable pageable = PageRequest.of(pageNum, pageSize);
        //해당 storeIdx의 리뷰들을 찾아옴.
        List<Review> byStoreIdx = reviewRepository.findByStoreIdx(storeIdx,pageable);

        //해당 가게에 작성된 리뷰가 0개인 경우
        if(byStoreIdx.size() == 0) {
            throw new CustomException(CustomExceptionCode.NOT_FOUND);
        }

        List<ReviewResponse.StoreJounalDto> storeJounalDtos = new ArrayList<>();

        //작성된 리뷰를 DTO로 변환합니다.
        for (Review review : byStoreIdx) {

            List<UploadImage> imageByAttachmentType = uploadService.getImageByAttachmentType(AttachmentType.REVIEW, review.getIdx());

            //작성된 리뷰에 업로드된 이미지 정보를 가져옵니다.
            Map<String,Object> reviewImage = new HashMap<>();
            List<Map<String,Object>> imageList = new ArrayList<>();

            for (UploadImage uploadImage : imageByAttachmentType) {
                reviewImage.put("imageUrl",uploadImage.getImageUrl());
                reviewImage.put("fileName",uploadImage.getFileName());
                reviewImage.put("fileSize",uploadImage.getFileSize());
            }

            imageList.add(reviewImage);

            //해당 스토어의 모든 리뷰에 붙은 태그정보를 조회함.
            List<Map<String, Object>> review_tag_list = reviewMapper.selectList(storeIdx);
            //각 리뷰의 태그 정보를 담을 빈 리스트
            List<Map<String, Object>> tagList = new ArrayList<>();

            //해당 리뷰에 달린 태그들을 찾아냄.
            for (Map<String, Object> tag : review_tag_list) {
                if (tag.get("review_idx") == review.getIdx()) {
                    tagList.add(tag);
                }
            }

            //완성된 정보를 Dto에 맵핑
            storeJounalDtos.add(ReviewResponse.StoreJounalDto.builder()
                    .reviewidx(review.getIdx())
                    .storeIdx(storeIdx)
                    .content(review.getContent())
                    .memberIdx(review.getMemberIdx())
                    .jounalImage(imageList)
                    .tagList(tagList)
                    .created(review.getCreated())
                    .modified(review.getModified())
                    .build());
        }

        return storeJounalDtos;
    }


    /**
     * @title 리뷰타입 정보 출력
     * @param idx
     * @return
     */
    @Override
    public ReviewTagType getReivewTypeByIdx(long idx) {
        Optional<ReviewTagType> reviewTag = reviewTagTypeRepository.findById(idx);
        System.out.println("reviewTag : "+reviewTag.get());
        return reviewTag.orElse(null);
    }
}
