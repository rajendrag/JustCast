package com.rp.justcast;

public class JustCastMenuItem {
	
	private String title;

	private int icon;
	
	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public int getIcon() {
		return icon;
	}

	public void setIcon(int icon) {
		this.icon = icon;
	}

	
	public JustCastMenuItem(String title, int icon) {
		this.title = title;
		this.icon = icon;
	}
	

}
