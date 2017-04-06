package plu.capstone;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.widget.Button;

/**
 * Created by playt on 4/5/2017.
 */

public class BuildingButton extends Button {
    private int height,width;
    private float orientation[];
    public BuildingButton(Context context) {
        super(context);
    }

    public BuildingButton(Context context, int w, int h, float[] ori){
        super(context);
        height = h;
        width = w;
        orientation = ori;
    }
    public void setHeightWidth(int w, int h){
        height=h;
        width=w;
    }

    @Override
    protected void onDraw(Canvas canvas){
        //canvas.rotate((float) (0.0f - Math.toDegrees(orientation[2])));
        Paint paint = new Paint();
        paint.setColor(Color.BLUE);
        paint.setStrokeWidth(10);
        paint.setStyle(Paint.Style.FILL);
        canvas.drawCircle(canvas.getWidth()/2, canvas.getHeight()/2, canvas.getHeight()/3, paint);
        super.onDraw(canvas);
    }
}
