package flambus.app.service;


import flambus.app.dto.map.MapResponse;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface MapService {

        List<MapResponse.MapStoreMarker> getStoreInfoByMap(long memberIdx);


}
