package flambus.app.service.impl;

import flambus.app._enum.CustomExceptionCode;
import flambus.app.dto.ResultDTO;
import flambus.app.entity.Member;
import flambus.app.entity.StoreJjim;
import flambus.app.exception.CustomException;
import flambus.app.mapper.StoreJjimMapper;
import flambus.app.repository.MemberRepository;
import flambus.app.repository.StoreJjimRepository;
import flambus.app.service.StoreJjimService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;


@Slf4j
@Service
@RequiredArgsConstructor
public class StoreJjimServiceImpl implements StoreJjimService {
    private final MemberRepository memberRepository;

    private final StoreJjimRepository storeJjimRepository;
    private final StoreJjimMapper storeJjimMapper;

    // 가게 찜하기
    @Override
    public ResultDTO addJjim(long storeIdx, String email) {


        Member member = memberRepository.findByEmail(email)
                .orElseThrow(() ->  new CustomException(CustomExceptionCode.NOT_FOUND_USER));

        long memberIdx = member.getIdx();


        StoreJjim existingJjim = storeJjimMapper.findByMemberIdxAndStoreIdx(memberIdx, storeIdx);


        if (existingJjim != null) {
            // 찜이 이미 존재하는 경우 삭제
            storeJjimMapper.deleteByMemberIdxAndStoreIdx(memberIdx, storeIdx);
        } else {
            // 찜이 존재하지 않는 경우 추가
            StoreJjim newJjim = new StoreJjim();
            newJjim.setMemberIdx(member.getIdx());
            newJjim.setStoreIdx(storeIdx);
            newJjim.setCreated(LocalDateTime.now());

            storeJjimRepository.save(newJjim);
        }

        return null;
    }
}
