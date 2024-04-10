package com.infosys.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.infosys.model.Item;
import com.infosys.service.ItemService;
import com.infosys.util.AddItemToWalletRequest;
import com.infosys.util.ItemRequest;
import java.util.List;

@RestController
@RequestMapping("/items")
public class ItemController {

    @Autowired
    private ItemService itemService;
    
    @PostMapping("/purchase")
    public ResponseEntity<String> purchaseItem(@RequestParam Long userId, @RequestParam String itemName, @RequestParam int quantity) {
    	try {
    		ResponseEntity<String> response=itemService.purchaseItem(userId, itemName, quantity);
    		return response;
    	}
    	catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to add item: " + e.getMessage());
        }
    }

    @PostMapping("/sell")
    public ResponseEntity<String> sellItem(@RequestParam Long userId, @RequestParam String itemName, @RequestParam int quantity) {
    	try {
    	ResponseEntity<String> response=itemService.sellItem(userId, itemName, quantity);
    	return response;
    	}
    	catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to add item: " + e.getMessage());
        }
        
    }
    
    @PostMapping("/addToUserWallet")
    public ResponseEntity<String> addItemToUserWallet(@RequestBody AddItemToWalletRequest itemRequest) {
       try {
    	itemService.addItemToUserWallet(itemRequest);
       }
       catch (RuntimeException e) {
           return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                   .body("Failed to add item: " + e.getMessage());
       }
        return ResponseEntity.ok("Item added to user wallet successfully.");
    }

    // Endpoint to add items to the inventory
    @PostMapping("/addToInventory")
    public ResponseEntity<String> addItemToInventory(@RequestBody ItemRequest itemRequest) {
        
    	try {
        itemService.addItemToInventory(itemRequest);
    	}
    	catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to add item: " + e.getMessage());
        }

        return ResponseEntity.ok("Item added to inventory successfully.");
    }


    @GetMapping
    public ResponseEntity<List<Item>> getAllItems() {
        List<Item> items = itemService.getAllItems();
        return ResponseEntity.ok(items);
    }

    @GetMapping("/{itemName}")
    public ResponseEntity<Item> getItemByName(@PathVariable String itemName) {
        Item item = itemService.getItemFromName(itemName);
        if (item != null) {
            return ResponseEntity.ok(item);
        } else {
            return ResponseEntity.notFound().build();
        }
    }
    @PutMapping("/{itemId}")
    public ResponseEntity<String> updateItemInInventory(
            @PathVariable Long itemId,
            @RequestBody ItemRequest updateRequest) {

        try {
            itemService.updateItemInInventory(itemId, updateRequest);
            return ResponseEntity.ok("Item updated successfully");
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to update item: " + e.getMessage());
        }
    }

    @DeleteMapping("/id/{itemId}")
    public ResponseEntity<Void> deleteItem(@PathVariable Long itemId) {
        itemService.deleteItemFromInventory(itemId);
        return ResponseEntity.noContent().build();      
    }
    
    @DeleteMapping("/name/{itemName}")
    public ResponseEntity<Void> deleteItem(@PathVariable String itemName) {
        itemService.deleteItemFromInventoryByName(itemName);
        return ResponseEntity.noContent().build();      
    } 
}
