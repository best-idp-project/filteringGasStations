package filteringgasstations.database.repositories;

import filteringgasstations.database.models.Competitors;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface CompetitorsRepository extends CrudRepository<Competitors, String> {

    @Query(value = "select id from competitors")
    List<String> getIds();
}
