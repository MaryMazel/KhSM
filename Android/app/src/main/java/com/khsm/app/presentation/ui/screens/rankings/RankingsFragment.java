package com.khsm.app.presentation.ui.screens.rankings;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import com.khsm.app.R;
import com.khsm.app.data.api.entities.RankingsFilterInfo;
import com.khsm.app.data.entities.DisciplineResults;
import com.khsm.app.domain.RankingsManager;
import com.khsm.app.presentation.ui.adapters.ResultsAdapter;
import com.khsm.app.presentation.ui.screens.MainActivity;

import java.util.List;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

public class RankingsFragment extends Fragment implements Toolbar.OnMenuItemClickListener {
    public static RankingsFragment newInstance() { return new RankingsFragment(); }

    @SuppressWarnings("FieldCanBeLocal")
    private RecyclerView recyclerView;

    private ProgressBar progressBar;

    @Nullable
    private Disposable loadDisposable;

    private RankingsManager rankingsManager;

    @SuppressWarnings({"FieldCanBeLocal", "unused"})
    private Toolbar toolbar;

    private TabLayout tabLayout;

    private ResultsAdapter adapter;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Context context = requireContext();

        rankingsManager = new RankingsManager(context);
        adapter = new ResultsAdapter(context, ResultsAdapter.DisplayMode.UserAndDate);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             Bundle savedInstanceState) {
        // init view
        View view = inflater.inflate(R.layout.rankings_fragment, container, false);

        toolbar = view.findViewById(R.id.toolbar);
        toolbar.inflateMenu(R.menu.rankings);
        toolbar.setOnMenuItemClickListener(this);

        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MainActivity mainActivity = (MainActivity) requireActivity();
                mainActivity.showMenu();
            }
        });

        tabLayout = view.findViewById(R.id.tabLayout);
        tabLayout.setVisibility(View.INVISIBLE);
        tabLayout.addOnTabSelectedListener(onTabSelectedListener);

        recyclerView = view.findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(adapter);

        progressBar = view.findViewById(R.id.progressBar);
        progressBar.setVisibility(View.INVISIBLE);

        // load data
        loadRankings(null);

        return view;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        cancelLoadOperation();
    }

    private final TabLayout.OnTabSelectedListener onTabSelectedListener = new TabLayout.OnTabSelectedListener() {
        @Override
        public void onTabSelected(TabLayout.Tab tab) {
            DisciplineResults disciplineResults = (DisciplineResults) tab.getTag();
            if (disciplineResults != null) {
                setDisciplineResults(disciplineResults);
            }
        }

        @Override
        public void onTabUnselected(TabLayout.Tab tab) {
        }

        @Override
        public void onTabReselected(TabLayout.Tab tab) {
        }
    };

    private void loadRankings(@Nullable RankingsFilterInfo rankingsFilterInfo) {
        if (rankingsFilterInfo == null)
            rankingsFilterInfo = new RankingsFilterInfo(RankingsFilterInfo.FilterType.Average, RankingsFilterInfo.SortType.Ascending, null);

        progressBar.setVisibility(View.VISIBLE);
        tabLayout.setVisibility(View.INVISIBLE);

        cancelLoadOperation();
        loadDisposable = rankingsManager.getRankings(rankingsFilterInfo)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        this::setDisciplineResults,
                        this::handleError
                );
    }

    private void cancelLoadOperation() {
        if (loadDisposable == null)
            return;

        loadDisposable.dispose();
        loadDisposable = null;
    }

    private void setDisciplineResults(List<DisciplineResults> disciplineResults) {
        progressBar.setVisibility(View.INVISIBLE);

        tabLayout.removeAllTabs();

        for (DisciplineResults disciplineResult : disciplineResults) {
            tabLayout.addTab(tabLayout.newTab().setText(disciplineResult.discipline.name).setTag(disciplineResult));
        }

        tabLayout.setVisibility(!disciplineResults.isEmpty() ? View.VISIBLE : View.INVISIBLE);
    }

    private void handleError(Throwable throwable) {
        progressBar.setVisibility(View.INVISIBLE);

        new AlertDialog.Builder(requireContext())
                .setTitle(R.string.Error)
                .setMessage(throwable.getMessage())
                .setPositiveButton(R.string.OK, null)
                .show();
    }

    private void setDisciplineResults(@NonNull DisciplineResults disciplineResults) {
        adapter.setResults(disciplineResults.results);
    }

    @Override
    public boolean onMenuItemClick(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.filter:
                FilterDialogFragment filterDialogFragment = FilterDialogFragment.newInstance();
                filterDialogFragment.show(getChildFragmentManager(), null);
                return true;
        }


        return false;
    }

    public void applyFilter(RankingsFilterInfo rankingsFilterInfo) {
        loadRankings(rankingsFilterInfo);
    }
}
