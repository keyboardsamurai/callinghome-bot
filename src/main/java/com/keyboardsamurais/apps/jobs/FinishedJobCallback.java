package com.keyboardsamurais.apps.jobs;

import com.keyboardsamurais.apps.db.model.ClassifiedItem;

import java.util.List;

/**
 * @author Antonio Agudo  on 28.01.23
 */
public interface FinishedJobCallback {
    void onFinished(List<ClassifiedItem> newItems);
}
