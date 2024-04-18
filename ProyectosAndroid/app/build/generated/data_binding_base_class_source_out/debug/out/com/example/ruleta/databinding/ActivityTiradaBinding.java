// Generated by view binder compiler. Do not edit!
package com.example.ruleta.databinding;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.viewbinding.ViewBinding;
import androidx.viewbinding.ViewBindings;
import com.example.ruleta.R;
import java.lang.NullPointerException;
import java.lang.Override;
import java.lang.String;

public final class ActivityTiradaBinding implements ViewBinding {
  @NonNull
  private final ConstraintLayout rootView;

  @NonNull
  public final ImageView MonedaTotal1;

  @NonNull
  public final ImageView MonedaTotal2;

  @NonNull
  public final ImageView Pointer;

  @NonNull
  public final ImageView Ruleta;

  @NonNull
  public final Button btnRetirarse;

  @NonNull
  public final Button btnTirar;

  @NonNull
  public final EditText editTextNumber3;

  @NonNull
  public final ImageView imageView3;

  @NonNull
  public final ConstraintLayout main;

  @NonNull
  public final TextView textUser;

  @NonNull
  public final TextView txtApostar;

  @NonNull
  public final TextView txtMonedasTotales;

  private ActivityTiradaBinding(@NonNull ConstraintLayout rootView, @NonNull ImageView MonedaTotal1,
      @NonNull ImageView MonedaTotal2, @NonNull ImageView Pointer, @NonNull ImageView Ruleta,
      @NonNull Button btnRetirarse, @NonNull Button btnTirar, @NonNull EditText editTextNumber3,
      @NonNull ImageView imageView3, @NonNull ConstraintLayout main, @NonNull TextView textUser,
      @NonNull TextView txtApostar, @NonNull TextView txtMonedasTotales) {
    this.rootView = rootView;
    this.MonedaTotal1 = MonedaTotal1;
    this.MonedaTotal2 = MonedaTotal2;
    this.Pointer = Pointer;
    this.Ruleta = Ruleta;
    this.btnRetirarse = btnRetirarse;
    this.btnTirar = btnTirar;
    this.editTextNumber3 = editTextNumber3;
    this.imageView3 = imageView3;
    this.main = main;
    this.textUser = textUser;
    this.txtApostar = txtApostar;
    this.txtMonedasTotales = txtMonedasTotales;
  }

  @Override
  @NonNull
  public ConstraintLayout getRoot() {
    return rootView;
  }

  @NonNull
  public static ActivityTiradaBinding inflate(@NonNull LayoutInflater inflater) {
    return inflate(inflater, null, false);
  }

  @NonNull
  public static ActivityTiradaBinding inflate(@NonNull LayoutInflater inflater,
      @Nullable ViewGroup parent, boolean attachToParent) {
    View root = inflater.inflate(R.layout.activity_tirada, parent, false);
    if (attachToParent) {
      parent.addView(root);
    }
    return bind(root);
  }

  @NonNull
  public static ActivityTiradaBinding bind(@NonNull View rootView) {
    // The body of this method is generated in a way you would not otherwise write.
    // This is done to optimize the compiled bytecode for size and performance.
    int id;
    missingId: {
      id = R.id.MonedaTotal1;
      ImageView MonedaTotal1 = ViewBindings.findChildViewById(rootView, id);
      if (MonedaTotal1 == null) {
        break missingId;
      }

      id = R.id.MonedaTotal2;
      ImageView MonedaTotal2 = ViewBindings.findChildViewById(rootView, id);
      if (MonedaTotal2 == null) {
        break missingId;
      }

      id = R.id.Pointer;
      ImageView Pointer = ViewBindings.findChildViewById(rootView, id);
      if (Pointer == null) {
        break missingId;
      }

      id = R.id.Ruleta;
      ImageView Ruleta = ViewBindings.findChildViewById(rootView, id);
      if (Ruleta == null) {
        break missingId;
      }

      id = R.id.btnRetirarse;
      Button btnRetirarse = ViewBindings.findChildViewById(rootView, id);
      if (btnRetirarse == null) {
        break missingId;
      }

      id = R.id.btnTirar;
      Button btnTirar = ViewBindings.findChildViewById(rootView, id);
      if (btnTirar == null) {
        break missingId;
      }

      id = R.id.editTextNumber3;
      EditText editTextNumber3 = ViewBindings.findChildViewById(rootView, id);
      if (editTextNumber3 == null) {
        break missingId;
      }

      id = R.id.imageView3;
      ImageView imageView3 = ViewBindings.findChildViewById(rootView, id);
      if (imageView3 == null) {
        break missingId;
      }

      ConstraintLayout main = (ConstraintLayout) rootView;

      id = R.id.textUser;
      TextView textUser = ViewBindings.findChildViewById(rootView, id);
      if (textUser == null) {
        break missingId;
      }

      id = R.id.txtApostar;
      TextView txtApostar = ViewBindings.findChildViewById(rootView, id);
      if (txtApostar == null) {
        break missingId;
      }

      id = R.id.txtMonedasTotales;
      TextView txtMonedasTotales = ViewBindings.findChildViewById(rootView, id);
      if (txtMonedasTotales == null) {
        break missingId;
      }

      return new ActivityTiradaBinding((ConstraintLayout) rootView, MonedaTotal1, MonedaTotal2,
          Pointer, Ruleta, btnRetirarse, btnTirar, editTextNumber3, imageView3, main, textUser,
          txtApostar, txtMonedasTotales);
    }
    String missingId = rootView.getResources().getResourceName(id);
    throw new NullPointerException("Missing required view with ID: ".concat(missingId));
  }
}
