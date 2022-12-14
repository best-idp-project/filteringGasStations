package filteringgasstations.database.repositories;

import filteringgasstations.database.models.Competitors;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface CompetitorsRepository extends CrudRepository<Competitors, String> {

    @Query(value = "select id from competitors")
    List<String> getIds();

    @Query(value = "select distinct(station) from (select distinct(c.first_station) as station from competitors c UNION select distinct(c.second_station) as station from competitors c) as view", nativeQuery = true)
    List<String> getAllStations();

    @Query(value = "select distinct(station) from (select distinct(c.first_station) as station from competitors c where c.second_station = ?1 UNION select distinct(c.second_station) as station from competitors c where c.first_station = ?1) as view", nativeQuery = true)
    List<String> getAllCompetitorsOfStation(String station);

    Competitors findByFirstStationAndSecondStation(String firstStation, String secondStation);

    @Query(value = "select distinct(id) from (select distinct c.first_station as id from competitors c where c.country_first = 'DE' and c.country_second = ?1 union distinct select c.second_station as id from competitors c where c.country_second = 'DE' and c.country_first = ?1) as view", nativeQuery = true)
    List<String> getAllGermanCompetitorsOfCountry(String country);
}
