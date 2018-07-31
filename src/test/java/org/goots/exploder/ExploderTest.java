package org.goots.exploder;

import org.apache.commons.io.FileUtils;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.net.URL;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class ExploderTest
{
    private final Logger logger = LoggerFactory.getLogger( getClass() );

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

        File target = new File (folder.getRoot(), "sourceclear.jar" );
        URL source = new URL ( "http://central.maven.org/maven2/com/srcclr/srcclr-maven-plugin/3.0.0/srcclr-maven-plugin-3.0.0.jar");
        FileUtils.copyURLToFile( source, target);

        u.unpack( target );

        File verify = new File( target.getParentFile(), target.getName() + Exploder.ARCHIVE_UNPACK_SUFFIX );
        assertTrue( verify.exists() );

        assertTrue ( new File ( verify.toString() + "/io/takari/maven-wrapper-0.1.4.tar.gz" ).exists() );
        assertTrue ( new File ( verify.toString() + "/io/takari/maven-wrapper-0.1.4.tar" ).exists() );
        assertTrue ( new File ( verify.toString() + "/io/takari/maven-wrapper-0.1.4.tar" + Exploder.ARCHIVE_UNPACK_SUFFIX).exists() );
    }

    @Test
    public void testUnpackWithTemporary() throws IOException, InternalException
    {
        Exploder u = new Exploder();

        File target = new File (folder.getRoot(), "sourceclear.jar" );
        URL source = new URL ( "http://central.maven.org/maven2/com/srcclr/srcclr-maven-plugin/3.0.0/srcclr-maven-plugin-3.0.0.jar");
        FileUtils.copyURLToFile( source, target);
        File temporaryFolder = folder.newFolder();

        u.unpack( null, temporaryFolder, target );

        File verify = new File( temporaryFolder, target.getName() + Exploder.ARCHIVE_UNPACK_SUFFIX );
        assertTrue( verify.exists() );

        assertTrue ( new File ( verify.toString() + "/io/takari/maven-wrapper-0.1.4.tar.gz" ).exists() );
        assertTrue ( new File ( verify.toString() + "/io/takari/maven-wrapper-0.1.4.tar" ).exists() );
        assertTrue ( new File ( verify.toString() + "/io/takari/maven-wrapper-0.1.4.tar" + Exploder.ARCHIVE_UNPACK_SUFFIX).exists() );
    }


    @Test
    public void testNoUnpackWithExcludes() throws IOException, InternalException
    {
        Exploder u = new Exploder().excludeSuffix( "jaR" );

        File target = new File (folder.getRoot(), "sourceclear.jar" );
        URL source = new URL ( "http://central.maven.org/maven2/com/srcclr/srcclr-maven-plugin/3.0.0/srcclr-maven-plugin-3.0.0.jar");
        FileUtils.copyURLToFile( source, target);

        u.unpack( target );

        File verify = new File( target.getParentFile(), target.getName() + Exploder.ARCHIVE_UNPACK_SUFFIX );
        assertFalse( verify.exists() );
    }

}