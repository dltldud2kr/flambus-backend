package flambus.app.repository;

import flambus.app.entity.StoreJjim;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface StoreJjimRepository extends JpaRepository<StoreJjim, Long > {


}
