package com.khsm.app.presentation.ui.screens.meetings;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import com.khsm.app.R;
import com.khsm.app.data.entities.DisciplineResults;
import com.khsm.app.data.entities.Meeting;
import com.khsm.app.domain.MeetingsManager;
import com.khsm.app.domain.ResultsManager;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

public class MeetingResultsFragment extends Fragment {
    private static final String KET_MEETING = "KET_MEETING";

    private static final SimpleDateFormat dateFormat = new SimpleDateFormat("dd MMMM yyyy", Locale.getDefault());

    private ProgressBar progressBar;

    @Nullable
    private Disposable loadDisposable;
    private ResultsManager resultsManager;


    public static MeetingResultsFragment newInstance(Meeting meeting) {
        MeetingResultsFragment fragment = new MeetingResultsFragment();

        Bundle arguments = new Bundle();
        arguments.putSerializable(KET_MEETING, meeting);

        fragment.setArguments(arguments);

        return fragment;
    }

    private Meeting meeting;

    @SuppressWarnings({"FieldCanBeLocal", "unused"})
    private Toolbar toolbar;

    private TabLayout tabLayout;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Bundle arguments = getArguments();

        meeting = (Meeting) arguments.getSerializable(KET_MEETING);

        resultsManager = new ResultsManager();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.meeting_results_fragment, container, false);

        toolbar = view.findViewById(R.id.toolbar);

        toolbar.setTitle(dateFormat.format(meeting.date));

        tabLayout = view.findViewById(R.id.tabLayout);


       /* tabLayout.addTab(tabLayout.newTab().setText("2 x 2"));
        tabLayout.addTab(tabLayout.newTab().setText("3 x 3"));
        tabLayout.addTab(tabLayout.newTab().setText("5 x 5"));
        tabLayout.addTab(tabLayout.newTab().setText("7 x 7"));*/

        progressBar = view.findViewById(R.id.progressBar);
        progressBar.setVisibility(View.INVISIBLE);

        loadResults();

        return view;
    }

    private void loadResults() {
        progressBar.setVisibility(View.VISIBLE);

        if (loadDisposable != null) {
            loadDisposable.dispose();
            loadDisposable = null;
        }

        loadDisposable = resultsManager.getMeetingResults(meeting.id)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        this::setResults,
                        this::handleError
                );
    }

    private void setResults(List<DisciplineResults> disciplineResults) {
        progressBar.setVisibility(View.INVISIBLE);

        tabLayout.removeAllTabs();

        for (DisciplineResults disciplineResult : disciplineResults) {
            tabLayout.addTab(tabLayout.newTab().setText(disciplineResult.discipline.name).setTag(disciplineResult));
        }

        //adapter.setMeetings(meetings);
    }

    private void handleError(Throwable throwable) {
        progressBar.setVisibility(View.INVISIBLE);

        new AlertDialog.Builder(getContext())
                .setTitle(R.string.Error)
                .setMessage(throwable.getMessage())
                .setPositiveButton(R.string.OK, null)
                .show();
    }
}
