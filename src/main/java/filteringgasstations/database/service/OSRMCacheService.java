package filteringgasstations.database.service;

import filteringgasstations.database.models.OSRMCache;
import filteringgasstations.database.repositories.OSRMCacheRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class OSRMCacheService {

    @Autowired
    private OSRMCacheRepository repository;

    public void save(OSRMCache entry) {
        repository.save(entry);
    }

    public Optional<OSRMCache> get(String uri) {
        return repository.findById(uri);
    }
}
