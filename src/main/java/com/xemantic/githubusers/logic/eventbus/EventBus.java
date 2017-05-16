/*
 * github-users - lists GitHub users. Minimal app demonstrating
 * cross-platform development (Web, Android, iOS) on top of
 * Java to JavaScript and Java to Objective-C transpilers.
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

package com.xemantic.githubusers.logic.eventbus;

import rx.Observable;
import rx.subjects.PublishSubject;
import rx.subjects.SerializedSubject;
import rx.subjects.Subject;

import java.util.Objects;

/**
 * Minimal Event Bus based on RxJava.
 *
 * @author morisil
 */
public class EventBus {

  private final Subject<Object, Object> subject = new SerializedSubject<>(PublishSubject.create());

  /**
   * Returns observable providing elements of the specified {@code eventType}.
   *
   * @param eventType the event type class.
   * @param <T>       the actual class type generic.
   * @return the observable providing events.
   */
  @SuppressWarnings("unchecked")
  public <T> Observable<T> observe(Class<T> eventType) {
    Objects.requireNonNull(eventType);
    return (Observable<T>) subject.filter(event -> event.getClass().equals(eventType));
  }

  /**
   * Posts event on the event bus.
   *
   * @param event the event.
   */
  public void post(Object event) {
    subject.onNext(Objects.requireNonNull(event));
  }

}
