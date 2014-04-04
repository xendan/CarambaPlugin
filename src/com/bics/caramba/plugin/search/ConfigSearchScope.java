package com.bics.caramba.plugin.search;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;

/**
 * User: id967161
 * Date: 15/11/12
 */
public class ConfigSearchScope extends FileNameSearchScope {

    public static final String CARAMBA_CONFIG_XML = "caramba-config.xml";

    public ConfigSearchScope(Project project) {
        super(project);
    }

    @Override
    public boolean contains(VirtualFile file) {
        return super.contains(file) && file.getName().equals(CARAMBA_CONFIG_XML);
    }
}
