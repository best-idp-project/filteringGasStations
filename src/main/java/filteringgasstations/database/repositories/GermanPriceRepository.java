package filteringgasstations.database.repositories;

import filteringgasstations.database.models.GermanPrice;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface GermanPriceRepository extends CrudRepository<GermanPrice, String> {
}
