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

import org.apache.commons.compress.archivers.ArchiveStreamFactory;
import org.apache.commons.compress.compressors.CompressorStreamFactory;
import org.apache.commons.compress.compressors.bzip2.BZip2Utils;
import org.apache.commons.compress.compressors.gzip.GzipUtils;
import org.apache.commons.compress.compressors.lzma.LZMAUtils;
import org.apache.commons.compress.compressors.xz.XZUtils;
import org.goots.exploder.types.ARFileType;
import org.goots.exploder.types.BZIP2FileType;
import org.goots.exploder.types.CpioFileType;
import org.goots.exploder.types.DumpFileType;
import org.goots.exploder.types.FileType;
import org.goots.exploder.types.GZIPFileType;
import org.goots.exploder.types.JavaFileType;
import org.goots.exploder.types.LZMAFileType;
import org.goots.exploder.types.SevenZFileType;
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
class FileHandler
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
        fileTypes.put( ArchiveStreamFactory.AR, new ARFileType() );
        fileTypes.put( ArchiveStreamFactory.CPIO, new CpioFileType() );
        fileTypes.put( ArchiveStreamFactory.SEVEN_Z, new SevenZFileType() );
        fileTypes.put( ArchiveStreamFactory.DUMP, new DumpFileType() );
    }


    Set<String> getSupportedSuffixes ()
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
        else if ( source.getName().endsWith( ".ar" ) )
        {
            return fileTypes.get( ArchiveStreamFactory.AR );
        }
        else if ( source.getName().endsWith( ".cpio" ) )
        {
            return fileTypes.get( ArchiveStreamFactory.CPIO );
        }
        else if ( source.getName().endsWith( ".7z" ) )
        {
            return fileTypes.get( ArchiveStreamFactory.SEVEN_Z );
        }
        else if ( source.getName().endsWith( ".dump" ) )
        {
            return fileTypes.get( ArchiveStreamFactory.DUMP );
        }
        else
        {
            return fileTypes.get( FILE );
        }
        // TODO: Other archive types : ar, 7z.
    }
}
