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

import org.apache.commons.compress.archivers.ArchiveEntry;
import org.apache.commons.compress.archivers.ArchiveException;
import org.apache.commons.compress.archivers.ArchiveInputStream;
import org.apache.commons.compress.compressors.CompressorException;
import org.apache.commons.compress.compressors.CompressorInputStream;
import org.apache.commons.io.IOUtils;
import org.goots.exploder.types.FileType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.UUID;

public class Exploder
{
    /**
     * Use a unique suffix for unpacking archives.
     */
    public final static String ARCHIVE_UNPACK_SUFFIX = "-unpacked-" + UUID.randomUUID().toString();

    private final Logger logger = LoggerFactory.getLogger( getClass() );

    private final FileHandler fsh = new FileHandler();

    private final HashSet<String> excludedSuffixes = new HashSet<>( );


    public Exploder excludeSuffix ( String suffix ) throws InternalException
    {
        suffix = suffix.trim().toLowerCase();
        if ( ! fsh.getSupportedSuffixes().contains( suffix ) )
        {
            throw new InternalException( "Unknown suffix " + suffix );
        }
        excludedSuffixes.add( suffix );

        return this;
    }

    public void unpack ( File root ) throws InternalException
    {
        unpack( root, null );
    }

    public void unpack ( File root, ExploderFileProcessor processor ) throws InternalException
    {
        if ( root.isDirectory() )
        {
            try ( DirectoryStream<Path> stream = Files.newDirectoryStream( root.toPath() ) )
            {
                for ( Path entry : stream )
                {
                    unpack( entry.toFile(), processor );
                }
            }
            catch ( IOException e )
            {
                throw new InternalException( "Unable to iterate through directory", e );
            }
        }
        else
        {
            FileType type = fsh.getType( root );

            // Check is type is supported for decompression / extraction or just move to processing.
            if ( !excludedSuffixes.contains( type.getTypename() ) )
            {
                if ( type.isArchive() )
                {
                    logger.info( "Unpacking {}", root );

                    unpackArchive( root, type, processor );
                }
                else if ( type.isCompressed() )
                {
                    logger.info( "Decompressing {}", root );

                    decompressFile ( root, type, processor );
                }
                else
                {
                    logger.info( "Found standard file {} ", root );
                }
            }
            if ( processor != null )
            {
                processor.processFile( null, null, null );
            }
        }
    }

    private void decompressFile( File root, FileType type, ExploderFileProcessor processor ) throws InternalException
    {
        try (CompressorInputStream c = type.getStream( root ))
        {
            File destination = new File( type.getUncompressedFilename( root ) );
            IOUtils.copy( c, Files.newOutputStream( destination.toPath() ) );

            logger.info( "### Now examining decompressed file {} ", destination );

            // Examine unpacked file - that in itself may be an ordinary file or an archive etc.
            unpack( destination, processor );
        }
        catch ( CompressorException | ArchiveException | IOException e )
        {
            throw new InternalException( "Caught exception decompressing file", e );
        }
    }

    private void unpackArchive( File root, FileType type, ExploderFileProcessor processor ) throws InternalException
    {
        try ( ArchiveInputStream i = type.getStream( root ) )
        {
            File target = new File( root.getParentFile(), root.getName() + ARCHIVE_UNPACK_SUFFIX );
            target.mkdirs();

            extract( i, target );

            // Recurse into unpacked directory
            unpack( target, processor );
        }
        catch ( CompressorException | ArchiveException | IOException e )
        {
            throw new InternalException( "Caught exception unpacking archive", e );
        }
    }

    private void extract( ArchiveInputStream input, File destination) throws IOException, InternalException
    {
        ArchiveEntry entry;
        while ( (entry = input.getNextEntry()) != null)
        {
            if ( ! input.canReadEntryData( entry ) )
            {
                throw new InternalException( "Unable to read data entry for " + entry.toString() );
            }
            File file = new File(destination, entry.getName());

            logger.info( "Entry is {} and file is {}", entry, file );
            if (entry.isDirectory())
            {
                file.mkdirs();
            }
            else
            {
                file.getParentFile().mkdirs();

                try (FileOutputStream output = new FileOutputStream( file ))
                {
                    IOUtils.copy( input, output );
                }
            }
        }
    }
}
