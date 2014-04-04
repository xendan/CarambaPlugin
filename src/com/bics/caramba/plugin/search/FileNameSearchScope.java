package com.bics.caramba.plugin.search;

import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ProjectFileIndex;
import com.intellij.openapi.roots.ProjectRootManager;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.search.GlobalSearchScope;
import org.jetbrains.annotations.NotNull;

/**
 * User: id967161
 * Date: 15/11/12
 */
public class FileNameSearchScope extends GlobalSearchScope {
    private final ProjectFileIndex myFileIndex;

    public FileNameSearchScope(Project project) {
        myFileIndex = ProjectRootManager.getInstance(project).getFileIndex();
    }

    @Override
    public boolean contains(VirtualFile file) {
        return myFileIndex.isInContent(file);
    }

    @Override
    public int compare(VirtualFile virtualFile, VirtualFile virtualFile1) {
        return 0;
    }

    @Override
    public boolean isSearchInModuleContent(@NotNull Module module) {
        return true;
    }

    @Override
    public boolean isSearchInLibraries() {
        return false;
    }
}
