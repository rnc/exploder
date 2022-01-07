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

import org.apache.commons.compress.archivers.ArchiveException;
import org.apache.commons.compress.archivers.ArchiveInputStream;
import org.apache.commons.compress.archivers.ArchiveStreamFactory;
import org.apache.commons.compress.compressors.CompressorException;
import org.apache.commons.compress.compressors.CompressorStreamFactory;
import org.apache.commons.io.FileUtils;
import org.goots.exploder.types.FileType;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class FileHandlerTest
{
    @Rule
    public TemporaryFolder folder = new TemporaryFolder();

    @Test
    public void testFileNames()
    {
        FileHandler f = new FileHandler();

        File test = new File( "/tmp/t.tgz" );
        assertEquals( f.getType( test ).getTypename(), CompressorStreamFactory.GZIP );
        assertEquals( "/tmp/t.tar", f.getType( test ).getUncompressedFilename( test ) );
    }

    @Test
    public void testFile1() throws IOException
    {
        FileHandler f = new FileHandler();
        File test = folder.newFile( "myfile" );

        assertTrue( f.getType( test ).getTypename().equals( "file" ) );
    }

    @Test
    public void testFile2() throws IOException
    {
        FileHandler f = new FileHandler();
        File test = folder.newFile( "myfile.jar" );

        FileType type = f.getType( test );
        assertEquals( type.toString(), ArchiveStreamFactory.JAR );
        assertTrue( type.isArchive() );
    }

    @Test
    public void testFileWar() throws IOException
    {
        FileHandler f = new FileHandler();
        File test = folder.newFile( "myfile.war" );

        FileType type = f.getType( test );
        assertEquals( type.toString(), ArchiveStreamFactory.JAR );
        assertTrue( type.isArchive() );
    }

    @Test
    public void testFile3() throws IOException, InternalException, CompressorException, ArchiveException
    {
        File target = new File (folder.getRoot(), "sourceclear.jar" );
        URL source = new URL ( "https://repo1.maven.org/maven2/com/srcclr/srcclr-maven-plugin/3.0.0/srcclr-maven-plugin-3.0.0.jar");
        FileUtils.copyURLToFile( source, target);

        FileHandler f = new FileHandler();
        FileType type = f.getType( target );
        assertEquals( type.toString(), ArchiveStreamFactory.JAR );
        assertTrue( type.isArchive() );
        InputStream i = type.getStream( target );
        assertTrue ( i instanceof ArchiveInputStream );
    }
}