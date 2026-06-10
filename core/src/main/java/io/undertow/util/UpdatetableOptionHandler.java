/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2026 Red Hat, Inc., and individual contributors
 * as indicated by the @author tags.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package io.undertow.util;

import org.xnio.Option;
import org.xnio.OptionMap;

import io.undertow.UndertowOptions;

/**
 * Util class to retain and keep option map. Note that resources using this and having "set" operation, will diverge of server
 * wide config.
 */
public class UpdatetableOptionHandler {

    private OptionMap current;

    public UpdatetableOptionHandler(final OptionMap current) {
        super();
        this.current = current;
    }

    public UpdatetableOptionHandler() {
        super();
        this.current = OptionMap.EMPTY;
    }

    public boolean supportsOption(final Option<?> option) {
        return UndertowOptions.supportsOption(option);
    }

    public <T> T getOption(final Option<T> option) {
        return current.get(option);
    }

    // NOTE: get/set dont vet supported, as in general this can contain XNIO as well....
    public <T> T getOption(final Option<T> option, final T defaultValue) {
        return current.get(option, defaultValue);
    }

    public <T> T setOption(final Option<T> option, final T value) {
        T t = getOption(option);
        this.current = OptionMap.builder().addAll(current).set(option, value).getMap();
        return t;
    }

    public OptionMap getOptionsMap() {
        return this.current;
    }
}
