/**
 * Copyright (c) Codice Foundation
 *
 * <p>This is free software: you can redistribute it and/or modify it under the terms of the GNU
 * Lesser General Public License as published by the Free Software Foundation, either version 3 of
 * the License, or any later version.
 *
 * <p>This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details. A copy of the GNU Lesser General Public
 * License is distributed along with this program and can be found at
 * <http://www.gnu.org/licenses/lgpl.html>.
 */
package org.codice.ditto.replication.dominion.pax.exam.internal;

import java.lang.annotation.Annotation;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import javax.annotation.Nullable;
import org.codice.ditto.replication.dominion.options.ReplicationOptions;
import org.codice.ditto.replication.dominion.pax.exam.options.extensions.InstallExtension;
import org.codice.dominion.options.Option;

/** Factory implementation for Replication's option extensions. */
public class OptionExtensionFactory implements Option.Factory {
  private static final Map<Class<? extends Annotation>, Option.Extension> EXTENSIONS;

  static {
    final Map<Class<? extends Annotation>, Option.Extension> map = new HashMap<>(6);

    map.put(ReplicationOptions.Install.class, new InstallExtension());
    EXTENSIONS = Collections.unmodifiableMap(map);
  }

  @Nullable
  @Override
  public Option.Extension getExtension(Annotation annotation) {
    return OptionExtensionFactory.EXTENSIONS.get(annotation.annotationType());
  }
}