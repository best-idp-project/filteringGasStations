package filteringgasstations.database.repositories;

import filteringgasstations.database.models.InputFile;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface InputFileRepository extends CrudRepository<InputFile, String> {
}
