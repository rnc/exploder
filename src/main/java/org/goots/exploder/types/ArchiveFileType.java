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

import org.apache.commons.compress.archivers.ArchiveException;
import org.apache.commons.compress.archivers.ArchiveStreamFactory;
import org.goots.exploder.InternalException;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

public abstract class ArchiveFileType
                implements FileType
{
    private static ArchiveStreamFactory archiveStreamFactory = new ArchiveStreamFactory( );

    @Override
    @SuppressWarnings({"unchecked"})
    public <T extends InputStream> T getStream( File source)
                    throws FileNotFoundException, ArchiveException, InternalException
    {
        return (T) archiveStreamFactory.createArchiveInputStream ( new BufferedInputStream( new FileInputStream( source)) );
    }

    @Override
    public String toString ()
    {
        return this.getTypename();
    }

    @Override
    public boolean isArchive()
    {
        return true;
    }

    @Override
    public boolean isCompressed()
    {
        return false;
    }
}
