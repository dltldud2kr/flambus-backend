package flambus.app.repository;//package flambus.flambus_v10.repository;

import flambus.app.entity.UploadImage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UploadRepository  extends JpaRepository<UploadImage, Long> {

    List<UploadImage> findByAttachmentTypeAndMappedId(String attachmentType, long mappedId);

    void deleteByMappedId(long mappedIdx);

}
