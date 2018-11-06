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

import com.xemantic.ankh.shared.presenter.Presenter;
import dagger.Module;
import io.reactivex.Observable;

import javax.inject.Inject;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotates a class representing event.
 * <p>
 * When this annotation is processed, it will cause generation of new {@link Module}
 * defining event channel for specific event type. The channel consists
 * of typed {@link Sink} (publishing) and {@link Observable} (subscribing).
 * </p>
 * <p>
 * If you want to emit or receive events in your code, just {@link Inject} typed
 * {@link Sink} or {@link Observable} into your {@link Presenter}.
 * </p>
 * <pre>
 *   &#064;Inject
 *   public FooPresenter(FooView view, Sink&lt;BarEvent&gt; barSink) {
 *     super(
 *       view.click$()
 *           .onNext(e -> barSink.publish(new BarEvent("payload"))
 *     );
 *   }
 * </pre>
 *
 * @author morisil
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface Event {
}
