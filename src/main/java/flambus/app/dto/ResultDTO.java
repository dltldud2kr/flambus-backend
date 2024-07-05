package flambus.app.dto;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@RequiredArgsConstructor(staticName = "of")
public class ResultDTO<D> {
    private final boolean success;
    private final String resultCode;
    private final String message;
    private final D data;
}
