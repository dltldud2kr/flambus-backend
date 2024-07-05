package flambus.app.mapper;

import flambus.app.dto.member.MemberDto;
import flambus.app.entity.StoreJjim;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface StoreJjimMapper {

    StoreJjim findByMemberIdxAndStoreIdx(@Param("memberIdx") long memberIdx, @Param("storeIdx") long storeIdx);

    void deleteByMemberIdxAndStoreIdx(@Param("memberIdx") long memberIdx, @Param("storeIdx") long storeIdx);

}
