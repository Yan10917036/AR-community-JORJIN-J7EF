package com.example.test0605_ar_test;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentOnAttachListener;

import com.google.ar.core.AugmentedImage;
import com.google.ar.core.AugmentedImageDatabase;
import com.google.ar.core.Config;
import com.google.ar.core.Session;
import com.google.ar.core.TrackingState;
import com.google.ar.sceneform.AnchorNode;
import com.google.ar.sceneform.Sceneform;
import com.google.ar.sceneform.math.Vector3;
import com.google.ar.sceneform.rendering.ModelRenderable;
import com.google.ar.sceneform.ux.ArFragment;
import com.google.ar.sceneform.ux.BaseArFragment;
import com.google.ar.sceneform.ux.InstructionsController;
import com.google.ar.sceneform.ux.TransformableNode;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class ArActivity extends AppCompatActivity implements
        FragmentOnAttachListener,
        BaseArFragment.OnSessionConfigurationListener {

    private final List<CompletableFuture<Void>> futures = new ArrayList<>();
    private ArFragment arFragment;
    private boolean rabbitDetected = false;

    private boolean redDetected = false;
    private boolean yellowDetected = false;
    private boolean whiteDetected = false;
    private boolean blueDetected = false;
    private AugmentedImageDatabase database;

    boolean isFavorite = false;
    boolean redFavorite = false;
    boolean blueFavorite = false;
    boolean yellowFavorite = false;
    boolean whiteFavorite = false;

    SharedPreferences pref;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ar);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ViewCompat.setOnApplyWindowInsetsListener(toolbar, (v, insets) -> {
            ((ViewGroup.MarginLayoutParams) toolbar.getLayoutParams()).topMargin = insets
                    .getInsets(WindowInsetsCompat.Type.systemBars())
                    .top;

            return WindowInsetsCompat.CONSUMED;
        });
        getSupportFragmentManager().addFragmentOnAttachListener(this);

        if (savedInstanceState == null) {
            if (Sceneform.isSupported(this)) {
                getSupportFragmentManager().beginTransaction()
                        .add(R.id.arFragment, ArFragment.class, null)
                        .commit();
            }
        }

        Button arButton=(Button)findViewById(R.id.ReturnBtn);
        arButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ArActivity.this,MainActivity.class);
                startActivity(intent);
            }
        });
    }

    @Override
    public void onAttachFragment(@NonNull FragmentManager fragmentManager, @NonNull Fragment fragment) {
        if (fragment.getId() == R.id.arFragment) {
            arFragment = (ArFragment) fragment;
            arFragment.setOnSessionConfigurationListener(this);
        }
    }

    @Override
    public void onSessionConfiguration(Session session, Config config) {
        // Disable plane detection
        config.setPlaneFindingMode(Config.PlaneFindingMode.DISABLED);

        // Images to be detected by our AR need to be added in AugmentedImageDatabase
        // This is how database is created at runtime
        // You can also prebuild database in you computer and load it directly (see: https://developers.google.com/ar/develop/java/augmented-images/guide#database)

        database = new AugmentedImageDatabase(session);

        Bitmap rabbitImage = BitmapFactory.decodeResource(getResources(), R.drawable.rabbit);
        Bitmap matrixImage = BitmapFactory.decodeResource(getResources(), R.drawable.matrix);
        Bitmap redImage = BitmapFactory.decodeResource(getResources(), R.drawable.red);
        Bitmap yellowImage = BitmapFactory.decodeResource(getResources(), R.drawable.yellow);
        Bitmap blueImage = BitmapFactory.decodeResource(getResources(), R.drawable.blue);
        Bitmap whiteImage = BitmapFactory.decodeResource(getResources(), R.drawable.white);
        // Every image has to have its own unique String identifier
        database.addImage("matrix", matrixImage);
        database.addImage("rabbit", rabbitImage);
        database.addImage("red", redImage);
        database.addImage("yellow", yellowImage);
        database.addImage("blue", blueImage);
        database.addImage("white", whiteImage);

        config.setAugmentedImageDatabase(database);

        // Check for image detection
        arFragment.setOnAugmentedImageUpdateListener(this::onAugmentedImageTrackingUpdate);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        futures.forEach(future -> {
            if (!future.isDone())
                future.cancel(true);
        });

    }

    public void onAugmentedImageTrackingUpdate(AugmentedImage augmentedImage) {
        // If there are both images already detected, for better CPU usage we do not need scan for them
        if (rabbitDetected && redDetected && yellowDetected && whiteDetected && blueDetected) {
            return;
        }

        if (augmentedImage.getTrackingState() == TrackingState.TRACKING
                && augmentedImage.getTrackingMethod() == AugmentedImage.TrackingMethod.FULL_TRACKING) {

            // Setting anchor to the center of Augmented Image
            AnchorNode anchorNode = new AnchorNode(augmentedImage.createAnchor(augmentedImage.getCenterPose()));

            // If rabbit model haven't been placed yet and detected image has String identifier of "rabbit"
            // This is also example of model loading and placing at runtime
            if (!rabbitDetected && augmentedImage.getName().equals("rabbit")) {
                rabbitDetected = true;
                Toast.makeText(this, "Rabbit tag detected", Toast.LENGTH_LONG).show();

                anchorNode.setWorldScale(new Vector3(3.5f, 3.5f, 3.5f));
                arFragment.getArSceneView().getScene().addChild(anchorNode);

                futures.add(ModelRenderable.builder()
                        .setSource(this, Uri.parse("models/Rabbit.glb"))
                        .setIsFilamentGltf(true)
                        .build()
                        .thenAccept(rabbitModel -> {
                            TransformableNode modelNode = new TransformableNode(arFragment.getTransformationSystem());
                            modelNode.setRenderable(rabbitModel);
                            anchorNode.addChild(modelNode);
                            modelNode.setOnTapListener((hitTestResult, motionEvent) -> {
                                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                                builder.setTitle("收藏");
                                builder.setMessage("是否要收藏兔子模型？");

                                builder.setPositiveButton("收藏", (dialogInterface, i) -> {
                                    isFavorite = true;
                                    Toast.makeText(this, "已收藏兔子", Toast.LENGTH_SHORT).show();
                                    pref = getSharedPreferences("Favorite_DATA",MODE_PRIVATE);
                                    pref.edit()
                                            .putBoolean("isFavorite",isFavorite)
                                            .apply();
                                });
                                builder.setNegativeButton("取消", (dialogInterface, i) -> {
                                    isFavorite = false;
                                    Toast.makeText(this, "已取消收藏兔子", Toast.LENGTH_SHORT).show();
                                    pref = getSharedPreferences("Favorite_DATA",MODE_PRIVATE);
                                    pref.edit()
                                            .putBoolean("isFavorite",isFavorite)
                                            .apply();
                                });
                                builder.show();
                                Toast.makeText(this, "Rabbit model clicked!", Toast.LENGTH_SHORT).show();
                            });

                        })
                        .exceptionally(
                                throwable -> {
                                    Toast.makeText(this, "Unable to load rabbit model", Toast.LENGTH_LONG).show();
                                    return null;
                                }));
            }
            if (!redDetected && augmentedImage.getName().equals("red")) {
                redDetected = true;
                Toast.makeText(this, "Red tag detected", Toast.LENGTH_LONG).show();

                anchorNode.setWorldScale(new Vector3(0.5f, 0.5f, 0.5f));
                arFragment.getArSceneView().getScene().addChild(anchorNode);

                futures.add(ModelRenderable.builder()
                        .setSource(this, Uri.parse("models/Red.glb"))
                        .setIsFilamentGltf(true)
                        .build()
                        .thenAccept(redModel -> {
                            TransformableNode modelNode = new TransformableNode(arFragment.getTransformationSystem());
                            modelNode.setRenderable(redModel);
                            modelNode.setLocalPosition(new Vector3(0f, 1f, 0f));
                            // Place the modelNode in the center of the augmented image
                            anchorNode.addChild(modelNode);

                            modelNode.setOnTapListener((hitTestResult, motionEvent) -> {
                                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                                builder.setTitle("收藏");
                                builder.setMessage("是否要收藏紅車模型？");

                                builder.setPositiveButton("收藏", (dialogInterface, i) -> {
                                    redFavorite = true;
                                    Toast.makeText(this, "已收藏紅車", Toast.LENGTH_SHORT).show();
                                    pref = getSharedPreferences("Favorite_DATA",MODE_PRIVATE);
                                    pref.edit()
                                            .putBoolean("redFavorite",redFavorite)
                                            .apply();
                                });
                                builder.setNegativeButton("取消", (dialogInterface, i) -> {
                                    redFavorite = false;
                                    Toast.makeText(this, "已取消收藏紅車", Toast.LENGTH_SHORT).show();
                                    pref = getSharedPreferences("Favorite_DATA",MODE_PRIVATE);
                                    pref.edit()
                                            .putBoolean("redFavorite",redFavorite)
                                            .apply();
                                });
                                builder.show();
                                Toast.makeText(this, "Red model clicked!", Toast.LENGTH_SHORT).show();
                            });
                        })
                        .exceptionally(
                                throwable -> {
                                    Toast.makeText(this, "Unable to load red model", Toast.LENGTH_LONG).show();
                                    return null;
                                }));
            }
            if (!yellowDetected && augmentedImage.getName().equals("yellow")) {
                yellowDetected = true;
                Toast.makeText(this, "Yellow tag detected", Toast.LENGTH_LONG).show();

                anchorNode.setWorldScale(new Vector3(0.5f, 0.5f, 0.5f));
                arFragment.getArSceneView().getScene().addChild(anchorNode);

                futures.add(ModelRenderable.builder()
                        .setSource(this, Uri.parse("models/Yellow.glb"))
                        .setIsFilamentGltf(true)
                        .build()
                        .thenAccept(yellowModel -> {
                            TransformableNode modelNode = new TransformableNode(arFragment.getTransformationSystem());
                            modelNode.setRenderable(yellowModel);
                            modelNode.setLocalPosition(new Vector3(0f, 1f, 0f));
                            // Place the modelNode in the center of the augmented image
                            anchorNode.addChild(modelNode);

                            modelNode.setOnTapListener((hitTestResult, motionEvent) -> {
                                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                                builder.setTitle("收藏");
                                builder.setMessage("是否要收藏黃車模型？");
                                builder.setPositiveButton("收藏", (dialogInterface, i) -> {
                                    yellowFavorite = true;
                                    Toast.makeText(this, "已收藏黃車", Toast.LENGTH_SHORT).show();
                                    pref = getSharedPreferences("Favorite_DATA",MODE_PRIVATE);
                                    pref.edit()
                                            .putBoolean("yellowFavorite",yellowFavorite)
                                            .apply();

                                });
                                builder.setNegativeButton("取消", (dialogInterface, i) -> {
                                    yellowFavorite = false;
                                    Toast.makeText(this, "已取消收藏黃車", Toast.LENGTH_SHORT).show();
                                    pref = getSharedPreferences("Favorite_DATA",MODE_PRIVATE);
                                    pref.edit()
                                            .putBoolean("yellowFavorite",yellowFavorite)
                                            .apply();
                                });
                                builder.show();
                                Toast.makeText(this, "Yellow model clicked!", Toast.LENGTH_SHORT).show();
                            });
                        })
                        .exceptionally(
                                throwable -> {
                                    Toast.makeText(this, "Unable to load yellow model", Toast.LENGTH_LONG).show();
                                    return null;
                                }));
            }
            if (!blueDetected && augmentedImage.getName().equals("blue")) {
                blueDetected = true;
                Toast.makeText(this, "Blue tag detected", Toast.LENGTH_LONG).show();

                anchorNode.setWorldScale(new Vector3(0.5f, 0.5f, 0.5f));
                arFragment.getArSceneView().getScene().addChild(anchorNode);

                futures.add(ModelRenderable.builder()
                        .setSource(this, Uri.parse("models/Blue.glb"))
                        .setIsFilamentGltf(true)
                        .build()
                        .thenAccept(blueModel -> {
                            TransformableNode modelNode = new TransformableNode(arFragment.getTransformationSystem());
                            modelNode.setRenderable(blueModel);
                            // Place the modelNode in the center of the augmented image
                            modelNode.setLocalPosition(new Vector3(0f, 1f, 0f));
                            anchorNode.addChild(modelNode);

                            modelNode.setOnTapListener((hitTestResult, motionEvent) -> {
                                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                                builder.setTitle("收藏");
                                builder.setMessage("是否要收藏藍車模型？");

                                builder.setPositiveButton("收藏", (dialogInterface, i) -> {
                                    blueFavorite = true;
                                    Toast.makeText(this, "已收藏藍車", Toast.LENGTH_SHORT).show();
                                    pref = getSharedPreferences("Favorite_DATA",MODE_PRIVATE);
                                    pref.edit()
                                            .putBoolean("blueFavorite",blueFavorite)
                                            .apply();

                                });
                                builder.setNegativeButton("取消", (dialogInterface, i) -> {
                                    blueFavorite = false;
                                    Toast.makeText(this, "已取消收藏藍車", Toast.LENGTH_SHORT).show();
                                    pref = getSharedPreferences("Favorite_DATA",MODE_PRIVATE);
                                    pref.edit()
                                            .putBoolean("blueFavorite",blueFavorite)
                                            .apply();
                                });
                                builder.show();
                                Toast.makeText(this, "Blue model clicked!", Toast.LENGTH_SHORT).show();
                            });
                        })
                        .exceptionally(
                                throwable -> {
                                    Toast.makeText(this, "Unable to load blue model", Toast.LENGTH_LONG).show();
                                    return null;
                                }));
            }
            if (!whiteDetected && augmentedImage.getName().equals("white")) {
                whiteDetected = true;
                Toast.makeText(this, "White tag detected", Toast.LENGTH_LONG).show();

                anchorNode.setWorldScale(new Vector3(0.5f, 0.5f, 0.5f));
                arFragment.getArSceneView().getScene().addChild(anchorNode);

                futures.add(ModelRenderable.builder()
                        .setSource(this, Uri.parse("models/White.glb"))
                        .setIsFilamentGltf(true)
                        .build()
                        .thenAccept(whiteModel -> {
                            TransformableNode modelNode = new TransformableNode(arFragment.getTransformationSystem());
                            modelNode.setRenderable(whiteModel);
                            // Place the modelNode in the center of the augmented image
                            modelNode.setLocalPosition(new Vector3(0f, 1f, 0f));
                            anchorNode.addChild(modelNode);

                            modelNode.setOnTapListener((hitTestResult, motionEvent) -> {
                                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                                builder.setTitle("收藏");
                                builder.setMessage("是否要收藏白車模型？");

                                builder.setPositiveButton("收藏", (dialogInterface, i) -> {
                                    whiteFavorite = true;
                                    Toast.makeText(this, "已收藏白車", Toast.LENGTH_SHORT).show();
                                    pref = getSharedPreferences("Favorite_DATA",MODE_PRIVATE);
                                    pref.edit()
                                            .putBoolean("whiteFavorite", whiteFavorite)
                                            .apply();

                                });
                                builder.setNegativeButton("取消", (dialogInterface, i) -> {
                                    whiteFavorite = false;
                                    Toast.makeText(this, "已取消收藏白車", Toast.LENGTH_SHORT).show();
                                    pref = getSharedPreferences("Favorite_DATA",MODE_PRIVATE);
                                    pref.edit()
                                            .putBoolean("whiteFavorite", whiteFavorite)
                                            .apply();
                                });
                                builder.show();
                                Toast.makeText(this, "White model clicked!", Toast.LENGTH_SHORT).show();
                            });
                        })
                        .exceptionally(
                                throwable -> {
                                    Toast.makeText(this, "Unable to load white model", Toast.LENGTH_LONG).show();
                                    return null;
                                }));
            }
        }
        if (rabbitDetected && redDetected && yellowDetected && blueDetected && whiteDetected) {
            arFragment.getInstructionsController().setEnabled(
                    InstructionsController.TYPE_AUGMENTED_IMAGE_SCAN, false);
        }
    }
}
