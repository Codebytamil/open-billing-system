package com.billing.billing_system.bill;

import jakarta.validation.Valid;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/bills")
public class BillController {

    private final BillService service;
    private final BillPdfService pdfService;

    public BillController(BillService service, BillPdfService pdfService) {
        this.service = service;
        this.pdfService = pdfService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Bill create(@Valid @RequestBody BillRequest request) {
        return service.create(request);
    }

    @GetMapping
    public List<Bill> getAll() {
        return service.getAll();
    }

    @GetMapping("/{id}")
    public Bill getById(@PathVariable Long id) {
        return service.getById(id);
    }

    @GetMapping("/{id}/pdf")
    public ResponseEntity<byte[]> pdf(@PathVariable Long id) {
        byte[] pdf = pdfService.generate(id);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=bill-" + id + ".pdf")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdf);
    }
}