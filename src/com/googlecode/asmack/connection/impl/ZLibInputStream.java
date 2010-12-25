/*
 * Licensed under Apache License, Version 2.0 or LGPL 2.1, at your option.
 * --
 *
 * Copyright 2010 Rene Treffer
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
 *
 * --
 *
 * Copyright (C) 2010 Rene Treffer
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301 USA
 */

package com.googlecode.asmack.connection.impl;

import java.io.IOException;
import java.io.InputStream;
import java.util.zip.Inflater;
import java.util.zip.InflaterInputStream;

/**
 * ZLibInputStream is a zlib and input stream compatible version of an
 * InflaterInputStream. This class solves the incompatibility between
 * {@link InputStream#available()} and {@link InflaterInputStream#available()}.
 */
public class ZLibInputStream extends InflaterInputStream {

    /**
     * Construct a ZLibInputStream, reading data from the underlying stream.
     *
     * @param is The {@code InputStream} to read data from.
     * @throws IOException If an {@code IOException} occurs.
     */
    public ZLibInputStream(InputStream is) throws IOException {
        super(is, new Inflater(), 512);
    }

    /**
     * Provide a more InputStream compatible version of available.
     * A return value of 1 means that it is likly to read one byte without
     * blocking, 0 means that the system is known to block for more input.
     *
     * @return 0 if no data is available, 1 otherwise
     * @throws IOException
     */
    @Override
    public int available() throws IOException {
        /* This is one of the funny code blocks.
         * InflaterInputStream.available violates the contract of
         * InputStream.available, which breaks kXML2.
         *
         * I'm not sure who's to blame, oracle/sun for a broken api or the
         * google guys for mixing a sun bug with a xml reader that can't handle
         * it....
         *
         * Anyway, this simple if breaks suns distorted reality, but helps
         * to use the api as intended.
         */
        if (inf.needsInput()) {
            return 0;
        }
        return super.available();
    }

}
