package flambus.app.controller;

import flambus.app._enum.ApiResponseCode;
import flambus.app.dto.ResultDTO;
import flambus.app.dto.store.CreateStoreDto;
import flambus.app.dto.store.StoreDto;
import flambus.app.exception.CustomException;
import flambus.app.service.StoreService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/store")
@Tag(name = "스토어,가게 정보 관련 API", description = "")
@CrossOrigin(origins = "*", exposedHeaders = {"Content-Disposition"}, allowedHeaders = "*")
public class StoreController {

    private final StoreService storeService;


    @PostMapping("/create")
    public ResultDTO createStore(@RequestBody CreateStoreDto dto){


        try {
            boolean result = storeService.createStore(dto);
            return ResultDTO.of(result, ApiResponseCode.SUCCESS.getCode(), "가게 등록 완료",null);

        } catch (CustomException e) {
            return ResultDTO.of(false, e.getCustomErrorCode().getStatusCode(), e.getDetailMessage(), null);
        }
    }

    @Operation(summary = "가게 정보 요청", description = "해당 가게 정보를 단건으로 요청" +
            "\n### HTTP STATUS 에 따른 조회 결과" +
            "\n- 200: 서버요청 정상 성공 "+
            "\n- 500: 서버에서 요청 처리중 문제가 발생" +
            "\n### Result Code 에 따른 요청 결과" +
            "\n- ")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "서버 요청 성공"),
    })
    @GetMapping
    public ResultDTO<StoreDto> getStore(@RequestParam("storeIdx") long storeIdx) {
        try {
            StoreDto storeInfo = storeService.getStoreInfo(storeIdx);
            return ResultDTO.of(storeInfo != null, ApiResponseCode.SUCCESS.getCode(), storeInfo != null ? "성공" : "해당 StoreIdx 정보를 찾을 수 없습니다.",storeInfo);

        } catch (CustomException e) {
            return ResultDTO.of(false, e.getCustomErrorCode().getStatusCode(), e.getDetailMessage(), null);
        }
  }


    @Operation(summary = "나만의 가게로 지정(찜)", description = "나만의 가게 해제를 원할 경우 한번더 요청하면됨." +
            "\n### HTTP STATUS 에 따른 조회 결과" +
            "\n- 200: 서버요청 정상 성공 "+
            "\n- 500: 서버에서 요청 처리중 문제가 발생" +
            "\n### Result Code 에 따른 요청 결과" +
            "\n- ")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "서버 요청 성공"),
    })
    @PostMapping("/jjim")
    public ResultDTO updateJjimStore() {
        return null;
    }







}
