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

import static org.junit.Assert.assertTrue;

public class ArchivesTest
{
    private static final File RESOURCES_DIR = new File("src/test/resources");

    @Rule
    public SystemOutRule output = new SystemOutRule().muteForSuccessfulTests();

    @Rule
    public TemporaryFolder folder = new TemporaryFolder();

    @Test
    public void testUnpackWithTemporary() throws IOException, InternalException
    {
        File target = new File (RESOURCES_DIR, "archive.7z" );
        File temporaryFolder = folder.newFolder();

        Exploder u = new Exploder().useWorkingDirectory( temporaryFolder );

        u.unpack( null, target );

        File verify = new File( temporaryFolder, target.getName() + Exploder.ARCHIVE_UNPACK_SUFFIX );
        assertTrue( verify.exists() );

        assertTrue ( new File ( verify.toString() + "/folder" ).exists() );
        assertTrue ( new File ( verify.toString() + "/folder/sample.txt" ).exists() );
    }
}