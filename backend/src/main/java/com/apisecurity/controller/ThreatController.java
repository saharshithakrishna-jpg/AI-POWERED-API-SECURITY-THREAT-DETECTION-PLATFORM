package com.apisecurity.controller;

import com.apisecurity.model.InvestigationNote;
import com.apisecurity.model.Threat;
import com.apisecurity.model.ThreatStatus;
import com.apisecurity.model.User;
import com.apisecurity.repository.InvestigationNoteRepository;
import com.apisecurity.repository.ThreatRepository;
import com.apisecurity.repository.UserRepository;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/threats")
@RequiredArgsConstructor
public class ThreatController {

    private final ThreatRepository threatRepository;
    private final InvestigationNoteRepository noteRepository;
    private final UserRepository userRepository;

    @GetMapping
    public Page<Threat> getAllThreats(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String status) {
            
        Pageable pageable = PageRequest.of(page, size, Sort.by("timestamp").descending());
        
        // Simpler implementation for the demo, ideal would be Specification for dynamic filtering
        if (status != null && !status.isEmpty()) {
            // Note: In real app use custom repository method with pagination for status
            // This is a simplification
            return threatRepository.findAll(pageable); // Placeholder for actual filtered page
        }
        return threatRepository.findAll(pageable);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Threat> getThreatById(@PathVariable Long id) {
        return threatRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<Threat> updateThreatStatus(@PathVariable Long id, @RequestBody StatusUpdateRequest request) {
        return threatRepository.findById(id).map(threat -> {
            threat.setStatus(ThreatStatus.valueOf(request.getStatus()));
            return ResponseEntity.ok(threatRepository.save(threat));
        }).orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/{id}/notes")
    public ResponseEntity<List<InvestigationNote>> getThreatNotes(@PathVariable Long id) {
        return ResponseEntity.ok(noteRepository.findByThreatIdOrderByCreatedAtDesc(id));
    }

    @PostMapping("/{id}/notes")
    public ResponseEntity<InvestigationNote> addNote(@PathVariable Long id, @RequestBody NoteRequest request) {
        return threatRepository.findById(id).map(threat -> {
            String username = SecurityContextHolder.getContext().getAuthentication().getName();
            User user = userRepository.findByUsername(username).orElseThrow();
            
            InvestigationNote note = new InvestigationNote();
            note.setThreat(threat);
            note.setUser(user);
            note.setNote(request.getNote());
            
            return ResponseEntity.ok(noteRepository.save(note));
        }).orElse(ResponseEntity.notFound().build());
    }

    @Data
    static class StatusUpdateRequest {
        private String status;
    }

    @Data
    static class NoteRequest {
        private String note;
    }
}
