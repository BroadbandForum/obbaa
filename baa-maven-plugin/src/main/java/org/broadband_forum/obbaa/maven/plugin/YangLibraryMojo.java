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

import java.io.File;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.broadband_forum.obbaa.device.adapter.AdapterSpecificConstants;
import org.broadband_forum.obbaa.device.adapter.util.VariantFileUtils;

/**
 * Goal which process the yang-library.xml and generates supported_features.xml & supported_deviations.xml
 *
 * @goal process-yang-library
 * @phase process-resources
 * @requiresDependencyResolution compile+runtime
 */
public class YangLibraryMojo extends AbstractBaaMojo {

    /**
     * Location of the output files.
     *
     * @parameter default-value="${project.build.directory}/generated-resources/model"
     * @required
     */
    private String m_outputDir;

    public void setOutputDir(String outputDir) {
        m_outputDir = outputDir;
    }

    private boolean m_enableForTesting;

    public void setEnableForTesting(boolean value) {
        m_enableForTesting = value;
    }

    private String m_yangLibTestFile;

    public String getYangLibFilePath() {
        return m_yangLibTestFile;
    }

    public void setYangLibFilePath(String yangLib) {
        m_yangLibTestFile = yangLib;
    }

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        updateClasspath(m_project);
        String yangLibFile;
        if (m_enableForTesting && m_yangLibTestFile != null) {
            yangLibFile = m_yangLibTestFile;
        } else {
            String yangLibraryFile = AdapterSpecificConstants.YANG_LIB_FILE;
            ClassLoader cl = Thread.currentThread().getContextClassLoader();
            yangLibFile = cl.getResource(yangLibraryFile).getFile();
        }
        File modelDir = new File(m_outputDir);
        modelDir.mkdirs();
        try {
            VariantFileUtils.copyYangLibFiles(yangLibFile, m_outputDir);
        } catch (Exception e) {
            throw new MojoExecutionException(e.getMessage(), e);
        }
    }
}
