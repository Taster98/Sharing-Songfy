package ml.luiggi.geosongfy.fragments;

import android.content.Context;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;

//listener customizzato per switchare tra un fragment e l'altro facendo swipe
class OnSwipeTouchListener implements View.OnTouchListener {
    //lo faccio mediante un gestureDetector in quanto sono gesture di base (swipe)
    private final GestureDetector gestureDetector;

    OnSwipeTouchListener(Context c) {
        gestureDetector = new GestureDetector(c, new GestureListener());
    }

    public boolean onTouch(final View view, final MotionEvent motionEvent) {
        return gestureDetector.onTouchEvent(motionEvent);
    }

    private final class GestureListener extends
            GestureDetector.SimpleOnGestureListener {
        private static final int SOGLIA_SWIPE = 100;

        @Override
        public boolean onDown(MotionEvent e) {
            return true;
        }

        @Override
        public boolean onSingleTapUp(MotionEvent e) {
            onClick();
            return super.onSingleTapUp(e);
        }

        @Override
        public boolean onDoubleTap(MotionEvent e) {
            onDoubleClick();
            return super.onDoubleTap(e);
        }

        @Override
        public void onLongPress(MotionEvent e) {
            onLongClick();
            super.onLongPress(e);
        }

        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            if (e1 == null || e2 == null)
                return false;
            try {
                //salvo le distanze degli swipe:
                float diffX = e2.getX() - e1.getX();
                float diffY = e2.getY() - e1.getY();
                //Se è effettivamente occorso uno swipe orizzontale, verifico che sia nella soglia data
                if (Math.abs(diffX) > Math.abs(diffY)) {
                    if (Math.abs(diffX) > SOGLIA_SWIPE && Math.abs(velocityX) > SOGLIA_SWIPE) {
                        //ora distinguo se si è andati a sinistra o a destra
                        if (diffX > 0) {
                            onSwipeRight();
                        } else {
                            onSwipeLeft();
                        }
                    }
                }
                //altrimenti lo swipe è avvenuto in verticale, verifico sempre la soglia:
                else {
                    if (Math.abs(diffY) > SOGLIA_SWIPE && Math.abs(velocityY) > SOGLIA_SWIPE) {
                        if (diffY > 0) {
                            onSwipeDown();
                        } else {
                            onSwipeUp();
                        }
                    }
                }
            } catch (Exception exception) {
                exception.printStackTrace();
            }
            return false;
        }
    }

    public void onSwipeRight() {
    }

    public void onSwipeLeft() {
    }

    private void onSwipeUp() {
    }

    private void onSwipeDown() {
    }

    private void onClick() {
    }

    private void onDoubleClick() {
    }

    private void onLongClick() {
    }
}