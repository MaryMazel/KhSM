package com.khsm.app.data.api.base;

import io.reactivex.Completable;
import io.reactivex.Single;

public class ApiBase {
    protected ApiBase() {
    }

    protected Completable processResponse(Completable upstream) {
        return upstream; // TODO: 14.02.2018 add generic error handling
    }

    protected <TResponse> Single<TResponse> processResponse(Single<TResponse> upstream) {
        return upstream; // TODO: 14.02.2018 add generic error handling
    }
}