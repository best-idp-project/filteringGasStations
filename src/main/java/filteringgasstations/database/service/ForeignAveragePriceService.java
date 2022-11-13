package filteringgasstations.database.service;

import com.google.common.collect.ImmutableList;
import filteringgasstations.database.models.ForeignAveragePrice;
import filteringgasstations.database.repositories.ForeignAveragePriceRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.Optional;

@Service
public class ForeignAveragePriceService {

    @Autowired
    private ForeignAveragePriceRepository repository;

    public void save(ForeignAveragePrice entry) {
        repository.save(entry);
    }

    public Optional<ForeignAveragePrice> get(String id) {
        return repository.findById(id);
    }

    public List<ForeignAveragePrice> getAll() {return ImmutableList.copyOf(repository.findAll()); }

    public List<ForeignAveragePrice> getAllByStation(String station) {
        return repository.getAllByStation(station);
    }
    public List<ForeignAveragePrice> getAllByStationAndDate(String station, Date date) {
        return repository.getAllByStationAndDate(station, date);
    }

    public void saveAll(List<ForeignAveragePrice> prices) {
        repository.saveAll(prices);
    }

    public List<String> getAllIds() { return repository.getIds(); }
}
