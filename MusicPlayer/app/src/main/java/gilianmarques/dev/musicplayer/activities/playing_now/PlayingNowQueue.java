package gilianmarques.dev.musicplayer.activities.playing_now;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.animation.FastOutSlowInInterpolator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.text.InputType;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.squareup.picasso.Picasso;

import java.util.Collections;

import es.dmoral.toasty.Toasty;
import gilianmarques.dev.musicplayer.R;
import gilianmarques.dev.musicplayer.activities.MyActivity;
import gilianmarques.dev.musicplayer.adapters.AnimatedRvAdapter;
import gilianmarques.dev.musicplayer.adapters.dynamic.MovementsCallback;
import gilianmarques.dev.musicplayer.adapters.dynamic.RecyclerViewDragEventsHelper;
import gilianmarques.dev.musicplayer.mediaplayer.service_and_related.MusicService;
import gilianmarques.dev.musicplayer.mediaplayer.structure.MusicPlayer;
import gilianmarques.dev.musicplayer.models.Playlist;
import gilianmarques.dev.musicplayer.models.Track;
import gilianmarques.dev.musicplayer.models.TrackRef;
import gilianmarques.dev.musicplayer.movement.DragUpDown;
import gilianmarques.dev.musicplayer.persistence.UIRealm;
import gilianmarques.dev.musicplayer.utils.AdapterUtils;
import gilianmarques.dev.musicplayer.utils.App;
import gilianmarques.dev.musicplayer.utils.IdUtils;
import gilianmarques.dev.musicplayer.utils.MenuActions;
import gilianmarques.dev.musicplayer.utils.Utils;
import io.realm.Realm;


public class PlayingNowQueue extends MyActivity implements Runnable {
    private RelativeLayout DraggableParent;
    private static MusicPlayer player = MusicService.binder.getPlayer();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_playing_now_playlist);
        final RecyclerView rv = findViewById(R.id.RvPlaylist);
        DraggableParent = findViewById(R.id.DraggableParent);


        new TracksAdapter.Builder(rv)
                .setCallback(new TracksAdapter.Callback() {
                    @Override
                    public void onTrackClicked(int position) {
                        player.playFrom(position);
                    }

                }).attach().build();


        rv.setLayoutManager(new LinearLayoutManager(this));

        Runnable mRunnable = new Runnable() {
            @Override
            public void run() {
                if (player.isShuffling()) {
                    Track curr = player.getCurrentTrack();
                    for (TrackRef track : player.getQueue()) {
                        if (track.getId() == curr.getId()) {
                            rv.smoothScrollToPosition(player.getQueue().indexOf(track));
                            break;
                        }
                    }

                } else
                    rv.smoothScrollToPosition(player.getCurrentTrack().getIndex());
            }
        };
        new Handler().postDelayed(mRunnable, 250);

        ConstraintLayout.LayoutParams params = (ConstraintLayout.LayoutParams) DraggableParent.getLayoutParams();
        params.topMargin = (int) (Utils.screenHeight / 10);
        DraggableParent.setLayoutParams(params);
        DraggableParent.post(this);

        findViewById(R.id.ivNewPlaylist).setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                MaterialDialog dialog = new MaterialDialog.Builder(PlayingNowQueue.this)
                        .title(R.string.Nova_playlist)
                        .input(PlayingNowQueue.this.getString(R.string.Nome_da_playlist), "", false, new MaterialDialog.InputCallback() {
                            @Override
                            public void onInput(@NonNull final MaterialDialog dialog, final CharSequence input) {


                                UIRealm.get().executeTransaction(new Realm.Transaction() {
                                    @Override
                                    public void execute(@NonNull Realm realm) {

                                        final Playlist nPlaylist = realm.copyToRealm(new Playlist(IdUtils.createStringObjectId()));

                                        Toasty.success(PlayingNowQueue.this, getString(R.string.Playlist_criada)).show();

                                        nPlaylist.setName(IdUtils.removeAllButLetterAndNumber(input.toString()));

                                        for (final TrackRef track : player.getQueue())
                                            nPlaylist.addTrack(track.getTrack());

                                        Toasty.success(PlayingNowQueue.this, Utils.format(Utils.toPlural(nPlaylist.getTracks(false).size(), R.plurals.Faixas_adicionadas), String.valueOf(nPlaylist.getTracks(false).size()))).show();

                                        dialog.dismiss();
                                    }
                                });


                            }
                        }).inputType(InputType.TYPE_TEXT_FLAG_AUTO_COMPLETE)
                        .positiveText(R.string.Concluir)
                        .build();
                dialog.show();
            }
        });
    }

    @Override protected void applyTheme(Window mWindow, View decorView) {
        setTheme(darkTheme ? R.style.AppThemeDark_Translucent_Full : R.style.AppThemeLight_Translucent_Full);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
        mWindow.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        mWindow.addFlags(View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
        mWindow.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);

    }


    @Override public void run() {
        DraggableParent.setOnTouchListener(new DragUpDown(Utils.screenHeight + MyActivity.navigationHeight, (int) DraggableParent.getY(), new DragUpDown.callback() {
            @Override public void onTargetDismissed() {
                finish();
                super.onTargetDismissed();
            }
        }));

        ValueAnimator anim = ValueAnimator.ofFloat(Utils.screenHeight + MyActivity.navigationHeight, DraggableParent.getY());
        anim.setDuration(350);
        anim.setInterpolator(new FastOutSlowInInterpolator());

        anim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override public void onAnimationUpdate(ValueAnimator animation) {
                DraggableParent.setY((Float) animation.getAnimatedValue());
            }
        });
        anim.start();
    }

    @Override public void onBackPressed() {
        ValueAnimator anim = ValueAnimator.ofFloat(DraggableParent.getY(), Utils.screenHeight + MyActivity.navigationHeight);
        anim.setDuration(350);
        anim.setInterpolator(new FastOutSlowInInterpolator());

        anim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override public void onAnimationUpdate(ValueAnimator animation) {
                DraggableParent.setY((Float) animation.getAnimatedValue());
            }
        });
        anim.addListener(new AnimatorListenerAdapter() {
            @Override public void onAnimationEnd(Animator animation) {

                PlayingNowQueue.super.onBackPressed();

                super.onAnimationEnd(animation);
            }
        });
        anim.start();

    }

    //==================================================================================
    //======================================ADAPTER=====================================
    //==================================================================================
    private static class TracksAdapter extends AnimatedRvAdapter {
        private Activity mActivity;
        private TracksAdapter.Callback callback;
        private ItemTouchHelper itemTouchHelper;
        private int colorPrimary, colorSecundary;

        public TracksAdapter() {
            colorPrimary = ContextCompat.getColor(App.binder.get(), R.color.text_primary_light);
            colorSecundary = ContextCompat.getColor(App.binder.get(), R.color.text_secondary_light);
        }

        @NonNull
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            return new TracksAdapter.MyViewHolder(mActivity.getLayoutInflater().inflate(R.layout.view_music_with_label, parent, false));
        }

        @SuppressLint("ClickableViewAccessibility")
        @Override
        public void onBindViewHolder(@NonNull final RecyclerView.ViewHolder holder, int position) {

            final TracksAdapter.MyViewHolder mHolder = (TracksAdapter.MyViewHolder) holder;
            final Track mTrack = player.getQueue().get(position).getTrack();

            Picasso.get().load(mTrack.getAlbum().getURI()).placeholder(R.drawable.no_art_background).into(mHolder.ivAlbumArt);


            mHolder.tvTrackArtist.setText(mTrack.getAlbumName());
            mHolder.tvTrackTitle.setText(mTrack.getTitle());
            mHolder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    callback.onTrackClicked(holder.getAdapterPosition());
                }
            });
            mHolder.ivLabel.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View view, MotionEvent motionEvent) {
                    itemTouchHelper.startDrag(mHolder);
                    return true;
                }
            });
            MenuActions.ActionsCallback callback = new MenuActions.ActionsCallback() {
                @Override public void trackRemovedFromPlayingNow(Track mTrack) {

                    boolean trackRemoved = false;
                    for (int i = 0; i < player.getQueue().size(); i++) {
                        if (player.getQueue().get(i).getId() == (mTrack.getId())) {
                            player.getQueue().remove(i);
                            Toasty.success(mActivity, mActivity.getString(R.string.Faixa_removida_da_fila), Toast.LENGTH_LONG).show();
                            trackRemoved = true;
                            notifyItemRemoved(i);
                            break;
                        }
                    }
                    if (!trackRemoved)
                        Toasty.error(mActivity, mActivity.getString(R.string.Erro), Toast.LENGTH_LONG).show();
                    super.trackRemovedFromPlayingNow(mTrack);
                }


            };

            mHolder.ivMenu.setOnClickListener(new Menu(mTrack, mHolder.ivAlbumArt).setCallback(callback));
            AdapterUtils.changeBackground(mHolder.itemView, position);
            super.onBindViewHolder(mHolder, position);
        }

        @Override
        public int getItemCount() {
            return player.getQueue().size();
        }


        private final MovementsCallback dynamicCallback = new MovementsCallback() {
            @Override
            public void onMove(RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
                move(viewHolder, target);
            }

            void move(RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {

                final int initialPosition = viewHolder.getAdapterPosition();
                final int finalPosition = target.getAdapterPosition();


                if (initialPosition < player.getQueue().size() && finalPosition < player.getQueue().size()) {
                    if (initialPosition < finalPosition) {
                        for (int i = initialPosition; i < finalPosition; i++) {
                            Collections.swap(player.getQueue(), i, i + 1);
                        }
                    } else {
                        for (int i = initialPosition; i > finalPosition; i--) {
                            Collections.swap(player.getQueue(), i, i - 1);
                        }
                    }

                    notifyItemMoved(initialPosition, finalPosition);
                    Runnable mRunnable = new Runnable() {
                        @Override
                        public void run() {
                            TrackRef ref = player.getQueue().get(finalPosition);
                            TrackRef ref2 = player.getQueue().get(initialPosition);
                            ref.setIndex(finalPosition);
                            ref2.setIndex(initialPosition);

                        }
                    };
                    new Thread(mRunnable).start();

                }
            }
        };


        class MyViewHolder extends RecyclerView.ViewHolder {

            final ImageView ivAlbumArt, ivMenu;
            final ImageView ivLabel;
            final TextView tvTrackTitle;
            final TextView tvTrackArtist;

            MyViewHolder(View itemView) {
                super(itemView);
                ivMenu = itemView.findViewById(R.id.iv_menu);
                ivAlbumArt = itemView.findViewById(R.id.iv_art);
                ivLabel = itemView.findViewById(R.id.ivLabel);
                tvTrackTitle = itemView.findViewById(R.id.tv_track_title);
                tvTrackArtist = itemView.findViewById(R.id.tv_track_artist);

                tvTrackTitle.setTextColor(colorPrimary);
                tvTrackArtist.setTextColor(colorSecundary);

                Utils.applyColorOnDrawable(colorSecundary, ivMenu.getDrawable());
                Utils.applyColorOnDrawable(colorSecundary, ivLabel.getDrawable());
            }
        }


        /**
         * CallbackI pra notificar a activity  das ações feitas nas view do recyclerView
         */
        interface Callback {
            void onTrackClicked(int position);
        }

        public static class Builder {
            private final TracksAdapter mAdapter;
            private final RecyclerView mRecyclerView;

            Builder(RecyclerView mRecyclerView) {
                this.mRecyclerView = mRecyclerView;
                mAdapter = new TracksAdapter();
            }


            TracksAdapter.Builder attach() {
                mRecyclerView.setAdapter(mAdapter);
                return this;
            }


            TracksAdapter.Builder setCallback(TracksAdapter.Callback callback) {
                mAdapter.callback = callback;
                return this;
            }

            TracksAdapter build() {
                mAdapter.mActivity = App.binder.get().getActivity();
                mAdapter.itemTouchHelper = new ItemTouchHelper(new RecyclerViewDragEventsHelper(mAdapter.dynamicCallback, false, false));
                mAdapter.itemTouchHelper.attachToRecyclerView(mRecyclerView);
                return mAdapter;
            }


        }


    }

}

