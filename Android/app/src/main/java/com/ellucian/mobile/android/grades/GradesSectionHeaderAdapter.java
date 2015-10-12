/*
 * Copyright 2015 Ellucian Company L.P. and its affiliates.
 */

package com.ellucian.mobile.android.grades;

import android.content.Context;

import com.ellucian.elluciango.R;
import com.ellucian.mobile.android.adapter.TwoLineArrayAdapter;

public class GradesSectionHeaderAdapter extends TwoLineArrayAdapter<GradesSectionHeader> {

    public GradesSectionHeaderAdapter(Context context, GradesSectionHeader[] gradesSectionHeaders) {
        super(context, R.layout.grades_section_header,
                R.id.course_label, R.id.course_title, gradesSectionHeaders);
    }

    @Override
    public String lineOneText(GradesSectionHeader gradesSectionHeader) {
        return gradesSectionHeader.label;
    }

    @Override
    public String lineTwoText(GradesSectionHeader gradesSectionHeader) {
        return gradesSectionHeader.title;
    }
}
