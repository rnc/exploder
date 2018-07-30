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
