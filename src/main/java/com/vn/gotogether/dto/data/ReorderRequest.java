package com.vn.gotogether.dto.data;


import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record ReorderRequest(
        @NotNull @Min(1) Integer dayNumber,
        @NotEmpty List<String> itemIdsInOrder
) {}