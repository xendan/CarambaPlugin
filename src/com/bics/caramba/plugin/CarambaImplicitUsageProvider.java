package com.bics.caramba.plugin;

import com.bics.caramba.plugin.search.CarambaUtils;
import com.intellij.codeInsight.daemon.ImplicitUsageProvider;
import com.intellij.navigation.GotoRelatedProvider;
import com.intellij.psi.*;
import com.intellij.psi.util.PsiTreeUtil;

import java.util.HashMap;
import java.util.Map;

/**
 * User: id967161
 * Date: 11/12/12
 */
public class CarambaImplicitUsageProvider implements ImplicitUsageProvider {
    
    private Map<String, Record> foundFields = new HashMap<String, Record>();
    private GotoRelatedProvider provider = new CarambaRelatedFilesProvider();
    private static final long HOUR = 1000 * 60 * 60;

    @Override
    public boolean isImplicitUsage(PsiElement element) {
        if (element instanceof PsiMethod) {
            PsiMethod method = (PsiMethod) element;
            return (isDefaultConstructor(method) && isParentCarambaComponent(element));
        }
        return false;
    }

    private boolean isParentCarambaComponent(PsiElement element) {
        return CarambaUtils.isCarambaComponent(findClass(element));
    }

    private boolean isDefaultConstructor(PsiMethod method) {
        return method.isConstructor() && method.getParameterList().getParametersCount() == 0;
    }

    @Override
    public boolean isImplicitRead(PsiElement element) {
        return false;
    }

    @Override
    public boolean isImplicitWrite(PsiElement element) {
        return element instanceof PsiField &&
                isParentCarambaComponent(element) &&
                getRecord((PsiField) element).isUsed;
    }

    private Record getRecord(PsiField field) {
        String name = getName(field);
        Record record = findOrCreateRecord(foundFields.get(name), field);
        foundFields.put(name, record);
        return record;
    }

    private Record findOrCreateRecord(Record record, PsiField field) {
        long time = System.currentTimeMillis();
        if (record == null || time - record.recordTime > HOUR )  {
            record = new Record();
            record.recordTime = time;
            record.isUsed = !provider.getItems(field).isEmpty();
        }
        return record;
    }

    private String getName(PsiField field) {
        PsiClass psiClass = findClass(field);
        return psiClass.getName() + "." + field.getName();
    }

    private PsiClass findClass(PsiElement element) {
        return PsiTreeUtil.getParentOfType(element, PsiClass.class, false);
    }
    
    private static class Record {
        boolean isUsed;
        long recordTime;
    }
}
