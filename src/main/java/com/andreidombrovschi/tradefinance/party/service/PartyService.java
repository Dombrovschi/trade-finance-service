package com.andreidombrovschi.tradefinance.party.service;

import com.andreidombrovschi.tradefinance.party.model.Party;
import com.andreidombrovschi.tradefinance.party.model.PartyType;
import com.andreidombrovschi.tradefinance.party.repository.PartyRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PartyService {

    private final PartyRepository repo;

    public PartyService(PartyRepository repo) {
        this.repo = repo;
    }

    public Party getById(Long id) {
        return repo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Party not found: " + id));
    }

    public List<Party> listByType(PartyType type) {
        if (type == null) return repo.findAll();
        return repo.findByType(type);
    }

    public boolean exists(Long id, PartyType type) {
        return repo.existsByIdAndType(id, type);
    }
}
