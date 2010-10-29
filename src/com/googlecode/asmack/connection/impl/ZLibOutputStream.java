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
import java.io.OutputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.security.NoSuchAlgorithmException;
import java.util.zip.Deflater;
import java.util.zip.DeflaterOutputStream;

/**
 * <p>Android 2.2 includes Java7 FLUSH_SYNC option, which will be used by this
 * Implementation, preferable via reflection.</p>
 * <p>Please use {@link ZLibOutputStream#SUPPORTED} to check for flush
 * compatibility.</p> 
 */
public class ZLibOutputStream extends DeflaterOutputStream {

    /**
     * The reflection based flush method.
     */

    private final static Method method;
    /**
     * SUPPORTED is true if a flush compatible method exists.
     */
    public final static boolean SUPPORTED;

    /**
     * Static block to initialize {@link #SUPPORTED} and {@link #method}.
     */
    static {
        Method m = null;
        try {
            m = Deflater.class.getMethod("deflate", byte[].class, int.class, int.class, int.class);
        } catch (SecurityException e) {
        } catch (NoSuchMethodException e) {
        }
        method = m;
        SUPPORTED = (method != null);
    }

    /**
     * Create a new ZLib compatible output stream wrapping the given low level
     * stream. ZLib compatiblity means we will send a zlib header. 
     * @param os OutputStream The underlying stream.
     * @throws IOException In case of a lowlevel transfer problem.
     * @throws NoSuchAlgorithmException In case of a {@link Deflater} error.
     */
    public ZLibOutputStream(OutputStream os) throws IOException,
            NoSuchAlgorithmException {
        super(os, new Deflater(Deflater.BEST_COMPRESSION));
    }

    /**
     * Flush the given stream, preferring Java7 FLUSH_SYNC if available.
     * @throws IOException In case of a lowlevel exception.
     */
    @Override
    public void flush() throws IOException {
        if (!SUPPORTED) {
            super.flush();
            return;
        }
        int count = 0;
        if (!def.needsInput()) {
            do {
                count = def.deflate(buf, 0, buf.length);
                out.write(buf, 0, count);
            } while (count > 0);
            out.flush();
        }
        try {
            do {
                count = (Integer) method.invoke(def, buf, 0, buf.length, 2);
                out.write(buf, 0, count);
            } while (count > 0);
        } catch (IllegalArgumentException e) {
            throw new IOException("Can't flush");
        } catch (IllegalAccessException e) {
            throw new IOException("Can't flush");
        } catch (InvocationTargetException e) {
            throw new IOException("Can't flush");
        }
        super.flush();
    }

}