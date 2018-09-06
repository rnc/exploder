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
import org.apache.commons.io.FileUtils;
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
import java.util.Set;
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

    private File directoryRoot;

    private File workingDirectory;

    private boolean cleanup;

    /**
     * Register suffix to ignore when exploding the archive(s).
     *
     * @param suffix the suffix to ignore e.g. {@code .jar}
     * @return the current Exploder instance.
     * @throws InternalException if an error occurs.
     */
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

    /**
     * This will configure the current instance to use a temporary directory to
     * copy the target File to prior to unpacking. This is useful if running the
     * {@link ExploderFileProcessor} on an archive. This WILL delete the temporary
     * directory on completion.
     *
     * @return the current Exploder instance.
     * @throws InternalException if an error occurs.
     */
    public Exploder useTemporaryDirectory () throws InternalException
    {
        Path temporaryLocation;
        try
        {
            temporaryLocation = Files.createTempDirectory( "exploder-" + UUID.randomUUID().toString() );

            cleanup = true;
            useWorkingDirectory( temporaryLocation.toFile() );
        }
        catch ( IOException e )
        {
            throw new InternalException( "Error setting up working directory", e );
        }

        return this;
    }

    /**
     * This will configure the current instance to use the specified working directory
     * and copy the target File to it prior to unpacking. It will NOT delete the working
     * directory on completion.
     *
     * @param workingDirectory the specified working directory to configure.
     * @return the current Exploder instance.
     */
    public Exploder useWorkingDirectory( File workingDirectory )
    {
        this.workingDirectory = workingDirectory;

        if ( ! workingDirectory.exists() )
        {
            workingDirectory.mkdirs();
        }
        return this;
    }

    public Set<String> getSupportedSuffixes ()
    {
        return fsh.getSupportedSuffixes();
    }

    /**
     * Unpacks the contents of the file/directory, decompressing and unarchiving recursively.
     *
     * If a working/temporary directory has been set it will copy everything to that first. If
     * a temporary directory has been configured it will be cleaned up at the end.
     *
     * @param root root file (or directory contents) to explode
     * @throws InternalException if an error occurs.
     */
    public void unpack ( File root ) throws InternalException
    {
        unpack( null, root );
    }

    /**
     * Unpacks the contents of the file/directory, decompressing and unarchiving recursively.
     * It will use the specified ExploderFileProcessor on each target file.
     *
     * If a working/temporary directory has been set it will copy everything to that first. If
     * a temporary directory has been configured it will be cleaned up at the end.
     *
     * @param processor the optional FileProcessor
     * @param root root file (or directory contents) to explode
     * @throws InternalException if an error occurs.
     */
    public void unpack( ExploderFileProcessor processor, File root ) throws InternalException
    {
        try
        {
            if ( workingDirectory == null )
            {
                workingDirectory = root;
            }
            else
            {
                if ( root.isDirectory() )
                {
                    FileUtils.copyDirectory( root, workingDirectory );
                }
                else if ( root.isFile() )
                {
                    if ( workingDirectory.isDirectory() )
                    {
                        FileUtils.copyFileToDirectory( root, workingDirectory );
                    }
                    else
                    {
                        FileUtils.copyFile( root, workingDirectory );
                    }
                }
                else
                {
                    throw new InternalException(
                                    "Target (" + root + ") is not directory or file ( exists: " + root.exists() + ')' );
                }
            }

            logger.debug( "Setting directory root to {}", root );
            directoryRoot = workingDirectory;

            internal_unpack( processor, workingDirectory );

            if ( cleanup )
            {
                try
                {
                    logger.debug( "Cleaning up temporary directory {} ", workingDirectory );
                    FileUtils.deleteDirectory( workingDirectory );
                }
                catch ( IOException e )
                {
                    throw new InternalException( "Error cleaning up working directory", e );
                }
            }
        }
        catch ( IOException e )
        {
            throw new InternalException( "Error setting up workingDirectory directory", e );
        }
    }

    /**
     * Unpacks the contents of the file/directory, decompressing and unarchiving recursively.
     * It will use the specified ExploderFileProcessor on each target file. This is a internal
     * method that will recurse correctly ; the wrapper unpack methods handle cleanup and working
     * directory configuration.
     *
     * @param processor the optional FileProcessor
     * @param root root file (or directory contents) to explode
     * @throws InternalException if an error occurs.
     */
    private void internal_unpack( ExploderFileProcessor processor, File root ) throws InternalException
    {
        if ( root.isDirectory() )
        {
            try ( DirectoryStream<Path> stream = Files.newDirectoryStream( root.toPath() ) )
            {
                for ( Path entry : stream )
                {
                    internal_unpack( processor, entry.toFile() );
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
                    logger.debug( "Unpacking {} and type {} ", root, type.getTypename() );

                    unpackArchive( root, type, processor );
                }
                else if ( type.isCompressed() )
                {
                    logger.debug( "Decompressing {}", root );

                    decompressFile ( root, type, processor );
                }
                else
                {
                    logger.debug( "Found standard file {} ", root );
                }
            }
            if ( processor != null )
            {
                processor.processFile( directoryRoot, root );
            }
        }
    }

    private void decompressFile( File root, FileType type, ExploderFileProcessor processor ) throws InternalException
    {
        try (CompressorInputStream c = type.getStream( root ))
        {
            File destination = new File( type.getUncompressedFilename( root ) );
            IOUtils.copy( c, Files.newOutputStream( destination.toPath() ) );

            logger.debug( "Now examining decompressed file {} ", destination );

            // Examine unpacked file - that in itself may be an ordinary file or an archive etc.
            internal_unpack( processor, destination );
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
            internal_unpack( processor, target );
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
