package fr.lab.iahomelab.setup.controller;

import fr.lab.iahomelab.common.api.ApiV1Controller;
import fr.lab.iahomelab.setup.controller.dto.CreateSetupVersionRequest;
import fr.lab.iahomelab.setup.controller.dto.SetupVersionResponse;
import fr.lab.iahomelab.setup.controller.dto.UpdateSetupVersionRequest;
import fr.lab.iahomelab.setup.service.SetupVersionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.net.URI;
import java.util.List;
import java.util.UUID;

@ApiV1Controller
@RequiredArgsConstructor
public class SetupVersionController {

    private final SetupVersionService setupVersionService;

    @PostMapping("/setups/{setupId}/versions")
    public ResponseEntity<SetupVersionResponse> create(
            @PathVariable UUID setupId,
            @Valid @RequestBody CreateSetupVersionRequest request
    ) {
        SetupVersionResponse version = setupVersionService.create(setupId, request);

        return ResponseEntity
                .created(URI.create("/api/v1/setup-versions/" + version.id()))
                .body(version);
    }

    @GetMapping("/setups/{setupId}/versions")
    public ResponseEntity<List<SetupVersionResponse>> list(
            @PathVariable UUID setupId
    ) {
        return ResponseEntity.ok(setupVersionService.list(setupId));
    }

    @GetMapping("/setup-versions/{id}")
    public ResponseEntity<SetupVersionResponse> getById(
            @PathVariable UUID id
    ) {
        return ResponseEntity.ok(setupVersionService.getById(id));
    }

    @PutMapping("/setup-versions/{id}")
    public ResponseEntity<SetupVersionResponse> update(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateSetupVersionRequest request
    ) {
        return ResponseEntity.ok(setupVersionService.update(id, request));
    }

    @PostMapping("/setup-versions/{id}/freeze")
    public ResponseEntity<SetupVersionResponse> freeze(
            @PathVariable UUID id
    ) {
        return ResponseEntity.ok(setupVersionService.freeze(id));
    }

    @DeleteMapping("/setup-versions/{id}")
    public ResponseEntity<Void> delete(
            @PathVariable UUID id
    ) {
        setupVersionService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
