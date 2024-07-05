package flambus.app.repository;//package flambus.flambus_v10.repository;

import flambus.app.entity.Review;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;


@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {

    Long countByStoreIdx(long storeIdx);
    List<Review> findByStoreIdx(long storeIdx,Pageable pageable);

}
