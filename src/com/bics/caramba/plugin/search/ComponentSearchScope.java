package com.bics.caramba.plugin.search;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;

/**
 * User: id967161
 * Date: 15/11/12
 */
public class ComponentSearchScope extends FileNameSearchScope {
    private String fullFileName;

    public ComponentSearchScope(String fileName, Project project) {
        super(project);
        this.fullFileName = fileName;
    }

    @Override
    public boolean contains(VirtualFile file) {
        return super.contains(file) && fullFileName.endsWith(file.getName());
    }
}
