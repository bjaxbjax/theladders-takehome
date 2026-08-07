package com.theladders.data;

import com.theladders.model.Location;
import org.springframework.data.repository.ListCrudRepository;

import java.util.List;

public interface LocationRepository extends ListCrudRepository<Location, Long> {
    List<Location> findByCityAndStateAndCountry(String city, String state, String country);
}
