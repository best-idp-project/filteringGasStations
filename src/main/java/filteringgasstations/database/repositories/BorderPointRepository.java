package filteringgasstations.database.repositories;

import filteringgasstations.database.models.InputFile;
import filteringgasstations.geolocation.BorderPoint;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BorderPointRepository extends CrudRepository<BorderPoint, String> {
}
