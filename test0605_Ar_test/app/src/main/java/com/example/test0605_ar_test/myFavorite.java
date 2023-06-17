package com.example.test0605_ar_test;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentOnAttachListener;

import com.google.ar.core.Anchor;
import com.google.ar.core.Config;
import com.google.ar.core.HitResult;
import com.google.ar.core.Plane;
import com.google.ar.core.Session;
import com.google.ar.sceneform.AnchorNode;
import com.google.ar.sceneform.ArSceneView;
import com.google.ar.sceneform.Node;
import com.google.ar.sceneform.SceneView;
import com.google.ar.sceneform.Sceneform;
import com.google.ar.sceneform.math.Vector3;
import com.google.ar.sceneform.rendering.ModelRenderable;
import com.google.ar.sceneform.rendering.Renderable;
import com.google.ar.sceneform.rendering.ViewRenderable;
import com.google.ar.sceneform.ux.ArFragment;
import com.google.ar.sceneform.ux.BaseArFragment;
import com.google.ar.sceneform.ux.TransformableNode;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

public class myFavorite extends AppCompatActivity implements
        FragmentOnAttachListener,
        BaseArFragment.OnTapArPlaneListener,
        BaseArFragment.OnSessionConfigurationListener,
        ArFragment.OnViewCreatedListener {

    private ArFragment arFragment;
    private Renderable model;
    private ViewRenderable viewRenderable;

    SharedPreferences pref;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_favorite);
        getSupportFragmentManager().addFragmentOnAttachListener(this);

        Button returnButton = (Button) findViewById(R.id.ReturnBtn);
        Button optionsButton = (Button) findViewById(R.id.optionsBtn);
        TextView tv = (TextView) findViewById(R.id.textView);

        pref = getSharedPreferences("Favorite_DATA", MODE_PRIVATE);

        optionsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(myFavorite.this);
                builder.setTitle("選擇模型");
                List<String> items = new ArrayList<>();

//                tv.setText(String.valueOf(pref.getBoolean("isFavorite",false)));
                if (pref.getBoolean("isFavorite", false)) {
                    items.add("兔子");
                }
                if (pref.getBoolean("redFavorite", false)) {
                    items.add("紅車");
                }
                if (pref.getBoolean("blueFavorite", false)) {
                    items.add("藍車");
                }
                if (pref.getBoolean("yellowFavorite", false)) {
                    items.add("黃車");
                }
                if (pref.getBoolean("whiteFavorite", false)) {
                    items.add("白車");
                }
                String[] itemsArray = items.toArray(new String[0]);
                builder.setItems(itemsArray, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // 點擊選項時的處理程式碼
                        String selectedItem = itemsArray[which];
                        // 根據選項進行相應的處理
                        if (selectedItem.equals("兔子")) {
                            tv.setText("兔子");
                            loadRabbit();
                        } else if (selectedItem.equals("紅車")) {
                            tv.setText("紅車");
                            loadRed();
                        } else if (selectedItem.equals("藍車")) {
                            tv.setText("藍車");
                            loadBlue();
                        } else if (selectedItem.equals("黃車")) {
                            tv.setText("黃車");
                            loadYellow();
                        } else if (selectedItem.equals("白車")) {
                            tv.setText("白車");
                            loadWhite();
                        }

                    }
                });
                builder.create().
                        show();
            }
        });


        returnButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setClass(myFavorite.this, MainActivity.class);
                startActivity(intent);
            }
        });

        if (savedInstanceState == null) {
            if (Sceneform.isSupported(this)) {
                getSupportFragmentManager().beginTransaction()
                        .add(R.id.arFragment, ArFragment.class, null)
                        .commit();
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Release the model and viewRenderable resources
        model = null;
        viewRenderable = null;

        // Release any other AR-related resources if necessary
        if (arFragment != null) {
            arFragment.setOnViewCreatedListener(null);
            arFragment.setOnTapArPlaneListener(null);
            arFragment.setOnSessionConfigurationListener(null);
            arFragment.getArSceneView().destroy();
        }
    }

    @Override
    public void onAttachFragment(@NonNull FragmentManager fragmentManager, @NonNull Fragment
            fragment) {
        if (fragment.getId() == R.id.arFragment) {
            arFragment = (ArFragment) fragment;
            arFragment.setOnSessionConfigurationListener(this);
            arFragment.setOnViewCreatedListener(this);
            arFragment.setOnTapArPlaneListener(this);
        }
    }

    @Override
    public void onSessionConfiguration(Session session, Config config) {
        if (session.isDepthModeSupported(Config.DepthMode.AUTOMATIC)) {
            config.setDepthMode(Config.DepthMode.AUTOMATIC);
        }
    }

    @Override
    public void onViewCreated(ArSceneView arSceneView) {
        arFragment.setOnViewCreatedListener(null);

        // Fine adjust the maximum frame rate
        arSceneView.setFrameRateFactor(SceneView.FrameRate.FULL);
    }

    //載入各個模型
    public void loadRabbit() {
        WeakReference<myFavorite> weakActivity = new WeakReference<>(this);
        ModelRenderable.builder()
                .setSource(this, Uri.parse("models/Rabbit.glb"))
                .setIsFilamentGltf(true)
                .setAsyncLoadEnabled(true)
                .build()
                .thenAccept(model -> {
                    myFavorite activity = weakActivity.get();
                    if (activity != null) {
                        activity.model = model;
                    }
                })
                .exceptionally(throwable -> {
                    Toast.makeText(
                            this, "Unable to load model", Toast.LENGTH_LONG).show();
                    return null;
                });
        ViewRenderable.builder()
                .setView(this, R.layout.view_model_title_rabbit)
                .build()
                .thenAccept(viewRenderable -> {
                    myFavorite activity = weakActivity.get();
                    if (activity != null) {
                        activity.viewRenderable = viewRenderable;
                    }
                })
                .exceptionally(throwable -> {
                    Toast.makeText(this, "Unable to load model", Toast.LENGTH_LONG).show();
                    return null;
                });
    }

    public void loadRed() {
        WeakReference<myFavorite> weakActivity = new WeakReference<>(this);
        ModelRenderable.builder()
                .setSource(this, Uri.parse("models/Red.glb"))
                .setIsFilamentGltf(true)
                .setAsyncLoadEnabled(true)
                .build()
                .thenAccept(model -> {
                    myFavorite activity = weakActivity.get();
                    if (activity != null) {
                        activity.model = model;
                    }
                })
                .exceptionally(throwable -> {
                    Toast.makeText(
                            this, "Unable to load model", Toast.LENGTH_LONG).show();
                    return null;
                });
        ViewRenderable.builder()
                .setView(this, R.layout.view_model_title_red)
                .build()
                .thenAccept(viewRenderable -> {
                    myFavorite activity = weakActivity.get();
                    if (activity != null) {
                        activity.viewRenderable = viewRenderable;
                    }
                })
                .exceptionally(throwable -> {
                    Toast.makeText(this, "Unable to load model", Toast.LENGTH_LONG).show();
                    return null;
                });

    }

    public void loadBlue() {
        WeakReference<myFavorite> weakActivity = new WeakReference<>(this);
        ModelRenderable.builder()
                .setSource(this, Uri.parse("models/Blue.glb"))
                .setIsFilamentGltf(true)
                .setAsyncLoadEnabled(true)
                .build()
                .thenAccept(model -> {
                    myFavorite activity = weakActivity.get();
                    if (activity != null) {
                        activity.model = model;
                    }
                })
                .exceptionally(throwable -> {
                    Toast.makeText(
                            this, "Unable to load model", Toast.LENGTH_LONG).show();
                    return null;
                });
        ViewRenderable.builder()
                .setView(this, R.layout.view_model_title_blue)
                .build()
                .thenAccept(viewRenderable -> {
                    myFavorite activity = weakActivity.get();
                    if (activity != null) {
                        activity.viewRenderable = viewRenderable;
                    }
                })
                .exceptionally(throwable -> {
                    Toast.makeText(this, "Unable to load model", Toast.LENGTH_LONG).show();
                    return null;
                });

    }

    public void loadYellow() {
        WeakReference<myFavorite> weakActivity = new WeakReference<>(this);
        ModelRenderable.builder()
                .setSource(this, Uri.parse("models/Yellow.glb"))
                .setIsFilamentGltf(true)
                .setAsyncLoadEnabled(true)
                .build()
                .thenAccept(model -> {
                    myFavorite activity = weakActivity.get();
                    if (activity != null) {
                        activity.model = model;
                    }
                })
                .exceptionally(throwable -> {
                    Toast.makeText(
                            this, "Unable to load model", Toast.LENGTH_LONG).show();
                    return null;
                });
        ViewRenderable.builder()
                .setView(this, R.layout.view_model_title_yellow)
                .build()
                .thenAccept(viewRenderable -> {
                    myFavorite activity = weakActivity.get();
                    if (activity != null) {
                        activity.viewRenderable = viewRenderable;
                    }
                })
                .exceptionally(throwable -> {
                    Toast.makeText(this, "Unable to load model", Toast.LENGTH_LONG).show();
                    return null;
                });

    }

    public void loadWhite() {
        WeakReference<myFavorite> weakActivity = new WeakReference<>(this);
        ModelRenderable.builder()
                .setSource(this, Uri.parse("models/White.glb"))
                .setIsFilamentGltf(true)
                .setAsyncLoadEnabled(true)
                .build()
                .thenAccept(model -> {
                    myFavorite activity = weakActivity.get();
                    if (activity != null) {
                        activity.model = model;
                    }
                })
                .exceptionally(throwable -> {
                    Toast.makeText(
                            this, "Unable to load model", Toast.LENGTH_LONG).show();
                    return null;
                });
        ViewRenderable.builder()
                .setView(this, R.layout.view_model_title_white)
                .build()
                .thenAccept(viewRenderable -> {
                    myFavorite activity = weakActivity.get();
                    if (activity != null) {
                        activity.viewRenderable = viewRenderable;
                    }
                })
                .exceptionally(throwable -> {
                    Toast.makeText(this, "Unable to load model", Toast.LENGTH_LONG).show();
                    return null;
                });

    }

    @Override
    public void onTapPlane(HitResult hitResult, Plane plane, MotionEvent motionEvent) {
        if (model == null || viewRenderable == null) {
            Toast.makeText(this, "Loading...", Toast.LENGTH_SHORT).show();
            return;
        }

        // Create the Anchor.
        Anchor anchor = hitResult.createAnchor();
        AnchorNode anchorNode = new AnchorNode(anchor);
        anchorNode.setParent(arFragment.getArSceneView().getScene());

        // Create the transformable model and add it to the anchor.
        TransformableNode model = new TransformableNode(arFragment.getTransformationSystem());
        model.setParent(anchorNode);
        model.setRenderable(this.model)
                .animate(true).start();
        model.select();

        Node titleNode = new Node();
        titleNode.setParent(model);
        titleNode.setEnabled(false);
        titleNode.setLocalPosition(new Vector3(0.0f, 1.0f, 0.0f));
        titleNode.setRenderable(viewRenderable);
        titleNode.setEnabled(true);
    }
}