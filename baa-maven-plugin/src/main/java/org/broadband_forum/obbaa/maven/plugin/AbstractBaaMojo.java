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
import java.net.URL;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.maven.model.Resource;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.project.MavenProject;
import org.codehaus.classworlds.ClassRealm;
import org.codehaus.classworlds.ClassWorld;

public abstract class AbstractBaaMojo extends AbstractMojo {
    /**
     * <i>Maven Internal</i>: The Project descriptor.
     *
     * @parameter default-value="${project}"
     * @required
     * @readonly
     */
    protected MavenProject m_project;
    private List<URL> m_includeFiles = new ArrayList<URL>();

    public void setProject(MavenProject project) {
        this.m_project = project;
    }

    @SuppressWarnings("unchecked")
    protected void updateClasspath(MavenProject project) throws MojoExecutionException {
        try {
            Set<String> classPathElements = new HashSet<>();
            List<Resource> resources = project.getResources();
            for (Resource resource : resources) {
                List<String> includes = resource.getIncludes();
                if (includes != null & !includes.isEmpty()) {
                    for (String include : includes) {
                        classPathElements.add(resource.getDirectory() + File.separator + include);
                    }
                } else {
                    classPathElements.add(resource.getDirectory());
                }
            }

            extendPluginClasspath(classPathElements);
        } catch (Exception e) {
            getLog().error("Error while updating classpath", e);
        }
    }

    private void extendPluginClasspath(Set<String> classPathElements) throws MojoExecutionException {
        ClassWorld world = new ClassWorld();
        ClassRealm realm;
        try {
            realm = world.newRealm("maven.plugin." + getClass().getSimpleName(), Thread.currentThread().getContextClassLoader());

            for (String element : classPathElements) {
                File elementFile = new File(element);
                getLog().debug("Adding element to plugin classpath: " + elementFile.getPath());
                URL url = new URL("file:///" + elementFile.getPath() + (elementFile.isDirectory() ? "/" : ""));
                realm.addConstituent(url);
                m_includeFiles.add(url);
            }
        } catch (Exception ex) {
            throw new MojoExecutionException(ex.toString(), ex);
        }
        Thread.currentThread().setContextClassLoader(realm.getClassLoader());
    }

    public List<URL> getResources() {
        return m_includeFiles;
    }


}
