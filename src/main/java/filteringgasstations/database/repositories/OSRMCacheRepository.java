package filteringgasstations.database.repositories;

import filteringgasstations.database.models.OSRMCache;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OSRMCacheRepository extends CrudRepository<OSRMCache, String> {
}
