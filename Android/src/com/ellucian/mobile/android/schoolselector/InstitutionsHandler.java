package com.ellucian.mobile.android.schoolselector;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

public class InstitutionsHandler extends DefaultHandler {

	private boolean currentElement;
	private String currentValue;
	private Institution institution;
	private final Institutions institutions = new Institutions();

	/**
	 * Called to get tag characters ( ex:- AndroidPeople
	 * 
	 * -- to get AndroidPeople Character )
	 */
	@Override
	public void characters(char[] ch, int start, int length)
			throws SAXException {
		if (currentElement) {
			currentValue += new String(ch, start, length);
		}
	}

	@Override
	public void endElement(String uri, String localName, String qName)
			throws SAXException {
		currentElement = false;
		if (localName.equalsIgnoreCase("configUrl")) {
			institution.setConfigUrl(currentValue);
		} else if (localName.equalsIgnoreCase("fullName")) {
			institution.setFullName(currentValue);
		} else if (localName.equalsIgnoreCase("displayName")) {
			institution.setDisplayName(currentValue);
		} else if (localName.equalsIgnoreCase("keyword")) {
			institution.addKeyword(currentValue);
		}
	}

	public Institutions getInstitutions() {
		return institutions;
	}

	@Override
	public void startElement(String uri, String localName, String qName,
			Attributes attributes) throws SAXException {
		currentElement = true;
		if (localName.equals("institution")) {
			institution = new Institution();
			institutions.add(institution);
		}
		currentValue = "";
	}
}