package filteringgasstations.database.service;

import com.google.common.collect.ImmutableList;
import filteringgasstations.database.models.GermanPrice;
import filteringgasstations.database.repositories.BorderPointRepository;
import filteringgasstations.database.repositories.GermanPriceRepository;
import filteringgasstations.geolocation.BorderPoint;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class GermanPriceService {

    @Autowired
    private GermanPriceRepository repository;

    public void save(GermanPrice entry) {
        repository.save(entry);
    }

    public Optional<GermanPrice> get(String id) {
        return repository.findById(id);
    }

    public List<GermanPrice> getAll() {return ImmutableList.copyOf(repository.findAll()); }
    public void purge() { repository.deleteAll(); }
}
