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

    @Query(value = "select avg(average) from german_average_prices where station = ?1 and TO_CHAR(DATE(date), 'MM') = ?2")
    Double getAveragePriceByStationAndMonth(String station, String month);
}
