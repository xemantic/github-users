package com.xemantic.githubusers.logic.presenter;

import io.reactivex.Observable;
import io.reactivex.disposables.Disposable;
import io.reactivex.plugins.RxJavaPlugins;
import io.reactivex.subjects.PublishSubject;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static io.reactivex.Observable.range;
import static io.reactivex.Observable.timer;
import static io.reactivex.internal.functions.Functions.identity;
import static java.lang.Integer.MAX_VALUE;
import static java.lang.Math.min;
import static java.util.concurrent.TimeUnit.SECONDS;

public abstract class Presenter<V> {
  private final List<Disposable> attachSubscriptions = new ArrayList<>();
  private final List<Observable<?>> attachObservables = new ArrayList<>();
  private final PublishSubject<Map<String, Object>> request$ = PublishSubject.create();
  protected final V view;

  public Presenter(V view) {
    this.view = view;
  }

  public void register(Observable<?> o) {
    assert !attachObservables.contains(o) : "duplicate subscription, pretty sure this is a bug";
    attachObservables.add(o);
  }

  public Observable<Map<String, Object>> request() {
    return request$;
  }

  public void start() {
    for (Observable<?> o : attachObservables) subscribe(o, attachSubscriptions);
  }

  public void request(Map<String, Object> data) {
    request$.onNext(data);
  }

  public void stop() {
    for (Disposable s : attachSubscriptions) s.dispose();
    attachSubscriptions.clear();
  }

  private void subscribe(Observable<?> o, List<Disposable> to) {
    to.add(o.retryWhen(attempt -> attempt.zipWith(range(1, MAX_VALUE), (ex, cnt) -> {
      RxJavaPlugins.getErrorHandler().accept(ex); // report exception
      return timer(min(cnt * cnt, 100), SECONDS); // wait some time until retry
    }).flatMap(identity())).subscribe());
  }
}
