package filteringgasstations.database.repositories;

import filteringgasstations.database.models.PriceDifferenceOfCompetitors;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface PriceDifferenceOfCompetitorsRepository extends CrudRepository<PriceDifferenceOfCompetitors, String> {

    @Query(value = "select id from price_per_km_comparison")
    List<String> getIds();
}
