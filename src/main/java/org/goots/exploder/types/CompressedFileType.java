package org.goots.exploder.types;

import org.apache.commons.compress.compressors.CompressorException;
import org.apache.commons.compress.compressors.CompressorStreamFactory;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

public abstract class CompressedFileType implements FileType
{
    private static CompressorStreamFactory compressorStreamFactory = new CompressorStreamFactory( );

    @Override
    @SuppressWarnings({"unchecked"})
    public <T extends InputStream> T getStream( File source) throws FileNotFoundException, CompressorException
    {
        return (T)compressorStreamFactory.createCompressorInputStream( new BufferedInputStream( new FileInputStream( source)) );
    }

    @Override
    public String toString ()
    {
        return this.getTypename();
    }

    @Override
    public boolean isArchive()
    {
        return false;
    }

    @Override
    public boolean isCompressed()
    {
        return true;
    }
}
