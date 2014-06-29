package gabilheri.cloudendpointtest;

import android.content.Context;
import android.util.Log;

import com.gabilheri.backend.todoApi.TodoApi;
import com.gabilheri.backend.todoApi.model.TodoBean;
import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.extensions.android.json.AndroidJsonFactory;
import com.google.api.client.util.DateTime;

import java.util.Calendar;
import java.util.List;

/**
 * @author Marcus Gabilheri
 * @version 1.0
 * @since 6/19/14
 */
public class SenderCloud  {

    private TodoApi cloudApiService = null;
    private String userEmail;
    private Context mContext;

    public SenderCloud(String userEmail, Context mContext) {
        this.userEmail = userEmail;
        this.mContext = mContext;
        TodoApi.Builder builder = new TodoApi.Builder(AndroidHttp.newCompatibleTransport(), new AndroidJsonFactory(), null);
        builder.setApplicationName("cloudApi");
        cloudApiService = builder.build();
    }

    public synchronized void pushToRemote(String title, String message, DateTime dueAt) {

        try {
            DateTime mDate = new DateTime(Calendar.getInstance().getTime());
            TodoBean mBean = new TodoBean();
            mBean.setTitle(title);
            mBean.setTodoMessage(message);
            mBean.setId((long) Math.floor(Math.random() * 1342141));
            mBean.setDueAt(dueAt);
            mBean.setCreatedAt(mDate);
            mBean.setUserEmail(userEmail);
            cloudApiService.storeTodo(mBean).execute();
            Log.i("SENDER: ", "Sending stuff!");
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public synchronized void pullFromRemote() {
        try {
            List<TodoBean> mBeanList = cloudApiService.getTodo().execute().getItems();
            if(mBeanList != null) {

                for(TodoBean mBean : mBeanList) {
                    Log.i("SENDER: ", "Bean: " + mBean.getTitle());
                }
            } else {
                Log.i("SENDER: ", "MY BEAN IS NULL!!");
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
