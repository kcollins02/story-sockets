/**
 * 
 */
package com.jackson;

import java.io.Serializable;

import javax.faces.bean.*;

/**
 * @author Kevin
 *
 */

	
@ManagedBean(name = "UserBean")
@SessionScoped
public class UserBean implements Serializable {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1997606313911361398L;
	private String name;
	
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public UserBean() {
	}
}
