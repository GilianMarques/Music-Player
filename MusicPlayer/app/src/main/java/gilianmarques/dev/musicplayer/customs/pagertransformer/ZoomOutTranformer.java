package gilianmarques.dev.musicplayer.customs.pagertransformer;

import android.view.View;

/**
 * Criado por Gilian Marques
 * Segunda-feira, 25 de Junho de 2018  as 20:36:11.
 *
 * @Since 0.1 Beta
 */
public class ZoomOutTranformer extends BaseTransformer {

    @Override
    protected void onTransform(View view, float position) {
        final float scale = 1f + Math.abs(position);
        view.setScaleX(scale);
        view.setScaleY(scale);
        view.setPivotX(view.getWidth() * 0.5f);
        view.setPivotY(view.getHeight() * 0.5f);
        view.setAlpha(position < -1f || position > 1f ? 0f : 1f - (scale - 1f));
        if (position == -1) {
            view.setTranslationX(view.getWidth() * -1);
        }
    }

}