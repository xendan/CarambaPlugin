package com.bics.caramba.plugin.search;

import com.intellij.codeInsight.completion.util.PsiTypeCanonicalLookupElement;
import com.intellij.ide.DataManager;
import com.intellij.openapi.actionSystem.DataKeys;
import com.intellij.openapi.project.Project;
import com.intellij.psi.*;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.search.searches.ClassInheritorsSearch;
import com.intellij.psi.xml.XmlTag;
import com.intellij.util.PsiNavigateUtil;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * User: id967161
 * Date: 24/06/13
 */
public class ComponentResolver extends Thread {

    private final TreeNode treeNode;
    private final String selected;

    public ComponentResolver(List<String> ids, String selected) {
        treeNode = new TreeNode("root");
        for (String id : ids) {
            treeNode.put(id.split("\\."));
        }
        this.selected = selected;
    }

    @Override
    public void run() {
        Collection<PsiClass> allPages = getAllPages();
        PsiClass page = findPage(allPages);
        final PsiElement jumpElement = findJumpElement(page);
        PsiNavigateUtil.navigate(jumpElement);

    }

    private Collection<PsiClass> getAllPages() {
        Project project = CarambaUtils.getProejct();
        PsiClass pageClass = JavaPsiFacade.getInstance(project).findClass("org.caramba.components.Page", GlobalSearchScope.allScope(project));
        return ClassInheritorsSearch.search(pageClass, true).findAll();
    }

    private PsiElement findJumpElement(PsiClass psiClass) {
        String[] parts = selected.split("\\.");
        PsiClass elementClass = psiClass;
        PsiField field = null;
        for (String part : parts) {
            field = findFieldByName(part, elementClass);
            elementClass = getFieldClass(field);
        }
        return field;
    }

    private PsiClass getFieldClass(PsiField field) {
        return new PsiTypeCanonicalLookupElement(field.getType()).getPsiClass();
    }

    private PsiField findFieldByName(String part, PsiClass elementClass) {
        for (PsiField psiField : elementClass.getFields()) {
            if (psiField.getName().equals(part)) {
                return psiField;
            }
        }
        return null;
    }

    private PsiClass findPage(Collection<PsiClass> pages) {
        for (PsiClass page : pages) {
            if (pageMatch(page)) {
                return page;
            }
        }
        return null;
    }

    private boolean pageMatch(PsiClass page) {
        List<PsiField> components = getCarambaFields(page.getFields());
        if (components.isEmpty()) {
            return false;
        }
        List<String> names = getNames(treeNode.getChildren());
        for (PsiField component : components) {
            if (!names.contains(component.getName()) && !isInTable(page, component)) {
                return false;
            }
        }
        return true;
    }

    private boolean isInTable(PsiClass page, PsiField component) {
        List<PsiElement> refs = CarambaUtils.findXmlReferences(page, component.getName());
        for (PsiElement ref : refs) {
            if (hasTableParent(ref))  {
                return true;
            }
        }
        return false;
    }

    private boolean hasTableParent(PsiElement ref) {
        if (ref.getParent() == null) {
            return false;
        }
        if (ref.getParent() instanceof XmlTag) {
            if (((XmlTag)ref.getParent()).getName().toLowerCase().contains("table")) {
                return true;
            }
        }

        return hasTableParent(ref.getParent());
    }

    private List<String> getNames(List<TreeNode> children) {
        List<String> names = new ArrayList<String>();
        for (TreeNode child : children) {
            names.add(child.getName());
        }
        return names;
    }

    private List<PsiField> getCarambaFields(PsiField[] fields) {
        List<PsiField> components = new ArrayList<PsiField>();
        for (PsiField field : fields) {
            PsiClass fieldClass = getFieldClass(field);
            if (CarambaUtils.isCarambaComponent(fieldClass)) {
                components.add(field);
            }
        }
        return components;
    }


}
