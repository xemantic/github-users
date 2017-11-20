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

import io.reactivex.Observer;

import java.util.Objects;

/**
 * An event sink capable of receiving typed events. This interface is exposing only
 * the {@link #publish(Object)} method. It is used to abstract call
 * to {@link Observer#onNext(Object)}.
 *
 * @author morisil
 */
public interface Sink<T> {

  void publish(T event);

  /**
   * Exposes an {@link Observer} as a {@link Sink}.
   *
   * @param observer the observer.
   * @param <T> the event type.
   * @return the event sink.
   */
  static  <T> Sink<T> of(Observer<T> observer) {
    Objects.requireNonNull(observer, "observer cannot be null");
    return observer::onNext;
  }

}
