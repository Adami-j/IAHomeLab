package fr.lab.iahomelab.web.controller;

import fr.lab.iahomelab.source.controller.dto.CreateSourceRequest;
import fr.lab.iahomelab.source.controller.dto.SourceResponse;
import fr.lab.iahomelab.source.controller.dto.UpdateSourceRequest;
import fr.lab.iahomelab.source.entity.SourceStatus;
import fr.lab.iahomelab.source.entity.SourceType;
import fr.lab.iahomelab.source.service.SourceService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/app/research")
@RequiredArgsConstructor
public class SourcePageController {

    private final SourceService sourceService;

    @GetMapping
    public String list(Model model) {
        model.addAttribute("sources", sourceService.list());
        return "source/list";
    }

    @GetMapping("/new")
    public String createForm(Model model) {
        populateFormOptions(model);
        model.addAttribute("editing", false);
        return "source/form";
    }

    @PostMapping
    public String create(
            @RequestParam String title,
            @RequestParam(required = false) String url,
            @RequestParam(required = false) String storagePath,
            @RequestParam(required = false) String fileName,
            @RequestParam(required = false) String mimeType,
            @RequestParam SourceType type,
            @RequestParam(required = false) SourceStatus status,
            @RequestParam(required = false) String summary,
            @RequestParam(required = false) String notes,
            @RequestParam(required = false) String tags
    ) {
        sourceService.create(new CreateSourceRequest(
                title.trim(),
                normalize(url),
                normalize(storagePath),
                normalize(fileName),
                normalize(mimeType),
                type,
                status,
                normalize(summary),
                normalize(notes),
                parseTags(tags)
        ));

        return "redirect:/app/research";
    }

    @GetMapping("/{sourceId}/edit")
    public String editForm(
            @PathVariable UUID sourceId,
            Model model
    ) {
        SourceResponse source = sourceService.getById(sourceId);
        model.addAttribute("source", source);
        model.addAttribute("tagsValue", String.join(", ", source.tags()));
        model.addAttribute("editing", true);
        populateFormOptions(model);
        return "source/form";
    }

    @PostMapping("/{sourceId}/edit")
    public String update(
            @PathVariable UUID sourceId,
            @RequestParam String title,
            @RequestParam(required = false) String url,
            @RequestParam(required = false) String storagePath,
            @RequestParam(required = false) String fileName,
            @RequestParam(required = false) String mimeType,
            @RequestParam SourceType type,
            @RequestParam(required = false) SourceStatus status,
            @RequestParam(required = false) String summary,
            @RequestParam(required = false) String notes,
            @RequestParam(required = false) String tags
    ) {
        sourceService.update(sourceId, new UpdateSourceRequest(
                title.trim(),
                normalize(url),
                normalize(storagePath),
                normalize(fileName),
                normalize(mimeType),
                type,
                status,
                normalize(summary),
                normalize(notes),
                parseTags(tags)
        ));

        return "redirect:/app/research";
    }

    @PostMapping("/{sourceId}/delete")
    public String delete(@PathVariable UUID sourceId) {
        sourceService.delete(sourceId);
        return "redirect:/app/research";
    }

    private void populateFormOptions(Model model) {
        model.addAttribute("sourceTypes", SourceType.values());
        model.addAttribute("sourceStatuses", SourceStatus.values());
    }

    private String normalize(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }

    private Set<String> parseTags(String value) {
        if (value == null || value.isBlank()) {
            return Set.of();
        }

        return Arrays.stream(value.split(","))
                .map(String::trim)
                .filter(tag -> !tag.isBlank())
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }
}
