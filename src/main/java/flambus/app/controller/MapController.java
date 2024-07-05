package flambus.app.controller;


import flambus.app._enum.ApiResponseCode;
import flambus.app.dto.ResultDTO;
import flambus.app.dto.map.MapResponse;
import flambus.app.dto.member.JoinRequestDto;
import flambus.app.dto.member.LoginRequestDto;
import flambus.app.dto.member.MemberDto;
import flambus.app.dto.member.TokenDto;
import flambus.app.entity.Member;
import flambus.app.exception.CustomException;
import flambus.app.service.EmailService;
import flambus.app.service.MapService;
import flambus.app.service.MemberService;
import flambus.app.service.ReviewService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;


@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/map")
@Tag(name = "지도 관련 요청 API", description = "지도와 관련된 API 입니다.")
@CrossOrigin(origins = "*", exposedHeaders = {"Content-Disposition"}, allowedHeaders = "*")
public class MapController {

    @Autowired
    private MapService mapService;

    @Operation(summary = "지도 내 맛집(마커)정보 요청", description = "" +
            "사용자가 요청한 좌표로 서버에 등록된 가게 정보를 반환합니다." +
            "\n### HTTP STATUS 에 따른 요청 결과" +
            "\n- 200: 서버요청 정상 성공" +
            "\n- 500: 서버에서 요청 처리중 문제가 발생했습니다." +
            "\n### Result Code 에 따른 요청 결과" +
            "\n- " +
            "\n- ")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "요청 성공"),
    })
    @GetMapping("/store")
    public ResultDTO<List<MapResponse.MapStoreMarker>> getStoreByMaps(@RequestParam long memberIdx) {
        try {
            System.out.println("hello");
            List<MapResponse.MapStoreMarker> markerDto = mapService.getStoreInfoByMap(memberIdx);
            return ResultDTO.of(true, ApiResponseCode.SUCCESS.getCode(), "Success", markerDto);
        } catch (CustomException e) {
            return ResultDTO.of(false, e.getCustomErrorCode().getStatusCode(), e.getDetailMessage(), null);
        }
    }
}
