
package com.example.helloworld;

public class ChannelItem {

	String name = "";
	boolean selected = false;
	
	public ChannelItem (String name, boolean selected) {
		this.name = name;
		this.selected = selected;
	}
	
	public ChannelItem () {
		this.name = "";
		this.selected = false;
	}
	
	public String getName () {
		return name;
	}
	
	public ChannelItem setName (String name) {
		this.name = name;
		return this;
	}
	
	public boolean getSelected () {
		return selected;
	}
	
	public ChannelItem setSelected (boolean selected) {
		this.selected = selected;
		return this;
	}
	
}
