package fr.lab.iahomelab.web.controller;

import fr.lab.iahomelab.common.exception.ResourceNotFoundException;
import fr.lab.iahomelab.setup.controller.dto.CreateSetupRequest;
import fr.lab.iahomelab.setup.controller.dto.CreateSetupVersionRequest;
import fr.lab.iahomelab.setup.controller.dto.SetupResponse;
import fr.lab.iahomelab.setup.controller.dto.SetupVersionResponse;
import fr.lab.iahomelab.setup.controller.dto.UpdateSetupRequest;
import fr.lab.iahomelab.setup.controller.dto.UpdateSetupVersionRequest;
import fr.lab.iahomelab.setup.service.ComponentInstanceService;
import fr.lab.iahomelab.setup.service.ConnectionService;
import fr.lab.iahomelab.setup.service.SetupService;
import fr.lab.iahomelab.setup.service.SetupVersionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.UUID;

@Controller
@RequestMapping("/app/setups")
@RequiredArgsConstructor
public class SetupPageController {

    private final SetupService setupService;
    private final SetupVersionService setupVersionService;
    private final ComponentInstanceService componentInstanceService;
    private final ConnectionService connectionService;

    @GetMapping
    public String list(Model model) {
        model.addAttribute("setups", setupService.list());
        return "setup/list";
    }

    @GetMapping("/new")
    public String createForm() {
        return "setup/new";
    }

    @PostMapping
    public String create(
            @RequestParam String name,
            @RequestParam(required = false) String description
    ) {
        SetupResponse setup = setupService.create(
                new CreateSetupRequest(name, normalize(description))
        );

        return "redirect:/app/setups/" + setup.id();
    }

    @GetMapping("/{setupId}")
    public String detail(
            @PathVariable UUID setupId,
            Model model
    ) {
        model.addAttribute("setup", setupService.getById(setupId));
        model.addAttribute("versions", setupVersionService.list(setupId));
        return "setup/detail";
    }

    @GetMapping("/{setupId}/edit")
    public String editForm(
            @PathVariable UUID setupId,
            Model model
    ) {
        model.addAttribute("setup", setupService.getById(setupId));
        return "setup/edit";
    }

    @PostMapping("/{setupId}/edit")
    public String update(
            @PathVariable UUID setupId,
            @RequestParam String name,
            @RequestParam(required = false) String description
    ) {
        setupService.update(
                setupId,
                new UpdateSetupRequest(name, normalize(description))
        );

        return "redirect:/app/setups/" + setupId;
    }

    @PostMapping("/{setupId}/delete")
    public String delete(@PathVariable UUID setupId) {
        setupService.delete(setupId);
        return "redirect:/app/setups";
    }

    @PostMapping("/{setupId}/versions")
    public String createVersion(
            @PathVariable UUID setupId,
            @RequestParam(required = false) String description
    ) {
        SetupVersionResponse version = setupVersionService.create(
                setupId,
                new CreateSetupVersionRequest(normalize(description))
        );

        return "redirect:/app/setups/" + setupId + "/versions/" + version.id();
    }

    @PostMapping("/{setupId}/versions/{versionId}/update")
    public String updateVersion(
            @PathVariable UUID setupId,
            @PathVariable UUID versionId,
            @RequestParam(required = false) String description
    ) {
        findVersionForSetup(setupId, versionId);
        setupVersionService.update(
                versionId,
                new UpdateSetupVersionRequest(normalize(description))
        );

        return "redirect:/app/setups/" + setupId + "/versions/" + versionId;
    }

    @PostMapping("/{setupId}/versions/{versionId}/freeze")
    public String freezeVersion(
            @PathVariable UUID setupId,
            @PathVariable UUID versionId
    ) {
        findVersionForSetup(setupId, versionId);
        setupVersionService.freeze(versionId);

        return "redirect:/app/setups/" + setupId + "/versions/" + versionId;
    }

    @PostMapping("/{setupId}/versions/{versionId}/delete")
    public String deleteVersion(
            @PathVariable UUID setupId,
            @PathVariable UUID versionId
    ) {
        findVersionForSetup(setupId, versionId);
        setupVersionService.delete(versionId);

        return "redirect:/app/setups/" + setupId;
    }

    @GetMapping("/{setupId}/versions/{versionId}")
    public String workspace(
            @PathVariable UUID setupId,
            @PathVariable UUID versionId,
            Model model
    ) {
        SetupResponse setup = setupService.getById(setupId);
        SetupVersionResponse version = findVersionForSetup(setupId, versionId);

        model.addAttribute("setup", setup);
        model.addAttribute("version", version);
        model.addAttribute("components", componentInstanceService.list(versionId));
        model.addAttribute("connections", connectionService.list(versionId));

        return "setup/workspace";
    }

    private SetupVersionResponse findVersionForSetup(UUID setupId, UUID versionId) {
        SetupVersionResponse version = setupVersionService.getById(versionId);

        if (!version.setupId().equals(setupId)) {
            throw new ResourceNotFoundException(
                    "Setup version " + versionId + " does not belong to setup " + setupId
            );
        }

        return version;
    }

    private String normalize(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
