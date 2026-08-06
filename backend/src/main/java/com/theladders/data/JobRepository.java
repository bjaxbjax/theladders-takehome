package com.theladders.data;

import com.theladders.model.Job;
import org.springframework.data.repository.ListCrudRepository;
import org.springframework.data.repository.PagingAndSortingRepository;

public interface JobRepository extends ListCrudRepository<Job, Long>, PagingAndSortingRepository<Job, Long> {
}
