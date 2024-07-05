package flambus.app.mapper;

import flambus.app.dto.review.ReviewResponse;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Map;

@Mapper
public interface ReviewMapper {

    //ReviewResponse.JournalTag
    List<Map<String,Object>> selectList(@Param("storeIdx") Long storeIdx);

    Map<String,Object> findMostLikeReview(@Param("storeIdx") Long storeIdx);



}
