package com.ellucian.mobile.android.notifications;

import java.util.Calendar;

public class ColleagueNotification {

	private String description;
	private String descriptionDetails;
	private String hyperlink;
	private String linkLabel;
	private int restriction;
	private Calendar startDate;

	public void setDescription(String description) {
		this.description = description;
	}

	public void setDescriptionDetails(String descriptionDetails) {
		this.descriptionDetails = descriptionDetails;
	}

	public String getDescription() {
		return description;
	}

	public String getDescriptionDetails() {
		return descriptionDetails;
	}

	public String getHyperlink() {
		return hyperlink;
	}

	public String getLinkLabel() {
		return linkLabel;
	}

	public int getRestriction() {
		return restriction;
	}

	public Calendar getStartDate() {
		return startDate;
	}

	public void setHyperlink(String hyperlink) {
		this.hyperlink = hyperlink;
	}

	public void setLinkLabel(String linkLabel) {
		this.linkLabel = linkLabel;
	}

	public void setRestriction(int restriction) {
		this.restriction = restriction;
	}

	public void setStartDate(Calendar startDate) {
		this.startDate = startDate;
	}

	 @Override 
	 public boolean equals(Object aThat) {
		    //check for self-comparison
		    if ( this == aThat ) return true;

		    //use instanceof instead of getClass here for two reasons
		    //1. if need be, it can match any supertype, and not just one class;
		    //2. it renders an explict check for "that == null" redundant, since
		    //it does the check for null already - "null instanceof [type]" always
		    //returns false. (See Effective Java by Joshua Bloch.)
		    if ( !(aThat instanceof ColleagueNotification) ) return false;
		    //Alternative to the above line :
		    //if ( aThat == null || aThat.getClass() != this.getClass() ) return false;

		    //cast to native object is now safe
		    ColleagueNotification that = (ColleagueNotification)aThat;

		    //now a proper field-by-field evaluation can be made
		    return
		      this.description.equals(that.description) &&
		      this.descriptionDetails.equals(that.descriptionDetails) &&
		      this.restriction == that.restriction;
		  }
	
}
