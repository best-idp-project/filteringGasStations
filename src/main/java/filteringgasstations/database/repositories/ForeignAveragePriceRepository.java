package filteringgasstations.database.repositories;

import filteringgasstations.database.models.ForeignAveragePrice;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;

@Repository
public interface ForeignAveragePriceRepository extends CrudRepository<ForeignAveragePrice, String> {

    List<ForeignAveragePrice> getAllByStation(String station);
    List<ForeignAveragePrice> getAllByStationAndDate(String station, Date date);

    @Query(value = "select id from foreign_average_prices")
    List<String> getIds();
}
