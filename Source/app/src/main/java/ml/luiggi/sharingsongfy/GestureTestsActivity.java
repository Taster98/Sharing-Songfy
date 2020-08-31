package ml.luiggi.sharingsongfy;

import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.DragEvent;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

public class GestureTestsActivity extends AppCompatActivity implements View.OnTouchListener, GestureDetector.OnGestureListener, View.OnDragListener, GestureDetector.OnDoubleTapListener {
    private static final String TAG = "DEBUG TAG";

    private ImageView image1,image2;
    private GestureDetector gestureDetector;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.test_layout_gestures);
        image1 = findViewById(R.id.image1);
        image2 = findViewById(R.id.image2);

        image1.setOnTouchListener(this);
        image2.setOnTouchListener(this);
        gestureDetector = new GestureDetector(this,this);
    }

    float startX=-1,startY=-1,middleX=-1,middleY=-1,endX=-1,endY=-1;
    //due dita
    float startFingerY1=-1, startFingerY2=-1,endFingerY1=-1,endFingerY2=-1;
    static final float SOGLIA_MIN_DUE_DITA = 100;
    boolean medium=false, finaleSin=false, finaleDes=false;
    boolean rangeMedium(float medX){
        if(Math.abs(medX-startX) >= 120 && Math.abs(medX-startX) <= 250){
            return true;
        }
        return false;
    }
    boolean rangeSinFinale(){
        if(Math.abs(endX-startX) <= 140 && endX != -1 && startX != -1 && endX > middleX && endY > middleY && middleY != -1 && Math.abs(endY-startY) >= 200 && Math.abs(endY-startY) <= 450){
            return true;
        }
        return false;
    }
    boolean rangeDesFinale(){
        if(Math.abs(endX-startX) <= 140 && endX != -1 && startX != -1 && endX < middleX && endY > middleY && middleY != -1 && Math.abs(endY-startY) >= 200 && Math.abs(endY-startY) <= 450){
            return true;
        }
        return false;
    }

    @Override
    public boolean onTouch(View view, final MotionEvent motionEvent) {
        int action = motionEvent.getAction();
        //MEMORIZZO I PUNTI
        switch(action & MotionEvent.ACTION_MASK) {
            case (MotionEvent.ACTION_DOWN) :
                if(motionEvent.getPointerCount() == 1){
                    Log.d(TAG,"Action was DOWN");
                    startX = motionEvent.getX();
                    startY = motionEvent.getY();
                    Log.d(TAG,"Punti start: "+String.valueOf(startX)+", "+String.valueOf(startY));
                }else if(motionEvent.getPointerCount() > 1){
                    startFingerY1 = motionEvent.getY(0);
                    startFingerY2 = motionEvent.getY(1);
                }

                return true;
            case (MotionEvent.ACTION_MOVE) :
                Log.d(TAG,"Action was MOVE");
                if(motionEvent.getPointerCount() == 1){
                    if(rangeMedium(motionEvent.getX())){
                        middleX = motionEvent.getX();
                        middleY = motionEvent.getY();
                        medium = true;
                        Log.d(TAG,"onTouch: (x,y) ("+motionEvent.getX()+", "+motionEvent.getY()+")");
                    }
                }
                return true;
            case (MotionEvent.ACTION_UP) :
                if(motionEvent.getPointerCount() == 1) {
                    Log.d(TAG, "Action was UP");
                    endX = motionEvent.getX();
                    endY = motionEvent.getY();
                    Log.d(TAG, "Punti end: " + String.valueOf(endX) + ", " + String.valueOf(endY));
                    if (rangeSinFinale()) {
                        finaleSin = true;
                    } else if (rangeDesFinale()) {
                        finaleDes = true;
                    }
                    if (medium) {
                        if (finaleDes) {
                            Toast.makeText(getApplicationContext(), "EUREKA DESTRA", Toast.LENGTH_SHORT).show();
                        } else if (finaleSin) {
                            Toast.makeText(getApplicationContext(), "EUREKA SINISTRA", Toast.LENGTH_SHORT).show();
                        }
                    }
                    middleY = -1;
                    middleX = -1;
                    startX = -1;
                    startY = -1;
                    endX = -1;
                    endY = -1;
                    finaleSin = false;
                    finaleDes = false;
                    medium = false;
                }else if(motionEvent.getPointerCount() > 1){
                    endFingerY1 = motionEvent.getY(0);
                    endFingerY2 = motionEvent.getY(1);
                    if(endFingerY1-startFingerY1>=0 && endFingerY2-startFingerY2>=0){
                        Toast.makeText(getApplicationContext(), "EUREKA DUE DITA IN GIU'", Toast.LENGTH_SHORT).show();
                    }
                }
                return true;
            case (MotionEvent.ACTION_CANCEL) :
                Log.d(TAG,"Action was CANCEL");
                return true;
            case (MotionEvent.ACTION_OUTSIDE) :
                Log.d(TAG,"Movement occurred outside bounds " +
                        "of current screen element");
                return true;
            case (MotionEvent.ACTION_POINTER_DOWN):
                if(motionEvent.getPointerCount() > 1){
                    startFingerY1 = motionEvent.getY(0);
                    startFingerY2 = motionEvent.getY(1);
                }
            case (MotionEvent.ACTION_POINTER_UP):
                if(motionEvent.getPointerCount() > 1){
                    endFingerY1 = motionEvent.getY(0);
                    endFingerY2 = motionEvent.getY(1);
                    if(endFingerY1-startFingerY1>=SOGLIA_MIN_DUE_DITA && endFingerY2-startFingerY2>=SOGLIA_MIN_DUE_DITA){
                        Toast.makeText(getApplicationContext(), "EUREKA DUE DITA IN GIU'", Toast.LENGTH_SHORT).show();
                    }
                }
            default :
                return super.onTouchEvent(motionEvent);
        }

//        if(view.getId() == R.id.image1){
//            gestureDetector.onTouchEvent(motionEvent);
//            return true;
//        }
//        if(view.getId() == R.id.image2){
//            return false;
//        }
    }

    /* Gesture */
    @Override
    public boolean onDown(MotionEvent motionEvent) {
        Log.d("TAG","onDown pressed");
        return false;
    }

    @Override
    public void onShowPress(MotionEvent motionEvent) {
        Log.d("TAG","onShowPress pressed");
    }

    @Override
    public boolean onSingleTapUp(MotionEvent motionEvent) {
        Log.d("TAG","onSingleTapUp pressed");
        return false;
    }

    @Override
    public boolean onScroll(MotionEvent motionEvent, MotionEvent motionEvent1, float v, float v1) {
        Log.d("TAG","onScroll pressed");
        return false;
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    public void onLongPress(MotionEvent motionEvent) {
        Log.d("TAG","onLongPress pressed");
//        View.DragShadowBuilder builder = new View.DragShadowBuilder(image1);
//        image1.startDragAndDrop(null,builder,null,0);
//        builder.getView().setOnDragListener(this);
    }

    @Override
    public boolean onFling(MotionEvent motionEvent, MotionEvent motionEvent1, float v, float v1) {
        Log.d("TAG","onFling pressed");
        return false;
    }

    @Override
    public boolean onDrag(View view, DragEvent dragEvent) {
//        switch(dragEvent.getAction()) {
//
//            case DragEvent.ACTION_DRAG_STARTED:
//                Log.d(TAG, "onDrag: drag started.");
//
//                return true;
//
//            case DragEvent.ACTION_DRAG_ENTERED:
//                Log.d(TAG, "onDrag: drag entered.");
//                return true;
//
//            case DragEvent.ACTION_DRAG_LOCATION:
//                Log.d(TAG, "onDrag: current point: ( " + dragEvent.getX() + " , " + dragEvent.getY() + " )"  );
//
//                return true;
//
//            case DragEvent.ACTION_DRAG_EXITED:
//                Log.d(TAG, "onDrag: exited.");
//                return true;
//
//            case DragEvent.ACTION_DROP:
//
//                Log.d(TAG, "onDrag: dropped.");
//
//                return true;
//
//            case DragEvent.ACTION_DRAG_ENDED:
//                Log.d(TAG, "onDrag: ended.");
//
//
//                return true;
//
//            // An unknown action type was received.
//            default:
//                Log.e(TAG,"Unknown action type received by OnStartDragListener.");
//                break;
//
//        }
        return false;
    }

    @Override
    public boolean onSingleTapConfirmed(MotionEvent motionEvent) {
        Log.d(TAG,"Single tap");
        return false;
    }

    @Override
    public boolean onDoubleTap(MotionEvent motionEvent) {
        Log.d(TAG,"Double tap");
        return false;
    }

    @Override
    public boolean onDoubleTapEvent(MotionEvent motionEvent) {
        Log.d(TAG,"Double tap event");
        return false;
    }
}
/*
X: 62 Y: 389
X: 136 Y: 276
X: 55 Y: 340
X: 21 Y: 286

PER X: --> MAX DIFF DI 145 --> MEDIA CA 70 --> (20,140) intervallo accettato per X  (0-140) X
PER Y: --> MIN DIFF DI 286 --> MIN 275 --> (200,450) intervallo accettato per Y     (200-450) Y

MEDIANA --> 240, 139,188, 186                                           ----------->(120,250) X e SALVO LA X perchè se startX è > di xTmp allora indietro, else avanti

 */
