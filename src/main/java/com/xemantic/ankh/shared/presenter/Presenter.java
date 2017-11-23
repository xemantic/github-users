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
import io.reactivex.functions.Consumer;

import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

/**
 * Base presenter with shared presenter logic and state.
 *
 * @author morisil
 */
public abstract class Presenter {

  private final List<Disposable> disposables = new LinkedList<>();

  protected <T> CallDefiner<T> on(Observable<T> observable) {
    Objects.requireNonNull(observable);
    return consumer ->
        disposables.add(
            observable
                .retry(throwable -> { // always retry, will cause unconditional resubscription
                  Errors.onError(throwable);
                  return true;
                })
                .subscribe(value -> {
                  try {
                    consumer.accept(value);
                  } catch (Exception e) { // prevent unsubscription but handle exception
                    Errors.onError(e);
                  }
                })
    );
  }

  /**
   * Stops this presenter. This method is supposed to be called
   * when it is required to cancel all the pending subscriptions,
   * like ongoing HTTP requests, and unsubscribe from any {@link Observable}.
   * It might happen for example when user is navigating to another
   * {@code Activity} on Android platform.
   */
  public void stop() {
    disposables.forEach(Disposable::dispose);
    disposables.clear();
  }

  public interface CallDefiner<T> {

    void call(Consumer<T> consumer);

  }

}
