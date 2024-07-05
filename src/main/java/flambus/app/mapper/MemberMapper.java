package flambus.app.mapper;

import flambus.app.dto.member.MemberDto;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface MemberMapper {

    List<MemberDto> selectList(MemberDto parameter);

}
