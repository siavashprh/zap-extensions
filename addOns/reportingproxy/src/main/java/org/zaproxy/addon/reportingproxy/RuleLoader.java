/*
 * Zed Attack Proxy (ZAP) and its related class files.
 *
 * ZAP is an HTTP/HTTPS proxy for assessing web application security.
 *
 * Copyright 2025 The ZAP Development Team
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
package org.zaproxy.addon.reportingproxy;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Modifier;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Loads rules from a JAR file.
 */
public class RuleLoader {

    private static final Logger LOGGER = LogManager.getLogger(RuleLoader.class);

    /**
     * Loads rules from a JAR file.
     * 
     * @param jarFile The JAR file to load rules from.
     * @return A list of rules loaded from the JAR file.
     * @throws IOException If an I/O error occurs.
     */
    public List<ReportingRule> loadRules(File jarFile) throws IOException {
        List<ReportingRule> rules = new ArrayList<>();

        if (!jarFile.exists() || !jarFile.getName().endsWith(".jar")) {
            throw new IOException("Invalid JAR file: " + jarFile.getAbsolutePath());
        }

        URL[] urls = {jarFile.toURI().toURL()};
        try (URLClassLoader cl =
                URLClassLoader.newInstance(urls, this.getClass().getClassLoader())) {
            try (JarFile jar = new JarFile(jarFile)) {
                Enumeration<JarEntry> entries = jar.entries();
                while (entries.hasMoreElements()) {
                    JarEntry entry = entries.nextElement();
                    if (entry.isDirectory() || !entry.getName().endsWith(".class")) {
                        continue;
                    }

                    // Convert path to class name
                    String className =
                            entry.getName()
                                    .substring(0, entry.getName().length() - 6)
                                    .replace('/', '.');

                    try {
                        Class<?> c = cl.loadClass(className);
                        if (ReportingRule.class.isAssignableFrom(c)
                                && !c.isInterface()
                                && !Modifier.isAbstract(c.getModifiers())) {
                            ReportingRule rule =
                                    (ReportingRule) c.getDeclaredConstructor().newInstance();
                            rules.add(rule);
                        }
                    } catch (NoClassDefFoundError | ClassNotFoundException e) {
                        // Ignore classes that cannot be loaded or are not relevant
                    } catch (Throwable e) {
                        LOGGER.error("Failed to load rule class: {}", className, e);
                    }
                }
            }
        }

        return rules;
    }
}
