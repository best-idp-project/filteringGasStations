package filteringgasstations.database.service;

import filteringgasstations.database.models.InputFile;
import filteringgasstations.database.models.OSRMCache;
import filteringgasstations.database.repositories.InputFileRepository;
import filteringgasstations.database.repositories.OSRMCacheRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class InputFileService {

    @Autowired
    private InputFileRepository repository;

    public void save(InputFile entry) {
        repository.save(entry);
    }

    public Optional<InputFile> get(String filename) {
        return repository.findById(filename);
    }

    public void delete(String filename) {
        try {
            repository.deleteById(filename);
        } catch (Exception e) {

        }
    }
}
