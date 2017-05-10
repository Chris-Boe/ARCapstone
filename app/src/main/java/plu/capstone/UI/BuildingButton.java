package plu.capstone.UI;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.Log;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.Button;

import plu.capstone.Models.PointOfInterest;

/**
 * Created by playt on 4/5/2017.
 */

public class BuildingButton extends android.support.v7.widget.AppCompatButton {

    private float orientation[];
    public BuildingButton(Context context) {
        super(context);
        setBackgroundColor(Color.TRANSPARENT);
    }

    public BuildingButton(Context context, int w, int h, float[] ori){
        super(context);
        orientation = ori;
        setBackgroundColor(Color.TRANSPARENT);
    }




    @Override
    public void setTranslationX(float x){

        final float fX = x;
        final BuildingButton thisButton = this;

        this.getViewTreeObserver().addOnGlobalLayoutListener(
                new ViewTreeObserver.OnGlobalLayoutListener() {


                    @Override
                    public void onGlobalLayout() {
                        thisButton.setTranslationhelperX(fX-(thisButton.getWidth()/2));
                        thisButton.setVisibility(VISIBLE);
                    }
                });

    }

    private void setTranslationhelperX(float x){
        super.setTranslationX(x);
    }

    @Override
    public void setTranslationY(float y){

        final float fY = y;

        final BuildingButton thisButton = this;
        this.getViewTreeObserver().addOnGlobalLayoutListener(
                new ViewTreeObserver.OnGlobalLayoutListener() {


                    @Override
                    public void onGlobalLayout() {
                        thisButton.setTranslationhelperY(fY-(thisButton.getWidth()/2));
                        thisButton.setVisibility(VISIBLE);
                    }
                });
    }

    private void setTranslationhelperY(float y){
        super.setTranslationY(y);
    }

    @Override
    public void setX(float x){
        final float fX = x;
        final BuildingButton thisButton = this;

        this.getViewTreeObserver().addOnGlobalLayoutListener(
                new ViewTreeObserver.OnGlobalLayoutListener() {


                    @Override
                    public void onGlobalLayout() {
                        thisButton.setHelperX(fX-(thisButton.getWidth()/2));
                        thisButton.setVisibility(VISIBLE);
                    }
                });
    }

    public void setHelperX(float x){
        super.setX(x);
    }

    @Override
    public void setY(float y){

        final float fY = y;
        final BuildingButton thisButton = this;

        this.getViewTreeObserver().addOnGlobalLayoutListener(
                new ViewTreeObserver.OnGlobalLayoutListener() {


                    @Override
                    public void onGlobalLayout() {
                        thisButton.setHelperY(fY-(thisButton.getHeight()/2));
                        thisButton.setVisibility(VISIBLE);
                    }
                });
    }

    public void setHelperY(float y){
        super.setY(y);
    }

}
