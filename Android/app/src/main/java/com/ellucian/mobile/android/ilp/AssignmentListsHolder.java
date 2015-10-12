/*
 * Copyright 2015 Ellucian Company L.P. and its affiliates.
 */

package com.ellucian.mobile.android.ilp;

import android.content.Context;
import android.database.Cursor;
import android.text.TextUtils;

import com.ellucian.elluciango.R;
import com.ellucian.mobile.android.provider.EllucianContract;
import com.ellucian.mobile.android.util.CalendarUtils;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;

public class AssignmentListsHolder {
    private final List<AssignmentItemHolder> assignmentsToday;
    private final List<AssignmentItemHolder> assignmentsNoDate;
    private final List<AssignmentItemHolder> assignmentsOverdue;
    private final List<AssignmentItemHolder> widgetAssignments;

    public AssignmentListsHolder(Context context, Cursor cursor) {
        assignmentsToday = new ArrayList<AssignmentItemHolder>();
        assignmentsNoDate = new ArrayList<AssignmentItemHolder>();
        assignmentsOverdue = new ArrayList<AssignmentItemHolder>();
        widgetAssignments = new ArrayList<AssignmentItemHolder>();

        Calendar now = (Calendar)Calendar.getInstance().clone();
        Calendar tomorrow = (Calendar)now.clone();
        // 24:00:000 'belongs' to the next day
        tomorrow.set(Calendar.HOUR_OF_DAY, 24);
        tomorrow.set(Calendar.MINUTE, 0);
        tomorrow.set(Calendar.SECOND, 0);
        Calendar yesterday = (Calendar)tomorrow.clone();
        yesterday.roll(Calendar.DAY_OF_YEAR, -1);

        if (cursor.moveToFirst()) {
            do {

                String sectionId = cursor.getString(cursor.getColumnIndex(EllucianContract.CourseCourses.COURSE_ID));
                String sectionName = cursor.getString(cursor.getColumnIndex(EllucianContract.CourseAssignments.ASSIGNMENT_SECTION_NAME));
                String title = cursor.getString(cursor.getColumnIndex(EllucianContract.CourseAssignments.ASSIGNMENT_NAME));
                String dateString = cursor.getString(cursor.getColumnIndex(EllucianContract.CourseAssignments.ASSIGNMENT_DUE));
                String content = cursor.getString(cursor.getColumnIndex(EllucianContract.CourseAssignments.ASSIGNMENT_DESCRIPTION));
                String url = cursor.getString(cursor.getColumnIndex(EllucianContract.CourseAssignments.ASSIGNMENT_URL));

                if (!TextUtils.isEmpty(dateString)) {

                    Date assignmentDate = CalendarUtils.parseFromUTC(dateString);
                    String displayDate = "";
                    displayDate = CalendarUtils.getDefaultDateTimeString(context, assignmentDate);

                    Calendar assignmentDue = (Calendar)Calendar.getInstance().clone();
                    assignmentDue.setTime(assignmentDate);

                    AssignmentItemHolder infoHolder = new AssignmentItemHolder(sectionId, sectionName,
                            title, dateString, displayDate, content, url);
                    if (assignmentDue.before(now)) {
                        // overdue
                        assignmentsOverdue.add(infoHolder);
                    } else if (assignmentDue.before(tomorrow)) {
                        // today
                        assignmentsToday.add(infoHolder);
                    }

                    // widget gets everything from today, regardless of overdue or nto
                    if (assignmentDue.after(yesterday) && assignmentDue.before(tomorrow)) {
                        AssignmentItemHolder widgetInfoHolder = new AssignmentItemHolder(sectionId, sectionName,
                                title, dateString, displayDate, content, url);
                        widgetAssignments.add(widgetInfoHolder);
                    }
                } else {
                    String displayDate = context.getString(R.string.course_assignments_none_assigned);

                    AssignmentItemHolder infoHolder = new AssignmentItemHolder(sectionId, sectionName,
                            title, dateString, displayDate, content, url);
                    assignmentsNoDate.add(infoHolder);
                }

            } while (cursor.moveToNext());

            Collections.sort(assignmentsToday);
            Collections.sort(assignmentsOverdue);
            Collections.sort(widgetAssignments);
        }

    }

    public List<AssignmentItemHolder> getAssignmentsToday() {
        return assignmentsToday;
    }

    @SuppressWarnings("unused")
    public List<AssignmentItemHolder> getAssignmentsNoDate(){
        return assignmentsNoDate;
    }

    public List<AssignmentItemHolder> getAssignmentsOverdue(){
        return assignmentsOverdue;
    }

    public List<AssignmentItemHolder> getWidgetAssignments() {
        return widgetAssignments;
    }
}
