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

package com.xemantic.githubusers.logic.error;

import io.reactivex.Maybe;

/**
 * Transforms {@link Throwable} into optional user-friendly message.
 * Only specific runtime exceptions will be presented to end-user on the
 * snackbar, and they should be already translated into concise
 * non-technical form like {@code you are offline}. Note that specific
 * technical exceptions indicating lack of internet access would be different
 * on each platform depending on underlying HTTP transport mechanism used
 * for web service calls.
 *
 * @author morisil
 */
public interface ErrorMessageProvider {

  Maybe<String> getMessage(Throwable throwable);

}
