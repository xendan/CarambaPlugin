package com.bics.caramba.plugin;

import com.bics.caramba.plugin.search.CarambaUtils;
import com.intellij.navigation.GotoRelatedItem;
import com.intellij.navigation.GotoRelatedProvider;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.*;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.psi.xml.XmlAttribute;
import com.intellij.psi.xml.XmlTag;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * User: id967161
 * Date: 15/11/12
 */
public class CarambaRelatedFilesProvider extends GotoRelatedProvider {

    public static final String ID = "id";
    public static final String WEBROOT = "/webapp";

    @NotNull
    @Override
    public List<? extends GotoRelatedItem> getItems(@NotNull PsiElement context) {
        List<GotoRelatedItem> items = new ArrayList<GotoRelatedItem>();
        if (context instanceof PsiIdentifier || context instanceof PsiField) {
            String name = getFieldName(context);
            PsiClass psiClass = PsiTreeUtil.getParentOfType(context, PsiClass.class, false);
            if (psiClass != null) {
                items.addAll(createJavaItem(psiClass, name));
            }
        } else {
            XmlTag tag = CarambaUtils.findParentTag(context, ID);
            if (tag != null) {
                XmlAttribute id = tag.getAttribute(ID);
                VirtualFile virtualFile = tag.getContainingFile().getVirtualFile();
                if (virtualFile != null) {
                    String path = virtualFile.getPath();
                    int start = path.indexOf(WEBROOT);
                    if (start > 0 && id != null) {
                        items.addAll(createXmlItems(id.getValue(),
                                path.substring(start + WEBROOT.length()), tag.getProject()));
                    }
                }
            }
        }
        return items;

    }

    private String getFieldName(PsiElement context) {
        if (context instanceof PsiIdentifier) {
            PsiIdentifier identifier = (PsiIdentifier) context;
            return identifier.getText();
        }
        return ((PsiField) context).getName();
    }

    private Collection<GotoRelatedItem> createXmlItems(String id, String carambaPath, Project project) {
        List<GotoRelatedItem> tags = new ArrayList<GotoRelatedItem>();
        String fullName = CarambaUtils.getCarambaFileName(carambaPath, project, false);
        for (PsiClass eachClass : getAncestors(project, fullName)) {
            PsiField field = findField(eachClass.getFields(), id);
            if (field != null) {
                tags.add(new GotoRelatedItem(field));
            }
        }
        return tags;
    }

    private List<PsiClass> getAncestors(Project project, String fullName) {
        List<PsiClass> ancestors = new ArrayList<PsiClass>();
        for (PsiClass psiClass : JavaPsiFacade.getInstance(project).findClasses(fullName, GlobalSearchScope.projectScope(project))) {
            fillAncesstorsList(psiClass, ancestors);
        }
        return ancestors;
    }

    private void fillAncesstorsList(PsiClass psiClass, List<PsiClass> ancestors) {
        if (psiClass != null) {
            ancestors.add(psiClass);
            fillAncesstorsList(psiClass.getSuperClass(), ancestors);
        }
    }

    private PsiField findField(PsiField[] fields, String id) {
        for (PsiField field : fields) {
            if (field.getName().equals(id)) {
                return field;
            }
        }
        return null;
    }


    private Collection<GotoRelatedItem> createJavaItem(PsiClass psiClass, String fieldName) {
        return toItems(CarambaUtils.findXmlReferences(psiClass, fieldName));
    }

    private Collection<GotoRelatedItem> toItems(List<PsiElement> tags) {
        List<GotoRelatedItem> items = new ArrayList<GotoRelatedItem>();
        for (PsiElement element : tags) {
            items.add(new GotoRelatedItem(element));
        }
        return items;
    }

}
