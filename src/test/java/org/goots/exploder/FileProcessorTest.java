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

import org.junit.Rule;
import org.junit.Test;
import org.junit.contrib.java.lang.system.SystemOutRule;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.io.IOException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class FileProcessorTest
{
    private static final File RESOURCES_DIR = new File("src/test/resources");

    @Rule
    public SystemOutRule output = new SystemOutRule().muteForSuccessfulTests();

    @Rule
    public TemporaryFolder folder = new TemporaryFolder();

    @Test
    public void testUnpackWithTemporary() throws IOException, InternalException
    {
        File target = new File (RESOURCES_DIR, "example.war" );
        File temporaryFolder = folder.newFolder();

        Exploder u = new Exploder().useWorkingDirectory( temporaryFolder );

        u.unpack( new Processor(""), target );

        File verify = new File( temporaryFolder, target.getName() + Exploder.ARCHIVE_UNPACK_SUFFIX );
        File verifyjar = new File( verify.toString() + "/example.jar" + Exploder.ARCHIVE_UNPACK_SUFFIX );
        assertTrue( verify.exists() );

        assertTrue ( verifyjar.exists() );
        assertTrue ( new File ( verifyjar.toString() + "/folder" ).exists() );

        File verifyjarclass = new File( verifyjar + "/folder/Exploder.class" );
        assertTrue ( verifyjarclass.exists() );
    }

    @Test(expected = InternalException.class)
    public void testUnpackWithTemporaryFails() throws IOException, InternalException
    {
        File target = new File( RESOURCES_DIR, "example.war" );
        File temporaryFolder = folder.newFolder();

        Exploder u = new Exploder().useWorkingDirectory( temporaryFolder );
        u.unpack( new ProcessorFails(), target );
    }

    @Test
    public void testUnpackWithTemporaryAndVirtual1() throws IOException, InternalException
    {
        File target = new File (RESOURCES_DIR, "example.war" );
        File temporaryFolder = folder.newFolder();
        Processor p = new Processor("Exploder.class");

        Exploder u = new Exploder().useWorkingDirectory( temporaryFolder );
        u.unpack( p, target );

        assertEquals( "folder/Exploder.class", p.virtualPath );
    }

    @Test
    public void testUnpackWithTemporaryAndVirtual2() throws IOException, InternalException
    {
        File target = new File (RESOURCES_DIR, "example.war" );
        File temporaryFolder = folder.newFolder();
        Processor p = new Processor("example.jar");

        Exploder u = new Exploder().useWorkingDirectory( temporaryFolder );
        u.unpack( p, target );

        assertEquals( "example.jar", p.virtualPath );
    }


    private class Processor implements ExploderFileProcessor
    {
        private String search;

        String virtualPath;

        Processor( String s )
        {
            search = s;
        }

        @Override
        public void processFile( File baseDir, File file ) throws InternalException
        {
            System.out.println( "### Processing file " + file + " with baseDir " + baseDir + " and virtual path is " + getVirtualPath( baseDir, file ) );
            if ( file.getName().endsWith( search ) )
            {
                virtualPath = getVirtualPath( baseDir, file );
            }
        }
    }


    private class ProcessorFails implements ExploderFileProcessor
    {
        @Override
        public void processFile( File baseDir, File file ) throws InternalException
        {
            if ( file.getName().endsWith( ".class" ) )
            {
                if ( "Exploder.class".equals( file.getName() ) )
                {
                    throw new InternalException( "Unable to process file " );
                }
            }
        }
    }
}