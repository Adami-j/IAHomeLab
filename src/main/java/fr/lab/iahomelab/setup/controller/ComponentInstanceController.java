package fr.lab.iahomelab.setup.controller;

import fr.lab.iahomelab.common.api.ApiV1Controller;
import fr.lab.iahomelab.setup.controller.dto.ComponentInstanceResponse;
import fr.lab.iahomelab.setup.controller.dto.CreateComponentInstanceRequest;
import fr.lab.iahomelab.setup.controller.dto.UpdateComponentInstanceRequest;
import fr.lab.iahomelab.setup.service.ComponentInstanceService;
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
public class ComponentInstanceController {

    private final ComponentInstanceService componentInstanceService;

    @PostMapping("/setup-versions/{setupVersionId}/components")
    public ResponseEntity<ComponentInstanceResponse> create(
            @PathVariable UUID setupVersionId,
            @Valid @RequestBody CreateComponentInstanceRequest request
    ) {
        ComponentInstanceResponse component = componentInstanceService.create(
                setupVersionId,
                request
        );

        return ResponseEntity
                .created(URI.create("/api/v1/components/" + component.id()))
                .body(component);
    }

    @GetMapping("/setup-versions/{setupVersionId}/components")
    public ResponseEntity<List<ComponentInstanceResponse>> list(
            @PathVariable UUID setupVersionId
    ) {
        return ResponseEntity.ok(componentInstanceService.list(setupVersionId));
    }

    @GetMapping("/components/{id}")
    public ResponseEntity<ComponentInstanceResponse> getById(
            @PathVariable UUID id
    ) {
        return ResponseEntity.ok(componentInstanceService.getById(id));
    }

    @PutMapping("/components/{id}")
    public ResponseEntity<ComponentInstanceResponse> update(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateComponentInstanceRequest request
    ) {
        return ResponseEntity.ok(componentInstanceService.update(id, request));
    }

    @DeleteMapping("/components/{id}")
    public ResponseEntity<Void> delete(
            @PathVariable UUID id
    ) {
        componentInstanceService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
