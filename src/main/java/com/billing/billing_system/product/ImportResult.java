package com.billing.billing_system.product;

import java.util.List;

public record ImportResult(int imported, int skipped, List<String> errors) {
}