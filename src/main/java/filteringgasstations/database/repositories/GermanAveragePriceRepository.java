package filteringgasstations.database.repositories;

import filteringgasstations.database.models.GermanAveragePrice;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;

@Repository
public interface GermanAveragePriceRepository extends CrudRepository<GermanAveragePrice, String> {

    List<GermanAveragePrice> getAllByStation(String station);
    List<GermanAveragePrice> getAllByStationAndDate(String station, Date date);

    @Query(value = "select id from german_average_prices")
    List<String> getIds();
}
