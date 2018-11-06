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

package com.xemantic.ankh.shared.event;

import io.reactivex.Observable;
import io.reactivex.Observer;

import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Trigger carries no payload but represents a signal.
 * This class can be used as a type of {@link Observable}
 * signaling user intents after translating them from technical
 * UI events like mouse clicks.
 *
 * @author morisil
 */
public final class Trigger {

  public static final Trigger INSTANCE = new Trigger();

  private Trigger() { /* no instantiation */ }

  public static void fire(Observer<Trigger> observer) {
    (Objects.requireNonNull(observer)).onNext(Trigger.INSTANCE);
  }

  public static Observable<Trigger> noTriggers() {
    return Observable.empty();
  }

  /**
   * Creates {@link Observable} emitting only one {@link Trigger} instance on
   * creation. For subsequent subscribers it behaves like {@link Observable#empty()}.
   *
   * @return the one time firing Observable.
   */
  public static Observable<Trigger> oneTime() {
    AtomicBoolean firstTime = new AtomicBoolean(true);
    return Observable.create(e -> {
      if (firstTime.get()) {
        firstTime.set(false);
        e.onNext(Trigger.INSTANCE);
      }
    });
  }

}
