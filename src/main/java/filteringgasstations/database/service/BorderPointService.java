package filteringgasstations.database.service;

import com.google.common.collect.ImmutableList;
import filteringgasstations.database.repositories.BorderPointRepository;
import filteringgasstations.geolocation.BorderPoint;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class BorderPointService {

    @Autowired
    private BorderPointRepository repository;

    public void save(BorderPoint entry) {
        repository.save(entry);
    }

    public Optional<BorderPoint> get(String id) {
        return repository.findById(id);
    }

    public List<BorderPoint> getAll() {return ImmutableList.copyOf(repository.findAll()); }
    public void purge() { repository.deleteAll(); }
}
