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
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
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

    private File targetDirectory;

    private boolean cleanup;

    private boolean recurse = true;

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
     * This will disable recursive unpack. By default Exploder will recursively unpack all supported
     * types.
     *
     * @return the current Exploder instance.
     */
    public Exploder disableRecursion()
    {
        recurse = false;

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
        if ( this.targetDirectory != null )
        {
            throw new InternalException( "Working directory already configured" );
        }
        try
        {
            Path temporaryLocation = Files.createTempDirectory( "exploder-" + UUID.randomUUID().toString() );
            cleanup = true;
            targetDirectory = temporaryLocation.toFile();
        }
        catch ( IOException e )
        {
            throw new InternalException( "Error setting up working directory", e );
        }
        return this;
    }

    /**
     * This will configure the current instance to use the specified target directory
     * and copy the target File to it prior to unpacking. It will NOT delete the working
     * directory on completion.
     *
     * @param targetDirectory the specified target directory to configure.
     * @return the current Exploder instance.
     * @throws InternalException if an error occurs.
     */
    public Exploder useTargetDirectory( File targetDirectory ) throws InternalException
    {
        if ( this.targetDirectory != null )
        {
            throw new InternalException( "Target directory already configured" );
        }
        this.targetDirectory = targetDirectory;

        if ( ! targetDirectory.exists() )
        {
            targetDirectory.mkdirs();
        }
        return this;
    }

    public Set<String> getSupportedSuffixes ()
    {
        return fsh.getSupportedSuffixes();
    }

    /**
     * Unpacks the contents of the url/file/directory, decompressing and unarchiving recursively.
     *
     * This is a simple wrapper around the File/URL methods.
     *
     * @param path root url, file (or directory contents) to explode
     * @throws InternalException if an error occurs.
     */
    public void unpack ( String path ) throws InternalException
    {
        if ( path.startsWith("http:") || path.startsWith("https:") || path.startsWith("file:") )
        {
            try
            {
                unpack(null, new URL ( path ) );
            }
            catch ( MalformedURLException e )
            {
                throw new InternalException( "Unable to translate path (" + path + ") into URL." );
            }
        }
        else
        {
            unpack( new File ( path ) );
        }
    }

    /**
     * Unpacks the contents of the remote file, decompressing and unarchiving recursively.
     * It will use the specified ExploderFileProcessor on each target file.
     *
     * If a temporary or working directory has not been configured then this will implicitly
     * create and use a temporary directory which WILL be cleaned up.
     *
     * @param processor the optional FileProcessor
     * @param url remote file to explode
     * @throws InternalException if an error occurs.
     */
    public void unpack( ExploderFileProcessor processor, URL url ) throws InternalException
    {
        try
        {
            if ( targetDirectory == null )
            {
                useTemporaryDirectory();
            }

            File target = new File ( Files.createTempDirectory( "exploder-" + UUID.randomUUID().toString() ).toFile(),
                                     url.getFile().substring( url.getFile().lastIndexOf( '/' ) ) );

            // TODO : Implement concurrent axel/aria2c downloader
            logger.debug( "Downloading URL {} to {} unpacking to {}", url, target, targetDirectory );
            FileUtils.copyURLToFile(url, target);

            directoryRoot = targetDirectory;

            internal_unpack( processor, target, targetDirectory );
        }
        catch ( IOException e )
        {
            throw new InternalException( "Error downloading remote URL", e );
        }
        finally
        {
            cleanup();
        }
    }

    /**
     * Unpacks the contents of the file/directory, decompressing and unarchiving recursively.
     *
     * If a root is a directory then it will copy the it to the target directory first. If
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
     * If a root is a directory then it will copy the it to the target directory first. If
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
            if ( targetDirectory == null )
            {
                targetDirectory = root.isDirectory() ? root : root.getParentFile();
            }
            else
            {
                if ( root.isDirectory() )
                {
                    FileUtils.copyDirectory( root, targetDirectory );
                    root = targetDirectory;
                }
                else if ( ! root.isFile() )
                {
                    throw new InternalException(
                                    "Target (" + root + ") is not directory or file ( exists: " + root.exists() + ')' );
                }
            }

            logger.debug( "Setting directory root to {} with target directory {}", root, targetDirectory.getAbsolutePath() );
            directoryRoot = targetDirectory;

            internal_unpack( processor, root, targetDirectory );
        }
        catch ( IOException e )
        {
            throw new InternalException( "Error setting up targetDirectory directory", e );
        }
        finally
        {
            cleanup();
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
     * @param targetDirectory target directory to unpack to. Only used for first level unpack.
     * @throws InternalException if an error occurs.
     */
    private void internal_unpack( ExploderFileProcessor processor, File root, File targetDirectory ) throws InternalException
    {
        if ( root.isDirectory() )
        {
            try ( DirectoryStream<Path> stream = Files.newDirectoryStream( root.toPath() ) )
            {
                for ( Path entry : stream )
                {
                    internal_unpack( processor, entry.toFile(), null );
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
                    logger.debug( "Unpacking {} and type {}", root, type.getTypename());

                    unpackArchive( root, type, processor, targetDirectory );
                }
                else if ( type.isCompressed() )
                {
                    logger.debug( "Decompressing {}", root );

                    decompressFile ( root, type, processor, targetDirectory );
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

    private void decompressFile( File root, FileType type, ExploderFileProcessor processor, File targetDirectory ) throws InternalException
    {
        try (CompressorInputStream c = type.getStream( root ))
        {
            File destination;
            if ( targetDirectory != null )
            {
                destination = new File( targetDirectory, type.getUncompressedFilename( new File ( root.getName() ) ) );
            }
            else
            {
                destination = new File( type.getUncompressedFilename( root ) );
            }

            IOUtils.copy( c, Files.newOutputStream( destination.toPath() ) );

            logger.debug( "Now examining decompressed file {} ", destination );

            if ( recurse )
            {
                // Examine decompressed file - that in itself may be an ordinary file or an archive etc.
                internal_unpack( processor, destination, null );
            }
        }
        catch ( CompressorException | ArchiveException | IOException e )
        {
            throw new InternalException( "Caught exception decompressing file", e );
        }
    }

    private void unpackArchive( File root, FileType type, ExploderFileProcessor processor, File targetDirectory ) throws InternalException
    {
        try ( ArchiveInputStream i = type.getStream( root ) )
        {
            File target;
            if ( targetDirectory == null )
            {
                target = new File( root.getParentFile(), root.getName() + ARCHIVE_UNPACK_SUFFIX );
                target.mkdirs();
            }
            else
            {
                target = targetDirectory;
            }
            extract( i, target );

            if ( recurse )
            {
                // Recurse into unpacked directory
                internal_unpack( processor, target, null );
            }
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

    private void cleanup() throws InternalException
    {
        if ( cleanup )
        {
            try
            {
                logger.debug( "Cleaning up temporary directory {} ", targetDirectory );
                FileUtils.deleteDirectory( targetDirectory );
            }
            catch ( IOException e )
            {
                throw new InternalException( "Error cleaning up working directory", e );
            }
        }
    }
}
