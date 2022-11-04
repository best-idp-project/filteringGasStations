package filteringgasstations.database.repositories;

import filteringgasstations.database.models.StationOfInterest;
import filteringgasstations.geolocation.BorderPoint;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface StationOfInterestRepository extends CrudRepository<StationOfInterest, String> {
}
