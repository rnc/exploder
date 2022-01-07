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

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.reflect.FieldUtils;
import org.junit.Rule;
import org.junit.Test;
import org.junit.contrib.java.lang.system.SystemOutRule;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.io.IOException;
import java.net.URL;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class ExploderTest
{
    @Rule
    public SystemOutRule output = new SystemOutRule().muteForSuccessfulTests();

    @Rule
    public TemporaryFolder folder = new TemporaryFolder();

    @Test
    public void testSuffix() throws InternalException
    {
        Exploder u = new Exploder();
        u.excludeSuffix( "jar" );
    }

    @Test( expected = InternalException.class )
    public void testBadSuffix() throws InternalException
    {
        Exploder u = new Exploder();
        u.excludeSuffix( "wierd" );
    }

    @Test
    public void testUnpack() throws IOException, InternalException
    {
        Exploder u = new Exploder();

        File target = new File( folder.getRoot(), "sourceclear.jar" );
        URL source = new URL( "https://repo1.maven.org/maven2/com/srcclr/srcclr-maven-plugin/3.0.0/srcclr-maven-plugin-3.0.0.jar" );
        FileUtils.copyURLToFile( source, target );

        u.unpack( target );

        File verify = new File( target.getParentFile(), target.getName() + Exploder.ARCHIVE_UNPACK_SUFFIX );
        assertTrue( verify.exists() );

        assertTrue( new File( verify.toString() + "/io/takari/maven-wrapper-0.1.4.tar.gz" ).exists() );
        assertTrue( new File( verify.toString() + "/io/takari/maven-wrapper-0.1.4.tar" ).exists() );
        assertTrue( new File( verify.toString() + "/io/takari/maven-wrapper-0.1.4.tar"
                                              + Exploder.ARCHIVE_UNPACK_SUFFIX ).exists() );
    }

    @Test(expected = InternalException.class)
    public void testUnpackWithWorkingAndTemp() throws IOException, InternalException
    {
        new Exploder().useTargetDirectory( folder.newFolder() ).useTemporaryDirectory();
    }

    @Test
    public void testUnpackWithWorking() throws IOException, InternalException
    {
        File target = new File( folder.getRoot(), "sourceclear.jar" );
        URL source = new URL( "https://repo1.maven.org/maven2/com/srcclr/srcclr-maven-plugin/3.0.0/srcclr-maven-plugin-3.0.0.jar" );
        FileUtils.copyURLToFile( source, target );
        File temporaryFolder = folder.newFolder();

        Exploder u = new Exploder().useTargetDirectory( temporaryFolder );

        u.unpack( null, target );

        assertTrue( temporaryFolder.exists() );
        assertTrue( new File( temporaryFolder.toString() + "/io/takari/maven-wrapper-0.1.4.tar.gz" ).exists() );
        assertTrue( new File( temporaryFolder.toString() + "/io/takari/maven-wrapper-0.1.4.tar" ).exists() );
        assertTrue( new File( temporaryFolder.toString() + "/io/takari/maven-wrapper-0.1.4.tar"
                                              + Exploder.ARCHIVE_UNPACK_SUFFIX ).exists() );
    }

    @Test
    public void testNoUnpackWithExcludes() throws IOException, InternalException
    {
        Exploder u = new Exploder().excludeSuffix( "jaR" );

        File target = new File( folder.getRoot(), "sourceclear.jar" );
        URL source = new URL( "https://repo1.maven.org/maven2/com/srcclr/srcclr-maven-plugin/3.0.0/srcclr-maven-plugin-3.0.0.jar" );
        FileUtils.copyURLToFile( source, target );

        u.unpack( target );

        File verify = new File( target.getParentFile(), target.getName() + Exploder.ARCHIVE_UNPACK_SUFFIX );
        assertFalse( verify.exists() );
    }

    @Test
    public void testUnpackWithTemporary() throws IOException, InternalException, IllegalAccessException
    {
        File target = new File( folder.getRoot(), "sourceclear.jar" );
        URL source = new URL( "https://repo1.maven.org/maven2/com/srcclr/srcclr-maven-plugin/3.0.0/srcclr-maven-plugin-3.0.0.jar" );
        FileUtils.copyURLToFile( source, target );

        Exploder u = new Exploder().useTemporaryDirectory();

        File temporary = (File) FieldUtils.readField( u, "targetDirectory", true );
        Boolean cleanup = (Boolean) FieldUtils.readField( u, "cleanup", true );

        assertTrue( temporary.exists() );
        assertTrue( cleanup );

        u.unpack( null, target );

        assertFalse( temporary.exists() );
    }

    @Test
    public void testUnpackWithURL() throws IOException, InternalException, IllegalAccessException
    {
        URL source = new URL( "https://repo1.maven.org/maven2/com/srcclr/srcclr-maven-plugin/3.0.0/srcclr-maven-plugin-3.0.0.jar" );

        Exploder u = new Exploder().useTemporaryDirectory();

        File temporary = (File) FieldUtils.readField( u, "targetDirectory", true );
        Boolean cleanup = (Boolean) FieldUtils.readField( u, "cleanup", true );
        Processor p = new Processor();

        assertTrue( temporary.exists() );
        assertTrue( cleanup );

        u.unpack( p, source );

        assertTrue( p.found );
        assertFalse( temporary.exists() );
    }

    @Test
    public void testUnpackWithURLAndTemporary() throws IOException, InternalException
    {
        URL source = new URL( "https://repo1.maven.org/maven2/com/srcclr/srcclr-maven-plugin/3.0.0/srcclr-maven-plugin-3.0.0.jar" );

        Exploder u = new Exploder();
        Processor p = new Processor();

        u.unpack( p, source );

        assertTrue( p.found );
    }

    @Test
    public void testUnpackWithFileURL() throws IOException, InternalException, IllegalAccessException
    {
        URL source = new URL( "https://repo1.maven.org/maven2/com/srcclr/srcclr-maven-plugin/3.0.0/srcclr-maven-plugin-3.0.0.jar" );
        File target = new File( folder.newFolder(), "sourceclear.jar" );
        FileUtils.copyURLToFile( source, target );

        Exploder u = new Exploder();

        Boolean cleanup = (Boolean) FieldUtils.readField( u, "cleanup", true );
        assertFalse( cleanup );

        u.useTargetDirectory( folder.newFolder());
        File temporary = (File) FieldUtils.readField( u, "targetDirectory", true );
        cleanup = (Boolean) FieldUtils.readField( u, "cleanup", true );
        assertFalse( cleanup );
        assertTrue( temporary.exists() );

        u.unpack( null, target.toURI().toURL() );

        assertTrue( temporary.exists() );
    }

    private class Processor implements ExploderFileProcessor
    {
        boolean found;

        @Override
        public void processFile( File baseDir, File file )
        {
            if ( file.getName().endsWith( "maven-wrapper-0.1.4.tar.gz" ) )
            {
                found = true;
            }
        }
    }

    @Test
    public void testUnpackWithURLAndWorking() throws IOException, InternalException
    {
        File targetDir = folder.newFolder();
        Exploder u = new Exploder().useTargetDirectory( targetDir );
        URL source = new URL( "https://repo1.maven.org/maven2/com/srcclr/srcclr-maven-plugin/3.0.0/srcclr-maven-plugin-3.0.0.jar" );

        u.unpack( null, source );

        assertTrue( targetDir.exists() );

        assertTrue( new File( targetDir, "/io/takari/maven-wrapper-0.1.4.tar.gz" ).exists() );
        assertTrue( new File( targetDir, "/io/takari/maven-wrapper-0.1.4.tar" ).exists() );
        assertTrue( new File( targetDir, "/io/takari/maven-wrapper-0.1.4.tar"
                                              + Exploder.ARCHIVE_UNPACK_SUFFIX ).exists() );
    }

    @Test
    public void testUnpackWithURLAndWorkingNoRecurse() throws IOException, InternalException
    {
        File targetDir = folder.newFolder();
        Exploder u = new Exploder().useTargetDirectory( targetDir ).disableRecursion();
        URL source = new URL( "https://repo1.maven.org/maven2/com/srcclr/srcclr-maven-plugin/3.0.0/srcclr-maven-plugin-3.0.0.jar" );

        u.unpack( null, source );

        assertTrue( targetDir.exists() );
        assertTrue( new File( targetDir, "/io/takari/maven-wrapper-0.1.4.tar.gz" ).exists() );
        assertFalse( new File( targetDir, "/io/takari/maven-wrapper-0.1.4.tar" ).exists() );
        assertFalse( new File( targetDir, "/io/takari/maven-wrapper-0.1.4.tar" + Exploder.ARCHIVE_UNPACK_SUFFIX ).exists() );
    }
}
