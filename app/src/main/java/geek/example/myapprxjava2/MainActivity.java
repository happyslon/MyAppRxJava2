package geek.example.myapprxjava2;


import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.transition.Transition;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.concurrent.TimeUnit;


import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.reactivex.BackpressureStrategy;
import io.reactivex.Flowable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

public class MainActivity extends AppCompatActivity {
    AlertDialog dialog;
    Disposable disposable;


    @BindView(R.id.btn_start)
    Button btn;
    @BindView(R.id.btn_start_con)
    Button btnConvert;
    @BindView(R.id.profile_image)
    ImageView image;



    @OnClick(R.id.btn_start)
    public void btnStart() {
        loadSave();
    }

    @OnClick(R.id.btn_start_con)
    public void btnStartConvert() {
        myFlowable();
        dialog.show();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);


        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setTitle(R.string.dialog_about_title);
        //builder.setMessage(R.string.dialog_about_message);
        builder.setCancelable(true);
        builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() { // Кнопка ОК
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss(); // Отпускает диалоговое окно
            }
        });
        builder.setNegativeButton(R.string.alert_dialog_cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                disposable.dispose();
                dialog.dismiss();
            }
        });
        dialog = builder.create();
    }

    private void loadSave() {

        GlideApp.with(this)
                .asBitmap()
                .load("http://img-e.photosight.ru/a83/6804322_xlarge.jpg")
                .into(new SimpleTarget<Bitmap>() {
                    @Override
                    public void onResourceReady(Bitmap resource, Transition<? super Bitmap> transition) {
                        image.setImageBitmap(resource);
                        saveImage(resource);

                    }
                });
    }

    private void saveImage(Bitmap image) {
        String imageFileName = "picture.jpg";
        File imageFile = new File(getFilesDir(), imageFileName);
        try {
            OutputStream fOut = new FileOutputStream(imageFile);
            image.compress(Bitmap.CompressFormat.JPEG, 100, fOut);
            fOut.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        Toast.makeText(this, "IMAGE SAVED", Toast.LENGTH_LONG).show();
    }

    private void myFlowable() {

        Flowable<Boolean> flowable = Flowable.create(emitter -> {
            boolean success = false;
            try {
                TimeUnit.MILLISECONDS.sleep(10000);
                Bitmap bmp = BitmapFactory.decodeFile(getFilesDir() + "/picture.jpg");
                File convertedImage = new File(getFilesDir() + "/convertedimg.png");
                FileOutputStream outStream = new FileOutputStream(convertedImage);
                success = bmp.compress(Bitmap.CompressFormat.PNG, 100, outStream);
                outStream.flush();
                outStream.close();
                if(emitter.isCancelled()) return;
            } catch (InterruptedException e) {
                e.printStackTrace();
                return;
            } catch (IOException e) {
                e.printStackTrace();
                return;
            }
            emitter.onNext(success);
            emitter.onComplete();
        }, BackpressureStrategy.BUFFER);
        disposable = flowable.subscribeOn(Schedulers.computation())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(val -> message(val));
    }

    private void message(Boolean val) {
        if (val) {
            Toast.makeText(getApplicationContext(), "Converting is successful.", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(getApplicationContext(), "Converting is unsucessful.", Toast.LENGTH_SHORT).show();
        }
    }
}





