package fr.lab.iahomelab.source.controller;

import fr.lab.iahomelab.common.api.ApiV1Controller;
import fr.lab.iahomelab.source.controller.dto.CreateSourceRequest;
import fr.lab.iahomelab.source.controller.dto.SourceResponse;
import fr.lab.iahomelab.source.controller.dto.UpdateSourceRequest;
import fr.lab.iahomelab.source.service.SourceService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;
import java.util.UUID;

@ApiV1Controller
@RequestMapping("/sources")
@RequiredArgsConstructor
public class SourceController {

    private final SourceService sourceService;

    @PostMapping
    public ResponseEntity<SourceResponse> create(
            @Valid @RequestBody CreateSourceRequest request
    ) {
        SourceResponse source = sourceService.create(request);

        return ResponseEntity
                .created(URI.create("/api/v1/sources/" + source.id()))
                .body(source);
    }

    @GetMapping
    public ResponseEntity<List<SourceResponse>> list() {
        return ResponseEntity.ok(sourceService.list());
    }

    @GetMapping("/{id}")
    public ResponseEntity<SourceResponse> getById(
            @PathVariable UUID id
    ) {
        return ResponseEntity.ok(
                sourceService.getById(id)
        );
    }

    @PutMapping("/{id}")
    public ResponseEntity<SourceResponse> update(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateSourceRequest request
    ) {
        return ResponseEntity.ok(
                sourceService.update(id, request)
        );
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        sourceService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
