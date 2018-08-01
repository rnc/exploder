/*
 * Copyright (C) 2018 Red Hat, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.goots.exploder;

import org.apache.commons.lang.StringUtils;

import java.io.File;

public interface ExploderFileProcessor
{
    /**
     * Perform arbitrary processing upon a standard file after unpacking and decompression.
     *
     * @param baseDir the base temporary folder from which the zip/jar/ear/war/etc was unzipped
     * @param file file to process
     * @throws InternalException if the processing reports an error. This will <b>abort</b> the
     *    unpack/decompress.
     */
    void processFile( File baseDir, File file ) throws InternalException;

    /**
     * Return the virtual path from the top level to the target location removing any temporary 'unpacked' markers.
     * This is not a valid file system path.
     *
     * @param baseDir the top level working directory
     * @param target the target file
     * @return the virtual path string.
     */
    default String getVirtualPath (File baseDir, File target)
    {
        return StringUtils.remove( StringUtils.removeStart( target.getPath(), baseDir.getPath() + File.separator),
                                   Exploder.ARCHIVE_UNPACK_SUFFIX );
    }
}
