package gilianmarques.dev.musicplayer.movement;

import android.animation.ValueAnimator;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.OvershootInterpolator;

import java.math.BigDecimal;
import java.math.RoundingMode;

import gilianmarques.dev.musicplayer.utils.Utils;

/**
 * Criado por Gilian Marques em 29/05/2018 as 06:35:18.
 */
@SuppressWarnings("FieldCanBeLocal")
public class FloatingPagerEvent implements View.OnTouchListener {


    private final View actView;
    private final View view;
    private final MovimentCallback movimentCallback;

    private final int MIN_ANIMATION_TIME_MILLIS = 500;
    private final int MAX_ANIMATION_TIME_MILLIS = 1000;

    private boolean stealEvent;
    private boolean motionEnabled = true;
    private boolean viewIsUping;

    /*O valor contido nessa constante indica o quanto % a view pode se mecher com base em seu tamanho
     * EX: se a view tem 100px e o valor da constante é 120 a view podera se mecher  100+120% do seu tamanho, nesse caso, 120 mesmo,
     * o valor de movimento em px será de 220px pra cima ou baixo*/
    private final int PERCENT_TO_MOVE;

    /*Aqui eu digo a disatancia que a view tem que se mecher em relação a sua posição atual para
     * ser lançada*/
    private int MIN_MOV_TO_LAUNCH_UP;
    private int MIN_MOV_TO_LAUNCH_DOWN;
    private final float TENSION_INCREMENTER = 5;

    /*usado pra rastrear qts px o dedo do usuario percorreu na tela pra dps somar esse valor com o valor Y da view para move-la suavemente*/
    private float initialTouchY;
    private float initialTouchX;
    private float initialViewY = -1;
    private float initialActivityViewY;
    private float viewHeigth;
    /*maxMovimentDown e maxMovimentUp mantem salvos a distancia em px que a view pode ser arrastada
     * com base na porcentagem definida em PERCENT_TO_MOVE */
    private float maxMovimentDown;
    private float maxMovimentUp;
    private float maxMovimentActView;
    private float tension = 1;
    private float velocity;

    private long time;// helps to discover velocity
    private long timeDownClick;
    private float initialClickPosX;


    public FloatingPagerEvent(View view, View actView, int maxMovimentBasedOnPercent, MovimentCallback movimentCallback) {
        this.view = view;
        this.actView = actView;
        this.movimentCallback = movimentCallback;
        PERCENT_TO_MOVE = maxMovimentBasedOnPercent;
        this.view.setOnTouchListener(this);

    }


    @Override
    public boolean onTouch(View v, MotionEvent event) {
        v.performClick();
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                handleDownAction(v, event);
                break;
            case MotionEvent.ACTION_MOVE:
                handleMoveAction(v, event);
                break;

            case MotionEvent.ACTION_UP:

                handleUpAction(v, event);
                break;
        }


        return stealEvent;
    }

    private void handleUpAction(View v, MotionEvent event) {
        calculateVelocity();

        initialTouchY = 0;
        initialTouchX = 0;

        float finalClickPosX = event.getX();
        float mov = (finalClickPosX - initialClickPosX);
        if (mov < 0) mov = mov * -1;

        int movementLimit = 20;

        if ((System.currentTimeMillis() - timeDownClick <= 100) && mov < movementLimit) {
            if (movimentCallback != null) movimentCallback.onClick();
        } else if (movimentCallback != null) movimentCallback.userRelease();

        //    if (movimentCallback != null)
        //     movimentCallback.positionChanged(initialTouchX, initialTouchY, percent(v.getY(), maxMovimentUp), percent(v.getY(), maxMovimentDown));
        tension = 1;
        animate(v);

    }

    private void handleMoveAction(View v, MotionEvent event) {
        /*para a animação que balança suavemente a vie dando a impressão de que esta flutuando no ar*/
        v.clearAnimation();
        viewIsUping = v.getY() - initialViewY < 0;
        moveY(event.getY(), v);
        if (movimentCallback != null) {
            movimentCallback.positionChanged(event.getX() - initialTouchX, view.getY() - initialViewY, percent(v.getY(), maxMovimentUp), percent(v.getY(), maxMovimentDown));
        }
    }

    private void handleDownAction(View v, MotionEvent event) {
        this.view.performClick();
        time = System.currentTimeMillis();
        timeDownClick = System.currentTimeMillis();

        initialTouchX = event.getX();
        initialTouchY = event.getY();

        initialClickPosX = event.getX();


        if (initialViewY == -1) initVars(this.view);
    }

    private void calculateVelocity() {
        time = System.currentTimeMillis() - time;

        float currViewY = (view.getY() < 0 ? -view.getY() : view.getY());
        float distance = (currViewY - initialViewY);
        velocity = (int) (distance / time);
    }


    private void moveY(float y, View v) {
        if (!motionEnabled) return;

        float yRunnedByFinger = initialTouchY - y;
        float yToRun = (v.getY() - yRunnedByFinger) - tension;

        //impede que a tensão fique tão forte a ponto de fazer a view andar pra trás
        if ((tension + TENSION_INCREMENTER) < (yToRun - TENSION_INCREMENTER))
            // modifica a tensão para qd a view subir e descer
            if (viewIsUping) tension -= TENSION_INCREMENTER;
            else tension += TENSION_INCREMENTER;


        if (yToRun >= maxMovimentUp && yToRun <= maxMovimentDown) {
            v.setY(yToRun);
        }

        if (actView != null) {
            /*descubro qts % o dialogo se moveu e uso esse valor para obter a quantidade proporcional de pixeis da view
             * da activity compradando sua posição com o seu movimento maximo permitido*/
            float movPercent = getViewMovimentPercent(v);
            float moveFinal = initialActivityViewY + percentValue(maxMovimentActView, (int) movPercent);
            actView.setY(moveFinal);

        }

    }

    /**
     * @param v Determina se a view foi arrastada na posição correte e pela distância suficiente para ser lançada
     *          e faz as animções necessárias
     */
    private void animate(final View v) {
        float y = v.getY();
        float movimentPercent = getViewMovimentPercent(v);

        long duration = (long) (viewIsUping ? (initialViewY - y) : (y - initialViewY));

        if (duration > MAX_ANIMATION_TIME_MILLIS) duration = MAX_ANIMATION_TIME_MILLIS;
        if (duration < MIN_ANIMATION_TIME_MILLIS) duration = MIN_ANIMATION_TIME_MILLIS;
        if (movimentPercent > 70 && viewIsUping || movimentPercent <= 70 && !viewIsUping)
            duration = ((duration / 10) * 7);

        ValueAnimator moveAnim, moveAnimActivityView;


        // view será lançada
        if (velocity > 2 && y <= MIN_MOV_TO_LAUNCH_UP || y >= MIN_MOV_TO_LAUNCH_DOWN) {
            moveAnim = ValueAnimator.ofFloat(y, viewIsUping ? -viewHeigth : viewHeigth);
            moveAnim.setDuration(duration);
            if (movimentCallback != null)
                movimentCallback.viewSwiped(viewIsUping);
        } else {
            moveAnim = ValueAnimator.ofFloat(y, initialViewY);
            duration = (duration / 3) * 2;
            moveAnim.setDuration(duration);
            if (movimentCallback != null) movimentCallback.rollingBackAnimation();
        }


        ValueAnimator.AnimatorUpdateListener mUpdateListener = new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                float val = (float) valueAnimator.getAnimatedValue();
                v.setY(val);
            }
        };


        moveAnim.addUpdateListener(mUpdateListener);


        /*Aplico a tensão correspondete a distância percorrida pela view.
         * Que a minha professora de física me perdoe!*/
        float tension = movimentPercent / 15;
        tension = tension < 0 ? tension * -1 : tension;
        moveAnim.setInterpolator(new OvershootInterpolator(tension));

        // A view da activity precisa voltar pro lugar tbm
        if (actView != null) {

            ValueAnimator.AnimatorUpdateListener mActivityViewUpdateListener = new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator valueAnimator) {
                    float val = (float) valueAnimator.getAnimatedValue();
                    actView.setY(val);
                }
            };

            moveAnimActivityView = ValueAnimator.ofFloat(actView.getY(), initialActivityViewY);
            moveAnimActivityView.setDuration(duration);
            moveAnimActivityView.setInterpolator(new OvershootInterpolator(tension));
            moveAnimActivityView.addUpdateListener(mActivityViewUpdateListener);
            moveAnimActivityView.start();
        }


        moveAnim.start();

    }

    private void initVars(View v) {

        initialViewY = v.getY();
        viewHeigth = v.getMeasuredHeight();
        maxMovimentDown = initialViewY + percentValue(v.getMeasuredHeight(), PERCENT_TO_MOVE);
        maxMovimentUp = initialViewY - percentValue(v.getMeasuredHeight(), PERCENT_TO_MOVE);
        MIN_MOV_TO_LAUNCH_UP = (int) percentValue((initialViewY - v.getMeasuredHeight()), 60);
        MIN_MOV_TO_LAUNCH_DOWN = (int) percentValue((initialViewY + v.getMeasuredHeight()), 60);

        if (actView != null) {
            maxMovimentActView = actView.getY() - percentValue(v.getMeasuredHeight(), 30);
            initialActivityViewY = actView.getY();
        }


    }

    /**
     * @param target t
     * @param value  view
     * @return quantos % value é de target.
     */
    private float percent(float value, float target) {
        return new BigDecimal(value).divide(new BigDecimal(target), 2, RoundingMode.HALF_DOWN).multiply(new BigDecimal(100)).floatValue();
    }

    /**
     * @param v view
     * @return a porcentegem de movimento que a view fez do seu ponto inicial até o ponto maximo de movimento permitido
     */
    private float getViewMovimentPercent(View v) {
        /*ao remover initialViewY de view.gety() descubro o quanto a view andou. Valores >0 indicam que a view subiu
         * ao remover initialViewY de maxMovimentUp descubro quantos px a view tem que andar da sua posição inicial até a posição
         * maxima de animação, assim descubro em quantos % a view andou apartir de seu ponto de origem até o ponto de movimento final
         *
         * A VARIAVEL maxMovimentUp  foi usada como referencia pq reotnou uma porcentagem melhor
         * do que a maxMovimentDown mesmo q na lógica elas tenha um valor bem parecido*/
        return percent(initialViewY - v.getY(), initialViewY - maxMovimentUp);

    }

    /**
     * @param target  t
     * @param percent p
     * @return o qual o valor correspondente a porcentagem recebida
     * <p>
     * EX: target =150 & percentValue = 50 return será 75
     */
    private float percentValue(float target, int percent) {
        return new BigDecimal(percent).multiply(new BigDecimal(target)).divide(new BigDecimal(100), 2, RoundingMode.HALF_DOWN).floatValue();
    }

    public void setMotionEnabled(boolean motionEnabled) {
        this.motionEnabled = motionEnabled;
    }


    public interface MovimentCallback {
        void viewSwiped(boolean up);

        void positionChanged(float posX, float posY, float percentToLaunchUp, float percentToLaunchDown);

        void userRelease();

        void onClick();

        /**
         * User has released the view that got no sufficient movement to be launched
         * so back to its original position
         */
        void rollingBackAnimation();
    }


}
