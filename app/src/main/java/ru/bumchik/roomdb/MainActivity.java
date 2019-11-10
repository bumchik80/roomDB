package ru.bumchik.roomdb;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import io.reactivex.Single;
import io.reactivex.SingleEmitter;
import io.reactivex.SingleOnSubscribe;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.observers.DisposableSingleObserver;
import io.reactivex.schedulers.Schedulers;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class MainActivity extends AppCompatActivity implements View.OnClickListener{

    Button btnSaveAllRoom;
    Button btnSelectAllRoom;
    Button btnDeleteAllRoom;
    Button btnLoad;
    RestAPI restAPI;
    private ProgressBar progressBar;
    private TextView mInfoTextView;
    List<RetrofitModel> modelList = new ArrayList<>();
    DisposableSingleObserver<Bundle> dso;
    List<RoomModel> products;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initViews();
    }

    private void initViews(){
        btnSaveAllRoom = findViewById(R.id.btnSaveAllRoom);
        btnSelectAllRoom = findViewById(R.id.btnSelectAllRoom);
        btnDeleteAllRoom = findViewById(R.id.btnDeleteAllRoom);
        btnSaveAllRoom.setOnClickListener(this);
        btnSelectAllRoom.setOnClickListener(this);
        btnDeleteAllRoom.setOnClickListener(this);
        progressBar = findViewById(R.id.progressBar);
        mInfoTextView = findViewById(R.id.tvLoad);
        btnLoad = findViewById(R.id.btnLoad);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btnLoad:
                mInfoTextView.setText("");
                Retrofit retrofit = null;
                try {
                    retrofit = new Retrofit.Builder()
                            .baseUrl("https://api.github.com/") // Обратить внимание на слеш в базовом адресе
                            .addConverterFactory(GsonConverterFactory.create())
                            .build();
                    restAPI = retrofit.create(RestAPI.class);
                } catch (Exception io) {
                    mInfoTextView.setText("no retrofit: " + io.getMessage());
                    return;
                }
                // Подготовили вызов на сервер
                Call<List<RetrofitModel>> call = restAPI.loadUsers();
                ConnectivityManager connectivityManager =
                        (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
                NetworkInfo networkinfo = connectivityManager.getActiveNetworkInfo();

                if (networkinfo != null && networkinfo.isConnected()) {
                    // Запускаем
                    try {
                        progressBar.setVisibility(View.VISIBLE);
                        downloadOneUrl(call);
                    } catch (IOException e) {
                        e.printStackTrace();
                        mInfoTextView.setText(e.getMessage());
                    }
                } else {
                    Toast.makeText(this, "Подключите интернет", Toast.LENGTH_SHORT).show();
                }
                break;
            case R.id.btnSaveAllRoom:
                Single<Bundle> singleSaveAllRoom = Single.create(new SingleOnSubscribe<Bundle>() {
                    @Override
                    public void subscribe(@NonNull SingleEmitter<Bundle> emitter) throws Exception {
                        String curLogin = "";
                        String curUserID = "";
                        String curAvatarUrl = "";
                        Date first = new Date();
                        List<RoomModel> roomModelList = new ArrayList<>();
                        RoomModel roomModel = new RoomModel();
                        for (RetrofitModel curItem : modelList) {
                            curLogin = curItem.getLogin();
                            curUserID = curItem.getId();
                            curAvatarUrl = curItem.getAvatarUrl();
                            roomModel.setLogin(curLogin);
                            roomModel.setAvatarUrl(curAvatarUrl);
                            roomModel.setUserId(curUserID);
                            roomModelList.add(roomModel);

                            RoomDBApp.get().getDB().productDao().insertAll(roomModelList);
                        }
                        Date second = new Date();
                        Bundle bundle = new Bundle();
                        List<RoomModel> tempList = RoomDBApp.get().getDB().productDao().getAll();
                        bundle.putInt("count", tempList.size());
                        bundle.putLong("msek", second.getTime() - first.getTime());
                        emitter.onSuccess(bundle);
                    }
                }).subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread());
                singleSaveAllRoom.subscribeWith(CreateObserver());
                break;
            case R.id.btnSelectAllRoom:
                Single<Bundle> singleSelectAllRoom = Single.create(new SingleOnSubscribe<Bundle>() {
                    @Override
                    public void subscribe(@NonNull SingleEmitter<Bundle> emitter) throws Exception {
                        try {
                            Date first = new Date();
                            products = RoomDBApp.get().getDB().productDao().getAll();
                            Date second = new Date();
                            Bundle bundle = new Bundle();
                            bundle.putInt("count", products.size());
                            bundle.putLong("msek", second.getTime() - first.getTime());
                            emitter.onSuccess(bundle);
                        } catch (Exception e) {
                            emitter.onError(e);
                        }
                    }
                }).subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread());
                singleSelectAllRoom.subscribeWith(CreateObserver());
                break;
            case R.id.btnDeleteAllRoom:
                Single<Bundle> singleDeleteAllRoom = Single.create(new SingleOnSubscribe<Bundle>() {
                    @Override
                    public void subscribe(@NonNull SingleEmitter<Bundle> emitter) throws Exception {
                        try {
                            Date first = new Date();
                            RoomDBApp.get().getDB().productDao().deleteAll();
                            Date second = new Date();
                            Bundle bundle = new Bundle();
                            bundle.putInt("count", products.size());
                            bundle.putLong("msek", second.getTime() - first.getTime());
                            emitter.onSuccess(bundle);
                        } catch (Exception e) {
                            emitter.onError(e);
                        }
                    }
                }).subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread());
                singleDeleteAllRoom.subscribeWith(CreateObserver());
                break;

        }

    }

    private DisposableSingleObserver<Bundle> CreateObserver() {
        return new DisposableSingleObserver<Bundle>() {
            @Override
            protected void onStart() {
                super.onStart();
                progressBar.setVisibility(View.VISIBLE);
                mInfoTextView.setText("");
            }
            @Override
            public void onSuccess(@NonNull Bundle bundle) {
                progressBar.setVisibility(View.GONE);
                mInfoTextView.append("количество = " + bundle.getInt("count") +
                        "\n милисекунд = " + bundle.getLong("msek"));
            }
            @Override
            public void onError(@NonNull Throwable e) {
                progressBar.setVisibility(View.GONE);
                mInfoTextView.setText("ошибка БД: " + e.getMessage());
            }
        };
    }

    private void downloadOneUrl(Call<List<RetrofitModel>> call) throws IOException {
        call.enqueue(new Callback<List<RetrofitModel>>() {
            @Override
            public void onResponse(Call<List<RetrofitModel>> call, Response<List<RetrofitModel>> response) {
                if (response.isSuccessful()) {
                    if (response != null) {
                        RetrofitModel curModel = null;
                        mInfoTextView.append("\n Size = " + response.body().size()+
                                "\n-----------------");
                        for (int i = 0; i < response.body().size(); i++) {
                            curModel = response.body().get(i);
                            modelList.add(curModel);
                            mInfoTextView.append(
                                    "\nLogin = " + curModel.getLogin() +
                                            "\nId = " + curModel.getId() +
                                            "\nURI = " + curModel.getAvatarUrl() +
                                            "\n-----------------");
                        }
                    }
                } else {
                    System.out.println("onResponse error: " + response.code());
                    mInfoTextView.setText("onResponse error: " + response.code());
                }
                progressBar.setVisibility(View.GONE);
            }
            @Override
            public void onFailure(Call<List<RetrofitModel>> call, Throwable t) {
                System.out.println("onFailure " + t);
                mInfoTextView.setText("onFailure " + t.getMessage());
                progressBar.setVisibility(View.GONE);
            }
        });
    }


}
