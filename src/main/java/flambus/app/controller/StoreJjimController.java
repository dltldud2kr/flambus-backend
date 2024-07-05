package flambus.app.controller;


import flambus.app._enum.ApiResponseCode;
import flambus.app.dto.ResultDTO;
import flambus.app.exception.CustomException;
import flambus.app.repository.StoreJjimRepository;
import flambus.app.service.StoreJjimService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/jjim")
@Tag(name = "가게 찜", description = "")
@CrossOrigin(origins = "*", exposedHeaders = {"Content-Disposition"}, allowedHeaders = "*")
public class StoreJjimController {

    private final StoreJjimService storeJjimService;

    @PostMapping
    public ResultDTO storeJjim (@RequestParam("storeIdx") long storeIdx, Principal principal) {

        String email = principal.getName();

        storeJjimService.addJjim(storeIdx, email);

        try {
            return ResultDTO.of(true, ApiResponseCode.SUCCESS.getCode(), "SUCCESS", null);
        } catch (CustomException e) {
            return ResultDTO.of(false, e.getCustomErrorCode().getStatusCode(), e.getDetailMessage(), null);
        }


    }
}
