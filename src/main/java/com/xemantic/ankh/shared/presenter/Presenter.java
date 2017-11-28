/*
 * github-users - lists GitHub users. Minimal app demonstrating
 * cross-platform app development (Web, Android, iOS) where core
 * logic is shared and transpiled from Java to JavaScript and
 * Objective-C. This project delivers core application logic.
 *
 * Copyright (C) 2017  Kazimierz Pogoda
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.xemantic.ankh.shared.presenter;

import com.xemantic.ankh.shared.error.Errors;
import io.reactivex.Observable;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Action;
import io.reactivex.internal.functions.Functions;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

/**
 * Base presenter with shared presenter logic and state.
 *
 * @author morisil
 */
public abstract class Presenter {

  private final List<Observable<?>> observables = new LinkedList<>();

  private Action onStart = Functions.EMPTY_ACTION;

  private List<Disposable> subscriptions;

  protected Presenter(Observable<?> ... observables) {
    this.observables.addAll(Arrays.asList(observables));
  }

  protected void register(Observable<?> observable) {
    observables.add(observable);
  }

  protected void onStart(Action onStart) {
    this.onStart = onStart;
  }

  public void start() {
    subscriptions = Observable.fromIterable(observables)
        .map(observable ->
            observable
                .retry(throwable -> { // always retry, will cause unconditional resubscription
                  Errors.onError(throwable);
                  return true;
                })
                .subscribe()
        )
        .toList()
        .blockingGet();

    try {
      onStart.run();
    } catch (Exception e) {
      throw new RuntimeException("Could not start presenter", e);
    }
  }

  /**
   * Stops this presenter. This method is supposed to be called
   * when it is required to cancel all the pending subscriptions,
   * like ongoing HTTP requests, and unsubscribe from any {@link Observable}.
   * It might happen for example when user is navigating to another
   * {@code Activity} on Android platform.
   */
  public void stop() {
    for (Disposable subscription : subscriptions) {
      subscription.dispose();
    }
    subscriptions.clear();
  }

}
