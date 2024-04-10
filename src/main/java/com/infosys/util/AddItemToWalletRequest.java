package com.infosys.util;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AddItemToWalletRequest {

    private Long userId;
    private String itemName;
    private int quantity;
}