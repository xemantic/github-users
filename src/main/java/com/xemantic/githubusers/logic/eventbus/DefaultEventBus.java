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

package com.xemantic.githubusers.logic.eventbus;

import rx.Observable;
import rx.subjects.PublishSubject;
import rx.subjects.SerializedSubject;
import rx.subjects.Subject;

import java.util.Objects;

/**
 * Minimal Event Bus based on {@code RxJava}.
 *
 * @author morisil
 */
public class DefaultEventBus implements EventBus {

  private final Subject<Object, Object> subject = new SerializedSubject<>(PublishSubject.create());

  /** {@inheritDoc} */
  @SuppressWarnings("unchecked")
  @Override
  public <T> Observable<T> observe(Class<T> eventType) {
    Objects.requireNonNull(eventType);
    return (Observable<T>) subject.filter(event -> event.getClass().equals(eventType));
  }

  /** {@inheritDoc} */
  @Override
  public void post(Object event) {
    subject.onNext(Objects.requireNonNull(event));
  }

}
