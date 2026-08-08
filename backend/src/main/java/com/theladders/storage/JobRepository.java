package com.theladders.storage;

import com.theladders.model.Job;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.ListCrudRepository;
import org.springframework.data.repository.PagingAndSortingRepository;

public interface JobRepository extends ListCrudRepository<Job, Long>, PagingAndSortingRepository<Job, Long>,
        JpaSpecificationExecutor<Job> {
}
