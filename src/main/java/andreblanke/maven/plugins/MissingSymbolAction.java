/*
 * Copyright 2019 Andre Blanke
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package andreblanke.maven.plugins;

import java.util.Locale;

import org.jetbrains.annotations.NotNull;

/**
 * Denotes a possible action taken by {@code jextract} when it encounters a missing native symbol.
 */
enum MissingSymbolAction {

    ERROR,
    INCLUDE,
    IGNORE,
    WARN;

    @NotNull
    @Override
    public String toString() {
        return name().toLowerCase(Locale.ROOT);
    }
}
