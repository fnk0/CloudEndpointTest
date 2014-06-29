package com.gabilheri.backend;

import com.google.api.server.spi.config.Api;
import com.google.api.server.spi.config.ApiMethod;
import com.google.api.server.spi.config.ApiNamespace;
import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.FetchOptions;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Transaction;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/** An endpoint class we are exposing */
@Api(name = "todoApi", version = "v1", namespace = @ApiNamespace(ownerDomain = "backend.gabilheri.com", ownerName = "backend.gabilheri.com", packagePath="com.gabilheri.backend"))
public class MyEndpoint {

    public static final String TODO_PARENT_KEY = "todoParentKey";
    public static final String TODO = "todo.txt";
    public static final String TODO_BEAN = "todoBean";
    public static final String TITLE = "title";
    public static final String TODO_MESSAGE = "todoMessage";
    public static final String CREATED_AT = "createdAt";
    public static final String DUE_AT = "dueAt";
    public static final String USER_EMAIL = "userEmail";

    @ApiMethod(name = "storeTodo")
    public void storeTodo(TodoBean todoBean) {
        DatastoreService datastoreService = DatastoreServiceFactory.getDatastoreService();
        Transaction txn = datastoreService.beginTransaction();
        try {

            Key taskBeanParentKey = KeyFactory.createKey(TODO_PARENT_KEY, TODO);
            Entity taskEntity = new Entity(TODO_BEAN, todoBean.getId(), taskBeanParentKey);
            taskEntity.setProperty(TITLE, todoBean.getTitle());
            taskEntity.setProperty(TODO_MESSAGE, todoBean.getTodoMessage());
            taskEntity.setProperty(USER_EMAIL, todoBean.getUserEmail());
            taskEntity.setProperty(CREATED_AT, todoBean.getCreatedAt());
            taskEntity.setProperty(DUE_AT, todoBean.getDueAt());
            datastoreService.put(taskEntity);
            txn.commit();
        } finally {
            if (txn.isActive()) {
                txn.rollback();
            }
        }
    }

    @ApiMethod(name = "getTodo")
    public List<TodoBean> getTodo() {
        DatastoreService datastoreService = DatastoreServiceFactory.getDatastoreService();
        Key taskBeanParentKey = KeyFactory.createKey(TODO_PARENT_KEY, TODO);
        Query query = new Query(taskBeanParentKey);
        List<Entity> results = datastoreService.prepare(query).asList(FetchOptions.Builder.withDefaults());

        ArrayList<TodoBean> todoBeans = new ArrayList<TodoBean>();
        for (Entity result : results) {
            TodoBean todoBean = new TodoBean();
            todoBean.setId(result.getKey().getId());
            todoBean.setTitle((String) result.getProperty(TITLE));
            todoBean.setTodoMessage((String) result.getProperty(TODO_MESSAGE));
            todoBean.setTitle((String) result.getProperty(USER_EMAIL));
            todoBean.setCreatedAt((Date) result.getProperty(CREATED_AT));
            todoBean.setDueAt((Date) result.getProperty(DUE_AT));
            todoBeans.add(todoBean);
        }
        return todoBeans;
    }

    @ApiMethod(name = "clearTodo")
    public void clearTodo() {
        DatastoreService datastoreService = DatastoreServiceFactory.getDatastoreService();
        Transaction txn = datastoreService.beginTransaction();
        try {
            Key taskBeanParentKey = KeyFactory.createKey(TODO_PARENT_KEY, TODO);
            Query query = new Query(taskBeanParentKey);
            List<Entity> results = datastoreService.prepare(query)
                    .asList(FetchOptions.Builder.withDefaults());
            for (Entity result : results) {
                datastoreService.delete(result.getKey());
            }
            txn.commit();
        } finally {
            if (txn.isActive()) { txn.rollback(); }
        }
    }

}