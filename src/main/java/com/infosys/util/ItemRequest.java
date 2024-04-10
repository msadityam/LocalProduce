package com.infosys.util;

import com.infosys.model.Item;
import com.infosys.model.User;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ItemRequest {
    private String name;
    private double price;
    private int quantity;

}
