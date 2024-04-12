package com.infosys.model;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.MapKeyColumn;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class User implements Serializable {
	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private Long id;

	@Email(message = "Email must be valid")
	private String email;

	private String name;
	private String phoneNumber;
	private String role;
	private String confirmationCode;
	private boolean confirmed;

	@Size(min = 6, message = "Password must be at least 6 characters long")
	private String password;

	private double coins = 500;

	// Map to store items in the user's wallet (item name -> quantity)
	@ElementCollection
	@CollectionTable(name = "user_wallet", joinColumns = @JoinColumn(name = "user_id"))
	@MapKeyColumn(name = "item_name")
	@Column(name = "quantity")
	private Map<String, Integer> walletItems = new HashMap<>();

	public void addToWallet(String itemName, int quantity) {
		if (walletItems.containsKey(itemName)) {
			int existingQuantity = walletItems.get(itemName);
			walletItems.put(itemName, existingQuantity + quantity);
		} else {
			walletItems.put(itemName, quantity);
		}
	}

	public void removeFromWallet(String itemName, int quantity) {
		if (walletItems.containsKey(itemName)) {
			int existingQuantity = walletItems.get(itemName);
			int remainingQuantity = existingQuantity - quantity;
			if (remainingQuantity <= 0) {
				walletItems.remove(itemName);
			} else {
				walletItems.put(itemName, remainingQuantity);
			}
		}
	}
}
