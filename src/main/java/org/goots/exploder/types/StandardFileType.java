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

import org.goots.exploder.InternalException;

import java.io.File;
import java.io.InputStream;

public class StandardFileType implements FileType
{
    @Override
    public <T extends InputStream> T getStream( File source) throws InternalException
    {
        throw new InternalException( "getStream is not supported for standard files" );
    }

    @Override
    public String getUncompressedFilename( File source )
    {
        return source.getPath();
    }

    @Override
    public String toString ()
    {
        return getTypename();
    }

    @Override
    public boolean isArchive()
    {
        return false;
    }

    @Override
    public boolean isCompressed()
    {
        return false;
    }

    @Override
    public String getTypename()
    {
        return "file";
    }
}
