package itstep.learning.androidrv211;

import android.media.MediaPlayer;
import android.os.Bundle;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;

public class AnimActivity extends AppCompatActivity {

    private Animation alphaAnimation, rotate360Animation, rotate180Animation, scaleAnimation, bellAnimation;
    private MediaPlayer bellPlayer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_anim);


        alphaAnimation = AnimationUtils.loadAnimation(this, R.anim.anim_alpha);
        rotate360Animation = AnimationUtils.loadAnimation(this, R.anim.anim_rotate);
        rotate180Animation = AnimationUtils.loadAnimation(this, R.anim.anim_rotate2);
        scaleAnimation = AnimationUtils.loadAnimation(this, R.anim.anim_scale);
        bellAnimation = AnimationUtils.loadAnimation(this, R.anim.anim_bell);


        bellPlayer = MediaPlayer.create(this, R.raw.bell_sound);

        // Назначение анимаций на элементы
        findViewById(R.id.square1).setOnClickListener(v -> v.startAnimation(alphaAnimation));
        findViewById(R.id.square2).setOnClickListener(v -> v.startAnimation(rotate360Animation));
        findViewById(R.id.square3).setOnClickListener(v -> v.startAnimation(rotate180Animation));
        findViewById(R.id.square4).setOnClickListener(v -> v.startAnimation(scaleAnimation));

        // Дзвоник с анимацией и звуком
        ImageView bell = findViewById(R.id.anim_v_dz);
        bell.setOnClickListener(v -> {
            v.startAnimation(bellAnimation);
            if (bellPlayer != null) {
                bellPlayer.start();
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (bellPlayer != null) {
            bellPlayer.release();
            bellPlayer = null;
        }
    }
}
/*
 *  КОНСПЕКТ: Анімація в Android
 *
 *  Анімація — це плавна зміна властивостей елементів (View):
 *    - прозорість (alpha)
 *    - обертання (rotate)
 *    - масштабування (scale)
 *    - переміщення (translate)
 *    - комбінація (set)
 *
 *Анімації створюються у файлах res/anim/ у вигляді XML:
 *    <rotate>, <scale>, <alpha>, <translate>, <set>
 *
 *  Щоб застосувати:
 *    1. Завантажити анімацію:
 *       Animation a = AnimationUtils.loadAnimation(this, R.anim.anim_rotate);
 *    2. Запустити на View:
 *       view.startAnimation(a);
 *
 *  У проєкті реалізовано:
 *    - square1 → прозорість (alpha)
 *    - square2 → обертання на 360°
 *    - square3 → обертання на 180°
 *    - square4 → масштабування (scale)
 *    - Дзвоник → комбо-анімація (scale + rotate) + звук
 *
 * 🔊 Звук підключено через MediaPlayer:
 *    MediaPlayer bellPlayer = MediaPlayer.create(this, R.raw.bell_sound);
 *    bellPlayer.start();
 *
 *
 */