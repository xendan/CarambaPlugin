package com.bics.caramba.plugin.search;

import com.intellij.ide.DataManager;
import com.intellij.openapi.actionSystem.DataKeys;
import com.intellij.openapi.project.Project;
import com.intellij.psi.*;
import com.intellij.psi.search.PsiNonJavaFileReferenceProcessor;
import com.intellij.psi.search.PsiSearchHelper;
import com.intellij.psi.search.searches.ClassInheritorsSearch;
import com.intellij.psi.xml.XmlAttribute;
import com.intellij.psi.xml.XmlTag;

import java.util.ArrayList;
import java.util.List;

/**
 * User: id967161
 * Date: 27/06/13
 */
public class CarambaUtils {
    public static boolean isCarambaComponent(PsiClass psiClass) {
        return psiClass != null &&
                (hasCarambaSuperClass(psiClass)
                        || isCarambaComponent(psiClass.getSuperClass()));
    }

    private static boolean hasCarambaSuperClass(PsiClass psiClass) {
        for (PsiClassType type : psiClass.getExtendsListTypes()) {
            String name = type.getCanonicalText();
            if (name.equals("org.caramba.components.Page")) {
                return true;
            }
        }
        for (PsiClassType type : psiClass.getImplementsListTypes()) {
            String name = type.getCanonicalText();
            if (name.equals("org.caramba.components.Component")) {
                return true;
            }
        }
        return false;
    }

    public static List<PsiElement> findXmlReferences(PsiClass psiClass, String fieldName) {
        final List<PsiElement> tags = new ArrayList<PsiElement>();
        List<String> names = getFamilyNames(psiClass);
        for (String className : names) {
            String carambaFileName = getCarambaFileName(className, psiClass.getProject(), true);
            if (carambaFileName != null) {
                PsiSearchHelper.SERVICE.getInstance(psiClass.getProject())
                        .processUsagesInNonJavaFiles(fieldName,
                                new CarambaFilePsiNonJavaFileReferenceProcessor(tags),
                                new ComponentSearchScope(carambaFileName, psiClass.getProject()));
            }
        }
        return tags;
    }

    public static Project getProejct() {
        return DataKeys.PROJECT.getData(DataManager.getInstance().getDataContext());
    }

    private static class CarambaConfigPsiNonJavaFileReferenceProcessor implements PsiNonJavaFileReferenceProcessor {
        private final boolean className;
        private final String[] name;

        public CarambaConfigPsiNonJavaFileReferenceProcessor(boolean className, String[] name) {
            this.className = className;
            this.name = name;
        }

        public boolean process(final PsiFile file, final int startOffset, final int endOffset) {
            PsiElement element = file.findElementAt(startOffset);
            XmlTag parent = findParentTag(element, "class");
            if (parent != null) {
                String attrName = (className) ? "resource" : "class";
                XmlAttribute attribute = parent.getAttribute(attrName);

                if (attribute != null) {
                    name[0] = attribute.getValue();
                }
            }
            return true;
        }
    }

    public static XmlTag findParentTag(PsiElement element, String attribute) {
        if (element.getParent() == null) {
            return null;
        }
        if (element.getParent() instanceof XmlTag) {
            XmlTag tag = (XmlTag) element.getParent();
            if (tag.getAttribute(attribute) != null) {
                return tag;
            }
        }
        return findParentTag(element.getParent(), attribute);
    }

    public static String getCarambaFileName(String className, Project project, final boolean isClassName) {
        final String[] name = {""};
        PsiSearchHelper.SERVICE.getInstance(project).processUsagesInNonJavaFiles(className,
                new CarambaConfigPsiNonJavaFileReferenceProcessor(isClassName, name), new ConfigSearchScope(project));
        if (name[0] == null || name[0].equals("")) {
            return null;
        }
        return name[0];
    }

    private static List<String> getFamilyNames(PsiClass psiClass) {
        List<String> names = new ArrayList<String>();
        names.add(psiClass.getQualifiedName());
        for (PsiClass childClass : ClassInheritorsSearch.search(psiClass)) {
            names.add(childClass.getQualifiedName());
        }
        return names;
    }

    private static class CarambaFilePsiNonJavaFileReferenceProcessor implements PsiNonJavaFileReferenceProcessor {
        private final List<PsiElement> tags;

        public CarambaFilePsiNonJavaFileReferenceProcessor(List<PsiElement> tags) {
            this.tags = tags;
        }

        public boolean process(final PsiFile file, final int startOffset, final int endOffset) {
            PsiReference reference = file.findReferenceAt(startOffset);
            if (reference != null) {
                tags.add(reference.getElement());
            }
            return true;
        }
    }
}
