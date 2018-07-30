package org.goots.exploder.types;

import org.apache.commons.compress.archivers.ArchiveException;
import org.apache.commons.compress.archivers.ArchiveStreamFactory;

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
    public <T extends InputStream> T getStream( File source) throws FileNotFoundException, ArchiveException
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
