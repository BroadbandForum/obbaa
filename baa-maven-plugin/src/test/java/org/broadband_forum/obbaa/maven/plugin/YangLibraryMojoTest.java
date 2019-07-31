/*
 * Copyright 2018 Broadband Forum
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.broadband_forum.obbaa.maven.plugin;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.maven.model.Build;
import org.apache.maven.model.Model;
import org.apache.maven.model.Resource;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.project.MavenProject;
import org.broadband_forum.obbaa.device.adapter.AdapterSpecificConstants;
import org.codehaus.plexus.PlexusTestCase;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class YangLibraryMojoTest  extends PlexusTestCase {
    private static final String RESOURCEDIR = getBasedir() + "/src/test/resources";
    private static final String TARGETDIR = getBasedir() + "/target/unittest-generated";

    @Before
    protected void setUp() throws Exception {
        super.setUp();
        deleteDirectories();
    }

    @Test
    public void testExecute() throws Exception {
        YangLibraryMojo mojo = setupMojo();
        mojo.execute();
        checkDeviationAndFeatureFiles();
    }

    @Test
    public void testExecuteForMissingDeviationModuleinYangLib() throws Exception {
        YangLibraryMojo mojo = setupMojo();
        ClassLoader cl = Thread.currentThread().getContextClassLoader();
        String yangLibFile = cl.getResource("model/deviationmissing-yang-library.xml").getFile();
        if (System.getProperty("os.name").startsWith("Windows")) {
            yangLibFile = yangLibFile.substring(1); //for windows adapt
        }
        mojo.setEnableForTesting(true);
        mojo.setYangLibFilePath(yangLibFile);
        try {
            mojo.execute();
            fail("Expected an exception here");
        }catch(MojoExecutionException e){
            assertEquals("Following Deviation modules dummy-netconf-server-dev@2017-03-27 are not listed as a module in the yang-library module list", e.getMessage());
        }
    }

    @After
    public void tearDown() throws Exception {
        deleteDirectories();
    }

    private YangLibraryMojo setupMojo() throws Exception {
        YangLibraryMojo mojo = new YangLibraryMojo();

        Model model = new Model();
        Build build = new Build();
        build.setOutputDirectory(TARGETDIR);
        Resource resource = mock(Resource.class);
        when(resource.getDirectory()).thenReturn(RESOURCEDIR);
        build.addResource(resource);
        model.setBuild(build);
        MavenProject project = new MavenProject(model);

        mojo.setProject(project);
        mojo.setOutputDir(TARGETDIR);

        return mojo;
    }

    private void checkDeviationAndFeatureFiles() throws IOException {
        File featuresFile = new File(TARGETDIR, AdapterSpecificConstants.SUPPORTED_FEATURES_FILE);
        List<String> lines = FileUtils.readLines(featuresFile);
        assertEquals(2, lines.size());
        assertTrue(lines.contains("(urn:ietf:params:xml:ns:yang:ietf-netconf-server?revision=2016-03-16)tls-listen"));
        assertTrue(lines.contains("(urn:ietf:params:xml:ns:yang:ietf-netconf-server?revision=2016-03-16)tls-call-home"));
        File deviationsFile = new File(TARGETDIR, AdapterSpecificConstants.SUPPORTED_DEVIATIONS_FILE);
        lines = FileUtils.readLines(deviationsFile);
        assertEquals(1, lines.size());
        assertEquals("(urn:ietf:params:xml:ns:yang:ietf-netconf-server?revision=2016-03-16)ietf-netconf-server,"
                        + "(urn:xxxxx-org:yang:netconf-server-dev?revision=2017-03-27)dummy-netconf-server-dev",
                lines.get(0));
    }

    private void deleteDirectories() throws Exception{
        File dir1 = new File(TARGETDIR);
        FileUtils.deleteDirectory(dir1);
    }
}
