package flambus.app.service;

import flambus.app.dto.ResultDTO;
import org.springframework.stereotype.Service;

@Service
public interface StoreJjimService {

    ResultDTO addJjim (long storeIdx, String email);
}
