package com.andreidombrovschi.tradefinance.party.controller;

import com.andreidombrovschi.tradefinance.party.model.Party;
import com.andreidombrovschi.tradefinance.party.model.PartyType;
import com.andreidombrovschi.tradefinance.party.service.PartyService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/parties")
public class PartyController {

    private final PartyService service;

    public PartyController(PartyService service) {
        this.service = service;
    }

    @GetMapping("/{id}")
    public Party getById(@PathVariable Long id) {
        return service.getById(id);
    }

    @GetMapping
    public List<Party> list(@RequestParam(required = false) PartyType type) {
        return service.listByType(type);
    }
}
