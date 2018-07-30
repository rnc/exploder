package org.goots.exploder;

import org.apache.commons.compress.archivers.ArchiveStreamFactory;
import org.apache.commons.compress.compressors.CompressorStreamFactory;
import org.apache.commons.compress.compressors.bzip2.BZip2Utils;
import org.apache.commons.compress.compressors.gzip.GzipUtils;
import org.apache.commons.compress.compressors.lzma.LZMAUtils;
import org.apache.commons.compress.compressors.xz.XZUtils;
import org.goots.exploder.types.BZIP2FileType;
import org.goots.exploder.types.FileType;
import org.goots.exploder.types.GZIPFileType;
import org.goots.exploder.types.JavaFileType;
import org.goots.exploder.types.LZMAFileType;
import org.goots.exploder.types.StandardFileType;
import org.goots.exploder.types.TarFileType;
import org.goots.exploder.types.XZFileType;
import org.goots.exploder.types.ZipFileType;

import java.io.File;
import java.util.Collections;
import java.util.HashMap;
import java.util.Set;

/**
 * Encapsulates all possible supported archive and compression formats. Utilises commons-compress.
 */
public class FileHandler
{
    private static final String WAR = "war";

    private static final String EAR = "ear";

    private static final String FILE = "file";

    private final HashMap<String,FileType> fileTypes = new HashMap<>(  );


    public FileHandler()
    {
        fileTypes.put( CompressorStreamFactory.BZIP2, new BZIP2FileType() );
        fileTypes.put( CompressorStreamFactory.LZMA, new LZMAFileType() );
        fileTypes.put( CompressorStreamFactory.XZ, new XZFileType() );
        fileTypes.put( CompressorStreamFactory.GZIP, new GZIPFileType() );
        fileTypes.put( ArchiveStreamFactory.TAR, new TarFileType() );
        fileTypes.put( ArchiveStreamFactory.ZIP, new ZipFileType() );
        fileTypes.put( ArchiveStreamFactory.JAR, new JavaFileType() );
        fileTypes.put( WAR, new JavaFileType() );
        fileTypes.put( EAR, new JavaFileType() );
        fileTypes.put( FILE, new StandardFileType() );
    }


    public Set<String> getSupportedSuffixes ()
    {
        return Collections.unmodifiableSet( fileTypes.keySet() );
    }


    public FileType getType ( File source )
    {
        if ( GzipUtils.isCompressedFilename( source.getName() ) )
        {
            return fileTypes.get( CompressorStreamFactory.GZIP );
        }
        else if ( LZMAUtils.isCompressedFilename( source.getName() ) )
        {
            return fileTypes.get( CompressorStreamFactory.LZMA );
        }
        else if ( XZUtils.isCompressedFilename( source.getName() ) )
        {
            return fileTypes.get( CompressorStreamFactory.XZ );
        }
        else if ( BZip2Utils.isCompressedFilename( source.getName() ) )
        {
            return fileTypes.get( CompressorStreamFactory.BZIP2 );
        }
        // Now check archive types.
        else if ( source.getName().endsWith( ".tar" ) )
        {
            return fileTypes.get( ArchiveStreamFactory.TAR );
        }
        else if ( source.getName().endsWith( ".zip" ) )
        {
            return fileTypes.get( ArchiveStreamFactory.TAR );
        }
        else if ( source.getName().endsWith( ".jar" ) )
        {
            return fileTypes.get( ArchiveStreamFactory.JAR );
        }
        else if ( source.getName().endsWith( ".war" ) )
        {
            return fileTypes.get( WAR );
        }
        else if ( source.getName().endsWith( ".ear" ) )
        {
            return fileTypes.get( EAR );
        }
        else
        {
            return fileTypes.get( FILE );
        }
        // TODO: Other archive types : ar, 7z.
    }
}
