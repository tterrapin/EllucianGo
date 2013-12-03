package com.ellucian.mobile.android.directory;

import android.content.SearchRecentSuggestionsProvider;

public class DirectorySearchSuggestionProvider extends SearchRecentSuggestionsProvider {
    public final static String AUTHORITY = "com.ellucian.mobile.android.directory.DirectorySearchSuggestionProvider";
    public final static int MODE = DATABASE_MODE_QUERIES;

    public DirectorySearchSuggestionProvider() {
        setupSuggestions(AUTHORITY, MODE);
    }
}