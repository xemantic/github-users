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

package com.xemantic.ankh.shared.request;

import com.xemantic.ankh.shared.event.Trigger;
import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.disposables.Disposable;

/**
 * The service response wrapped as a page with associated number.
 *
 * @author morisil
 * @param <T> the type of response payload wrapped by this page.
 */
public class Page<T> {

  private final int number;

  private final T payload;

  public Page(int number, T payload) {
    this.number = number;
    this.payload = payload;
  }

  public int getNumber() {
    return number;
  }

  public T getPayload() {
    return payload;
  }

  /**
   * Will return {@link Observable} of integer numbers representing pages
   * to request from remote server. The first page is emitted immediately,
   * the subsequent pages are emitted on {@link Trigger}s from specified
   * {@link Observable}.
   *
   * @param trigger$ the triggers causing emission of subsequent numbers.
   * @return the sequence of page numbers.
   */
  public static Observable<Integer> emitPagesOn(Observable<Trigger> trigger$) {
    return Observable.create(new ObservableOnSubscribe<Integer>() {
      private boolean firstTime = true;
      private int page = 1;
      @Override
      public void subscribe(ObservableEmitter<Integer> emitter) throws Exception {
        Disposable triggerSubscription = trigger$.subscribe(trigger -> {
          if (!emitter.isDisposed()) {
            emitter.onNext(page++);
          }
        });
        emitter.setDisposable(new Disposable() {
          @Override
          public void dispose() {
            page--;
            triggerSubscription.dispose();
          }
          @Override
          public boolean isDisposed() {
            return triggerSubscription.isDisposed();
          }
        });
        if (!emitter.isDisposed()) {
          if (firstTime) {
            firstTime = false;
            emitter.onNext(page++);
          }
        }
      }
    });
  }

}
