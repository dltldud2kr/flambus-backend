package flambus.app.repository;//package flambus.flambus_v10.repository;

import flambus.app.dto.store.StoreDto;
import flambus.app.entity.Member;
import flambus.app.entity.Store;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;


@Repository
public interface StoreRepository extends JpaRepository<Store, Long> {

}
