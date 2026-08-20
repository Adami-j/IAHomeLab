package fr.lab.iahomelab.setup.controller;

import fr.lab.iahomelab.common.api.ApiV1Controller;
import fr.lab.iahomelab.setup.controller.dto.ConnectionResponse;
import fr.lab.iahomelab.setup.controller.dto.CreateConnectionRequest;
import fr.lab.iahomelab.setup.controller.dto.UpdateConnectionRequest;
import fr.lab.iahomelab.setup.service.ConnectionService;
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
public class ConnectionController {

    private final ConnectionService connectionService;

    @PostMapping("/setup-versions/{setupVersionId}/connections")
    public ResponseEntity<ConnectionResponse> create(
            @PathVariable UUID setupVersionId,
            @Valid @RequestBody CreateConnectionRequest request
    ) {
        ConnectionResponse connection = connectionService.create(
                setupVersionId,
                request
        );

        return ResponseEntity
                .created(URI.create("/api/v1/connections/" + connection.id()))
                .body(connection);
    }

    @GetMapping("/setup-versions/{setupVersionId}/connections")
    public ResponseEntity<List<ConnectionResponse>> list(
            @PathVariable UUID setupVersionId
    ) {
        return ResponseEntity.ok(connectionService.list(setupVersionId));
    }

    @GetMapping("/connections/{id}")
    public ResponseEntity<ConnectionResponse> getById(
            @PathVariable UUID id
    ) {
        return ResponseEntity.ok(connectionService.getById(id));
    }

    @PutMapping("/connections/{id}")
    public ResponseEntity<ConnectionResponse> update(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateConnectionRequest request
    ) {
        return ResponseEntity.ok(connectionService.update(id, request));
    }

    @DeleteMapping("/connections/{id}")
    public ResponseEntity<Void> delete(
            @PathVariable UUID id
    ) {
        connectionService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
