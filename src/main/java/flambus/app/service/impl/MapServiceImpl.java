package flambus.app.service.impl;

import flambus.app.dto.map.MapResponse;
import flambus.app.entity.Member;
import flambus.app.mapper.StoreMapper;
import flambus.app.repository.MemberRepository;
import flambus.app.repository.StoreRepository;
import flambus.app.service.MapService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Slf4j
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class MapServiceImpl implements MapService {
    private final StoreRepository storeRepository;
    private final StoreMapper storeMapper;
    private final MemberRepository memberRepository;

    /**
     * @return
     * @title 맵에 표시할 마커(가게) 정보를 반환합x니다.
     */
    @Override
    public List<MapResponse.MapStoreMarker> getStoreInfoByMap(long memberIdx) {
        List<Map<String, Object>> marker = storeMapper.getMapStoreMarker(memberIdx);

        List<MapResponse.MapStoreMarker> dto = new ArrayList();

        for (Map<String, Object> data : marker) {


//            boolean hasReview = ((Integer) data.get("has_review")) == 1;
            boolean hasReview = ((Integer) data.get("has_review")).equals(1);

            dto.add(MapResponse.MapStoreMarker.builder()
                    .storeIdx((Long) data.get("idx"))
                    .storeName((String) data.get("name"))
                    .location(MapResponse.Location.builder()
                            .lng((Double) data.get("latitude"))
                            .lat((Double) data.get("longitude"))
                            .build())
                    .journalCount((Long) data.get("review_count"))
                    .hasReview(hasReview)
                    .build());
        }
        return dto;

    }
}
