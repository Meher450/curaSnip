package com.curapaste.controllers;


import com.curapaste.dto.*;
import com.curapaste.entities.Paste;
import com.curapaste.services.PasteService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/pastes")
public class PasteController {

    private final PasteService pasteService;


    public PasteController(PasteService service) {
        this.pasteService = service;
    }

    @PostMapping
    public ResponseEntity<CreatePasteResponse> create(@Valid @RequestBody CreatePasteRequest requestBody) {
        return ResponseEntity.status(HttpStatus.CREATED).body(
                pasteService.createPaste(requestBody)
        );
    }

    @GetMapping("/{shortId}")
    public ResponseEntity<PasteResponse> getPaste(@PathVariable String shortId,
                                                  @RequestParam(required = false) String password) {
        return ResponseEntity.ok(
                pasteService.getPaste(shortId,password)
        );
    }

    @GetMapping("{shortId}/metadata")
    public ResponseEntity<PasteMetadataResponse> getPasteMetadata(@PathVariable String shortId) {
        return ResponseEntity.ok(
                pasteService.getPasteMetadata(shortId)
        );
    }

    @DeleteMapping("/{shortId}")
    public ResponseEntity<Map<String,String>> deletePaste(@PathVariable String shortId
    , @Valid @RequestBody DeletePasteRequest request) {
        pasteService.deletePaste(
                shortId,
                request.getDeleteToken()
        );

        return ResponseEntity.status(HttpStatus.OK).body(
                Map.of("message", "Paste has been deleted successfully")
        );
    }
}
