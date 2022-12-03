package filteringgasstations.database.service;

import filteringgasstations.database.models.PriceDifferenceOfCompetitors;
import filteringgasstations.database.repositories.PriceDifferenceOfCompetitorsRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PriceDifferenceOfCompetitorsService {
    @Autowired
    private PriceDifferenceOfCompetitorsRepository repository;

    public void save(PriceDifferenceOfCompetitors entry) {
        repository.save(entry);
    }

    public void saveAll(List<PriceDifferenceOfCompetitors> list) {
        repository.saveAll(list);
    }

    public PriceDifferenceOfCompetitors get(String id) {
        return repository.findById(id).orElse(null);
    }


    public List<String> getIds() {
        return repository.getIds();
    }

    public Iterable<PriceDifferenceOfCompetitors> getAllPriceDifferenceOfCompetitors() {
        return repository.findAll();
    }
}
