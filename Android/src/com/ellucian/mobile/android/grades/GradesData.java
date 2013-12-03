package com.ellucian.mobile.android.grades;

import java.util.ArrayList;
import java.util.List;

public class GradesData {
	private String gradesAsOf;
	private List<Term> terms = new ArrayList<Term>();

	public String getGradesAsOf() {
		return gradesAsOf != null ? gradesAsOf : "";
	}

	public List<Term> getTerms() {
		return terms;
	}

	public void setGradesAsOf(String gradesAsOf) {
		this.gradesAsOf = gradesAsOf;
	}

	public void setTerms(List<Term> terms) {
		this.terms = terms;
	}
}
