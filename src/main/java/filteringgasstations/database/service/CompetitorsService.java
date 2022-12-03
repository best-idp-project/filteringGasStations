package filteringgasstations.database.service;

import filteringgasstations.database.models.Competitors;
import filteringgasstations.database.repositories.CompetitorsRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CompetitorsService {
    @Autowired
    private CompetitorsRepository repository;

    public void save(Competitors entry) {
        repository.save(entry);
    }

    public Competitors get(String id) {
        return repository.findById(id).orElse(null);
    }

    public List<String> getIds() {
        return repository.getIds();
    }

    public List<String> getAllStations() {
        return repository.getAllStations();
    }

    public List<String> getAllCompetitorsOfStation(String station) {
        return repository.getAllCompetitorsOfStation(station);
    }

    public Competitors findByFirstStationAndSecondStation(String firstStation, String secondStation) {
        return repository.findByFirstStationAndSecondStation(firstStation, secondStation);
    }
}
