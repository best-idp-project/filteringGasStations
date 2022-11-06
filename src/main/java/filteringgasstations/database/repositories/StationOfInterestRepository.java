package filteringgasstations.database.repositories;

import filteringgasstations.database.models.StationOfInterest;
import filteringgasstations.geolocation.BorderPoint;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface StationOfInterestRepository extends CrudRepository<StationOfInterest, String> {

    List<StationOfInterest> getAllByCountry(String country);
}
