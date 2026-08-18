package fr.lab.iahomelab.sourceidea.controller;

import fr.lab.iahomelab.common.api.ApiV1Controller;
import fr.lab.iahomelab.sourceidea.controller.dto.CreateSourceIdeaRequest;
import fr.lab.iahomelab.sourceidea.controller.dto.SourceIdeaResponse;
import fr.lab.iahomelab.sourceidea.service.SourceIdeaService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import java.net.URI;
import java.util.List;
import java.util.UUID;

@ApiV1Controller
@RequestMapping("/sources/{sourceId}/ideas")
@RequiredArgsConstructor
public class SourceIdeaController {

    private final SourceIdeaService sourceIdeaService;

    @PostMapping
    public ResponseEntity<SourceIdeaResponse> create(
            @PathVariable UUID sourceId,
            @Valid @RequestBody CreateSourceIdeaRequest request
    ) {
        SourceIdeaResponse sourceIdea = sourceIdeaService.create(sourceId, request);

        return ResponseEntity
                .created(URI.create("/api/v1/sources/" + sourceId + "/ideas/" + sourceIdea.id()))
                .body(sourceIdea);
    }

    @GetMapping
    public ResponseEntity<List<SourceIdeaResponse>> getAll(
            @PathVariable UUID sourceId
    ) {
        return ResponseEntity.ok(sourceIdeaService.getAllBySourceId(sourceId));
    }
}
