package fr.lab.iahomelab.setup.controller;

import fr.lab.iahomelab.common.api.ApiV1Controller;
import fr.lab.iahomelab.setup.controller.dto.CreateSetupRequest;
import fr.lab.iahomelab.setup.controller.dto.SetupResponse;
import fr.lab.iahomelab.setup.controller.dto.UpdateSetupRequest;
import fr.lab.iahomelab.setup.service.SetupService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import java.net.URI;
import java.util.List;
import java.util.UUID;

@ApiV1Controller
@RequestMapping("/setups")
@RequiredArgsConstructor
public class SetupController {

    private final SetupService setupService;

    @PostMapping
    public ResponseEntity<SetupResponse> create(
            @Valid @RequestBody CreateSetupRequest request
    ) {
        SetupResponse setup = setupService.create(request);

        return ResponseEntity
                .created(URI.create("/api/v1/setups/" + setup.id()))
                .body(setup);
    }

    @GetMapping
    public ResponseEntity<List<SetupResponse>> list() {
        return ResponseEntity.ok(setupService.list());
    }

    @GetMapping("/{id}")
    public ResponseEntity<SetupResponse> getById(
            @PathVariable UUID id
    ) {
        return ResponseEntity.ok(setupService.getById(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<SetupResponse> update(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateSetupRequest request
    ) {
        return ResponseEntity.ok(setupService.update(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            @PathVariable UUID id
    ) {
        setupService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
