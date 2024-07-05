package flambus.app.repository;//package flambus.flambus_v10.repository;

import flambus.app.entity.Review;
import flambus.app.entity.ReviewTagType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


@Repository
public interface ReviewTagTypeRepository extends JpaRepository<ReviewTagType, Long> {

}
