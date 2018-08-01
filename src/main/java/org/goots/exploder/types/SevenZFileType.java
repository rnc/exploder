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
package org.goots.exploder.types;

import org.apache.commons.compress.archivers.ArchiveInputStream;
import org.apache.commons.compress.archivers.ArchiveStreamFactory;
import org.apache.commons.compress.archivers.sevenz.SevenZFile;
import org.goots.exploder.InternalException;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

public class SevenZFileType
                extends ArchiveFileType
{
    @Override
    public String getUncompressedFilename( File source )
    {
        return source.getName();
    }

    @Override
    public String getTypename()
    {
        return ArchiveStreamFactory.SEVEN_Z;
    }

    @Override
    @SuppressWarnings({"unchecked"})
    public <T extends InputStream> T getStream( File source) throws InternalException
    {
        try
        {
            return (T) new SevenZInputStream( new SevenZFile( source ) );
        }
        catch ( IOException e )
        {
            throw new InternalException( "Exception unarchiving SeverZFile", e );
        }
    }

    /**
     * Wraps a SevenZFile to make it usable as an ArchiveInputStream.
     */
    private static class SevenZInputStream extends ArchiveInputStream
    {
        private SevenZFile file;

        SevenZInputStream(SevenZFile file) {
            this.file = file;
        }

        @Override
        public int read(byte[] b, int off, int len) throws IOException
        {
            return file.read(b, off, len);
        }

        @Override
        public org.apache.commons.compress.archivers.ArchiveEntry getNextEntry() throws IOException {
            return file.getNextEntry();
        }

        @Override
        public void close() throws IOException {
            file.close();
        }
    }
}
