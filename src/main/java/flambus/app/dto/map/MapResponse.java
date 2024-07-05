package flambus.app.dto.map;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;


@Builder
@Data
@AllArgsConstructor
public class MapResponse {

    @Builder
    @Data
    @AllArgsConstructor
    public static class MapStoreMarker {
        private long storeIdx;
        private String storeName;
        private Location location;
        private long journalCount; //리뷰개수

        private boolean hasReview;

    }
    @Builder
    @Data
    @AllArgsConstructor
    public static class Location {
        private Double lat;
        private Double lng;
    }
}
