package flambus.app.controller;

import flambus.app._enum.ApiResponseCode;
import flambus.app._enum.AttachmentType;
import flambus.app.dto.ResultDTO;
import flambus.app.entity.UploadImage;
import flambus.app.service.UploadService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.*;

@RequiredArgsConstructor
@RequestMapping("/test/api/v1")
@RestController
@Tag(name = "업로드 관련", description = "업로드 관련(리뷰이미지,프로필이미지 등 관련 API)")
@CrossOrigin(origins = "*", exposedHeaders = {"Content-Disposition"}, allowedHeaders = "*")
public class UploadController {
    private final UploadService uploadService;

    /**
     *
     * @param image 이미지
     * @param userId 사용자 유저 아이디
     * @return
     * @throws IOException
     */
    @Operation(summary = "get posts", description = "지역에 대한 posts들 가져오기")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = ResultDTO.class))),
            @ApiResponse(responseCode = "400", description = "BAD REQUEST"),
            @ApiResponse(responseCode = "404", description = "NOT FOUND"),
            @ApiResponse(responseCode = "500", description = "INTERNAL SERVER ERROR")
    })
    @Parameters({
            @Parameter(name = "province", description = "시", example = "경기도"),
            @Parameter(name = "city", description = "도", example = "고양시"),
            @Parameter(name = "hashtag", description = "검색한 해시태그", example = "['#자장면', '#중국집']")
    })
    @PutMapping(value="/upload",consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResultDTO<Map<String,Object>> saveImage(
            @RequestParam(value="image") List<MultipartFile> image,
            @RequestParam long memberIdx) throws IOException {

        Map<String,Object> sampleArray = new HashMap<>();
        uploadService.upload(image,memberIdx ,AttachmentType.REVIEW,2344);
        return ResultDTO.of(true, ApiResponseCode.CREATED.getCode(),ApiResponseCode.CREATED.getMessage(), sampleArray);
    }

    @GetMapping("/image/{id}")
    public ResultDTO<UploadImage> getImageById(@PathVariable Long id) {
        try{
            Optional<UploadImage> image = uploadService.getImageById(id);
            if (!image.isPresent()) {
                return ResultDTO.of(true, ApiResponseCode.SUCCESS.getCode(), ApiResponseCode.SUCCESS.getMessage(), null);
            } else {
                return ResultDTO.of(true, ApiResponseCode.SUCCESS.getCode(), ApiResponseCode.SUCCESS.getMessage(), image.get());
            }
        } catch(NullPointerException error) {
            Optional<UploadImage> image = uploadService.getImageById(id);
            return ResultDTO.of(true, ApiResponseCode.SUCCESS.getCode(), ApiResponseCode.SUCCESS.getMessage(), image.get());
        }
    }

    @GetMapping("/image/{attachment}/{mappedId}")
    public ResultDTO<List<UploadImage>> getImageByAttachmentType(
            @PathVariable String attachment,
            @PathVariable long mappedId) {
        List<UploadImage> results = uploadService.getImageByAttachmentType(AttachmentType.fromString(attachment),mappedId);
        return ResultDTO.of(true, ApiResponseCode.SUCCESS.getCode(), ApiResponseCode.SUCCESS.getMessage(), results);
    }
}
