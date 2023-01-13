package com.crochess.backend.repositories;

import com.crochess.backend.models.DrawRecord;
import org.springframework.data.repository.CrudRepository;

public interface DrawRecordRepository extends CrudRepository<DrawRecord, Integer> {
}
