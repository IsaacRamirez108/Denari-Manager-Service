package com.denari.manager.repositories;

import com.denari.manager.models.entity.User.PropertyManagerInfo;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PropertyManagerInfoRepository extends JpaRepository<PropertyManagerInfo, Long> {
}
