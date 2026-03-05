package com.andreidombrovschi.tradefinance.party.repository;

import com.andreidombrovschi.tradefinance.party.model.Party;
import com.andreidombrovschi.tradefinance.party.model.PartyType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PartyRepository extends JpaRepository<Party, Long> {
    List<Party> findByType(PartyType type);
    boolean existsByIdAndType(Long id, PartyType type);
}
