package com.mode.model;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;

@Repository
public interface SampleRepository extends CrudRepository<Sample, Long> {
    Collection<Sample> findAll();
}
