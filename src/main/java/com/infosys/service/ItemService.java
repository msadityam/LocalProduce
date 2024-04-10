package com.infosys.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import com.infosys.model.Item;
import com.infosys.model.User;
import com.infosys.repository.ItemRepository;
import com.infosys.repository.UserRepository;
import com.infosys.util.AddItemToWalletRequest;
import com.infosys.util.ItemRequest;
import jakarta.transaction.Transactional;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class ItemService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ItemRepository itemRepository;

    public ResponseEntity<String> purchaseItem(Long userId, String itemName, int quantity) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Item item = itemRepository.findByName(itemName)
                .orElseThrow(() -> new RuntimeException("Item not found"));

        double totalCost = item.getPrice() * quantity;

        if (user.getCoins() < totalCost) {
        	String errorMessage = "Insufficient coins to make the purchase '" + itemName + "' in user's wallet. required coins: '"+totalCost+"' user has: '"+user.getCoins() ;
	        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorMessage);
           
        }

        // Decrease user's coins and add item to user's wallet
        user.setCoins(user.getCoins() - totalCost);
        user.addToWallet(itemName, quantity); // Store item name in wallet

        // Update item inventory by reducing the quantity
        item.setQuantity(item.getQuantity() - quantity);
        itemRepository.save(item);

        userRepository.save(user);
        
        String successMessage = "Successfully purchased " + quantity + " units of item '" + itemName + "' ";
	    return ResponseEntity.ok(successMessage);
    }

    public ResponseEntity<String> sellItem(Long userId, String itemName, int quantity) {
    	 User user = userRepository.findById(userId)
    			 .orElseThrow(() -> {
 	                String errorMessage = "User with id '" + userId + "' not found";
 	                return new RuntimeException(errorMessage);
 	            });

    	 Item item = itemRepository.findByName(itemName)
    	            .orElseThrow(() -> {
    	                String errorMessage = "Item '" + itemName + "' not found";
    	                return new RuntimeException(errorMessage);
    	            });
    	 
    	    if (!user.getWalletItems().containsKey(itemName) || user.getWalletItems().get(itemName) < quantity) {
    	        String errorMessage = "Insufficient quantity of item '" + itemName + "' in user's wallet";
    	        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorMessage);
    	    }

    	    // Proceed with the sale process
    	    user.removeFromWallet(itemName, quantity); // Store item name in wallet
    	    double totalPriceEarned = item.getPrice() * quantity;
    	    user.setCoins(user.getCoins() + totalPriceEarned);

    	    // Update item inventory by increasing the quantity
    	    item.setQuantity(item.getQuantity() + quantity);
    	    itemRepository.save(item);

    	    userRepository.save(user);

    	    String successMessage = "Successfully sold " + quantity + " units of item '" + itemName + "' to inventory";
    	    return ResponseEntity.ok(successMessage);
    }

    
    public void addItemToInventory(ItemRequest itemRequest) {
      String itemName = itemRequest.getName();
      double itemPrice = itemRequest.getPrice();
      int quantity = itemRequest.getQuantity();

      // Retrieve the item from the database by name
      Optional<Item> existingItem = itemRepository.findByName(itemName);

      Item inventoryItem;
      if (existingItem.isPresent()) {
          // Item exists in the database, use its ID
          inventoryItem = existingItem.get();
      } else {
          // Item does not exist in the database, create a new item
          inventoryItem = new Item();
          inventoryItem.setName(itemName);
      }

      // Update the item details
      inventoryItem.setPrice(itemPrice);
      inventoryItem.setQuantity(inventoryItem.getQuantity() + quantity);

      // Save the updated inventory item (this will either update existing or save new)
      itemRepository.save(inventoryItem);
  }
  
    public void addItemToUserWallet(AddItemToWalletRequest itemRequest) {
    	String itemName = itemRequest.getItemName();
        long userId = itemRequest.getUserId();
        int quantity = itemRequest.getQuantity();
    	User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Retrieve the item from the database or save it if it's new
        Item item = itemRepository.findByName(itemName)
                .orElseGet(() -> {
                    Item newItem = new Item();
                    newItem.setName(itemName);
                    newItem.setPrice(50.0);
                    newItem.setQuantity(0);
                    return itemRepository.save(newItem);
                });

        // Add the item to the user's wallet
        user.addToWallet(item.getName(), quantity); // Store item ID and quantity in wallet

        // Save the updated user entity
        userRepository.save(user);
  }
    	
	   public List<Item> getAllItems() {
	    List<Item> items = itemRepository.findAll();
	    return items;
	    }
	
	    public Item getItemFromName(String itemName) {
	        Optional<Item> itemOptional = itemRepository.findByName(itemName);
	        return itemOptional.orElse(null);
	    }
	
	public void deleteItemFromInventory(Long itemId) {
        // Retrieve the item from the database
        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new RuntimeException("Item not found"));

         itemRepository.delete(item);
    }
	
	public void deleteItemFromInventoryByName(String name) {
        // Retrieve the item from the database
        Item item = itemRepository.findByName(name)
                .orElseThrow(() -> new RuntimeException("Item not found"));

        // Delete the item from the database
        itemRepository.delete(item);
    }
	
	public void updateItemInInventory(Long itemId,ItemRequest request ) {
        // Retrieve the item from the database
		String newName=request.getName();
		double newPrice=request.getPrice();
		int newQuantity=request.getQuantity();
        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new RuntimeException("Item not found"));

        // Update the item properties
        item.setName(newName);
        item.setPrice(newPrice);
        item.setQuantity(newQuantity);

        // Save the updated item to the database
        itemRepository.save(item);
    }
}


