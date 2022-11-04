package filteringgasstations.database.repositories;

import filteringgasstations.database.models.GermanPrice;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.HashMap;
import java.util.List;

@Repository
public interface GermanPriceRepository extends CrudRepository<GermanPrice, String> {

    List<GermanPrice> getAllByStation(String station);
    List<GermanPrice> getAllByStationAndDate(String station, Date date);

    @Query(value = "select id from german_prices")
    List<String> getIds();
}
