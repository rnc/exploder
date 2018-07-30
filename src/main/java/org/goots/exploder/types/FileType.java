package org.goots.exploder.types;

import org.apache.commons.compress.archivers.ArchiveException;
import org.apache.commons.compress.compressors.CompressorException;
import org.goots.exploder.InternalException;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.InputStream;

public interface FileType
{
    <T extends InputStream> T getStream( File source)
                    throws FileNotFoundException, CompressorException, ArchiveException, InternalException;

    String getUncompressedFilename(File source);

    boolean isArchive();

    boolean isCompressed();

    String getTypename ();
}
