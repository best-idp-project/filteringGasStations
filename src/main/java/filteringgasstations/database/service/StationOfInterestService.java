package filteringgasstations.database.service;

import com.google.common.collect.ImmutableList;
import filteringgasstations.database.models.StationOfInterest;
import filteringgasstations.database.repositories.StationOfInterestRepository;
import filteringgasstations.geolocation.CountryCode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class StationOfInterestService {

    @Autowired
    private StationOfInterestRepository repository;

    public void save(StationOfInterest entry) {
        repository.save(entry);
    }

    public Optional<StationOfInterest> get(String id) {
        return repository.findById(id);
    }

    public List<StationOfInterest> getAll() {return ImmutableList.copyOf(repository.findAll()); }

    public List<StationOfInterest> getAllByCountry(CountryCode country) {return repository.getAllByCountry(country.getCode()); }

    //public void purge() { repository.deleteAll(); }
}
