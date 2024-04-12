package com.infosys.util;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserDTO {

	private Long id;
	private String name;
	private String email;
	private String phoneNumber;

	// Constructor, getters, and setters...
}
