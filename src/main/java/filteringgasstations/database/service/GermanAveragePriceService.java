package filteringgasstations.database.service;

import com.google.common.collect.ImmutableList;
import filteringgasstations.database.models.GermanAveragePrice;
import filteringgasstations.database.repositories.GermanAveragePriceRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.Optional;

@Service
public class GermanAveragePriceService {

    @Autowired
    private GermanAveragePriceRepository repository;

    public void save(GermanAveragePrice entry) {
        repository.save(entry);
    }

    public Optional<GermanAveragePrice> get(String id) {
        return repository.findById(id);
    }

    public List<GermanAveragePrice> getAll() {return ImmutableList.copyOf(repository.findAll()); }

    public List<GermanAveragePrice> getAllByStation(String station) {
        return repository.getAllByStation(station);
    }
    public List<GermanAveragePrice> getAllByStationAndDate(String station, Date date) {
        return repository.getAllByStationAndDate(station, date);
    }

    public void saveAll(List<GermanAveragePrice> prices) {
        repository.saveAll(prices);
    }

    public List<String> getAllIds() { return repository.getIds(); }
}
