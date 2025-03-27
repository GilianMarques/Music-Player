package gilianmarques.dev.musicplayer.activities.folders;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.v4.view.animation.FastOutSlowInInterpolator;
import android.support.v7.view.menu.MenuBuilder;
import android.support.v7.view.menu.MenuPopupHelper;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.pixplicity.easyprefs.library.Prefs;

import java.io.File;
import java.util.ArrayList;

import es.dmoral.toasty.Toasty;
import gilianmarques.dev.musicplayer.R;
import gilianmarques.dev.musicplayer.activities.MyActivity;
import gilianmarques.dev.musicplayer.adapters.AnimatedRvAdapter;
import gilianmarques.dev.musicplayer.customs.MyGridLayoutManager;
import gilianmarques.dev.musicplayer.mediaplayer.service_and_related.MusicService;
import gilianmarques.dev.musicplayer.models.Folder;
import gilianmarques.dev.musicplayer.models.Track;
import gilianmarques.dev.musicplayer.movement.DragUpDown;
import gilianmarques.dev.musicplayer.persistence.c;
import gilianmarques.dev.musicplayer.utils.AdapterUtils;
import gilianmarques.dev.musicplayer.utils.App;
import gilianmarques.dev.musicplayer.utils.FolderUtils;
import gilianmarques.dev.musicplayer.utils.Utils;

/**
 * @Since 0.1 Beta
 */
public class FolderActivity extends MyActivity {
    private RecyclerView rv, RvPlaylist;
    private boolean asList;
    private RelativeLayout DraggableParent;
    private boolean hasAnOpenedFolder;
    private float initialtrayY;
    private TextView tvFolderName;
    private TracksAdapter mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_folder);
        rv = findViewById(R.id.rvFolders);
        asList = Prefs.getBoolean(c.show_folders_as_list, true);

        DraggableParent = findViewById(R.id.DraggableParent);
        DraggableParent.setVisibility(View.INVISIBLE);
        ConstraintLayout.LayoutParams params = (ConstraintLayout.LayoutParams) DraggableParent.getLayoutParams();
        params.topMargin = (int) (Utils.screenHeight / 10);
        DraggableParent.setLayoutParams(params);

        DraggableParent.post(new Runnable() {
            @Override public void run() {
                initialtrayY = DraggableParent.getY();
                DraggableParent.setOnTouchListener(new DragUpDown(Utils.screenHeight + MyActivity.navigationHeight, (int) initialtrayY, new DragUpDown.callback() {
                    @Override public void onTargetDismissed() {
                        hasAnOpenedFolder = false;
                        super.onTargetDismissed();
                    }
                }));
            }
        });
        tvFolderName = findViewById(R.id.tvFolderName);
        Toolbar tb = findViewById(R.id.toolbar);
        tb.setTitle(R.string.Pastas);
        setSupportActionBar(tb);


        initAdapter();
    }

    @Override protected void applyTheme(Window mWindow, View decorView) {
        setTheme(darkTheme ? R.style.AppThemeDark : R.style.AppThemeLight);

    }

    @Override public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.popup_folders_activity, menu);
        return true;
    }

    @Override public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.change_display_mode:
                asList = !asList;
                Prefs.putBoolean(c.show_folders_as_list, asList);
                recreate();
                break;


        }
        return super.onOptionsItemSelected(item);
    }



    private void initAdapter() {
        rv.setHasFixedSize(true);

        Runnable mRunnable = new Runnable() {
            @Override
            public void run() {
                final ArrayList<Folder> folders = FolderUtils.createFolders();
                runOnUiThread(new Runnable() {
                    @Override public void run() {
                        rv.setAdapter(new FoldersAdapter(folders));

                    }
                });
            }
        };
        new Thread(mRunnable).start();

        if (asList)
            rv.setLayoutManager(new MyGridLayoutManager(AdapterUtils.RV_FOLDER_LIST_VIEW_SIZE));
        else rv.setLayoutManager(new MyGridLayoutManager(AdapterUtils.RV_FOLDER_VIEW_SIZE));


    }

    private void openFolder(final Folder folder) {
        tvFolderName.setText(folder.getName());

        TracksAdapter.Callback callback = new TracksAdapter.Callback() {
            @Override public void onTrackClicked(int position) {
                MusicService.binder.getPlayer().initFromFolder(folder.getTracks(), position, false);
            }

            @Override public void trackRemoved(Track mTrack) {
                folder.remove(mTrack.getId());
                super.trackRemoved(mTrack);
            }
        };

        if (RvPlaylist == null) {
            RvPlaylist = findViewById(R.id.RvPlaylist);
            RvPlaylist.setHasFixedSize(true);
            RvPlaylist.setLayoutManager(new MyGridLayoutManager(AdapterUtils.RV_TRACK_VIEW_SIZE));


            mAdapter = new TracksAdapter(this);
            mAdapter.setCallback(callback);
            mAdapter.update(folder.getTracks());
            RvPlaylist.setAdapter(mAdapter);
        } else {
            mAdapter.update(folder.getTracks());
            mAdapter.setCallback(callback);
        }


        findViewById(R.id.ivMenu).setOnClickListener(new Foldermenu(folder));


        ValueAnimator anim = ValueAnimator.ofFloat(Utils.screenHeight + MyActivity.navigationHeight, initialtrayY);
        anim.setDuration(350);
        anim.setInterpolator(new FastOutSlowInInterpolator());
        anim.addListener(new AnimatorListenerAdapter() {
            @Override public void onAnimationStart(Animator animation) {
                DraggableParent.setVisibility(View.VISIBLE);
                super.onAnimationStart(animation);
            }
        });

        anim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override public void onAnimationUpdate(ValueAnimator animation) {
                DraggableParent.setY((Float) animation.getAnimatedValue());
            }
        });
        anim.start();
        hasAnOpenedFolder = true;
    }

    @Override public void onBackPressed() {
        if (hasAnOpenedFolder) {
            hasAnOpenedFolder = false;
            ValueAnimator anim = ValueAnimator.ofFloat(DraggableParent.getY(), Utils.screenHeight);
            anim.setDuration(350);
            anim.setInterpolator(new FastOutSlowInInterpolator());
            anim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override public void onAnimationUpdate(ValueAnimator animation) {
                    DraggableParent.setY((Float) animation.getAnimatedValue());
                }
            });
            anim.start();
        } else super.onBackPressed();
    }

    private class Foldermenu implements View.OnClickListener, PopupMenu.OnMenuItemClickListener {

        private Folder folder;

        public Foldermenu(Folder folder) {

            this.folder = folder;
        }

        @SuppressLint("RestrictedApi") @Override public void onClick(View v) {
            PopupMenu menu = new PopupMenu(FolderActivity.this, v);
            menu.inflate(R.menu.popup_folder);
            menu.setOnMenuItemClickListener(this);
            MenuPopupHelper helper = new MenuPopupHelper(FolderActivity.this, (MenuBuilder) menu.getMenu(), v);
            helper.setForceShowIcon(true);
            helper.show();

        }

        @Override public boolean onMenuItemClick(MenuItem item) {

            switch (item.getItemId()) {
                case R.id.removeFolder:
                    confirmRemoveFolder();
                    break;

            }

            return true;
        }

        private void confirmRemoveFolder() {
            final MaterialDialog materialDialog = new MaterialDialog.Builder(FolderActivity.this)
                    .title(R.string.Deseja_mesmo_remover_esta_pasta)
                    .content(getString(R.string.Esta_acao_nao_podera))
                    .positiveText(R.string.Remover)
                    .onPositive(new MaterialDialog.SingleButtonCallback() {
                        @Override
                        public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {


                            final MaterialDialog progressDialog = new MaterialDialog.Builder(App.binder.get().getActivity())
                                    .progress(false, 100)
                                    .title(R.string.Removendo_pasta)
                                    .cancelable(false)
                                    .content(getString(R.string.Removendo_faixas)).build();

                            progressDialog.show();

                            final Runnable removeTracksRunnable = new Runnable() {
                                @Override
                                public void run() {
                                    for (Track track : folder.getTracks()) {
                                        Utils.removeTrack(track, true);
                                    }

                                    File mFile = new File(folder.getPath());
                                    final boolean success = deleteRecursive(mFile); //make sure to delete every file on the folder not only tracks

                                    FolderActivity.this.runOnUiThread(new Runnable() {
                                        @Override public void run() {
                                            if (success) {
                                                ((FoldersAdapter) rv.getAdapter()).notifyItemRemoved(folder);
                                                Toasty.success(FolderActivity.this, getString(R.string.Pasta_apagada_com_sucesso)).show();
                                            } else
                                                Toasty.error(FolderActivity.this, getString(R.string.Erro)).show();

                                            FolderActivity.this.onBackPressed();


                                            progressDialog.setContent(getString(R.string.Atualizando_biblioteca_do_app));

                                        }
                                    });
                                }

                                boolean deleteRecursive(File fileOrDirectory) {
                                    if (fileOrDirectory.isDirectory())
                                        for (File child : fileOrDirectory.listFiles())
                                            deleteRecursive(child);

                                    return fileOrDirectory.delete();
                                }
                            };

                            new Thread(removeTracksRunnable).start();

                        }
                    }).negativeText(R.string.Cancelar).cancelable(false).build();
            materialDialog.show();
        }

    }


    private class FoldersAdapter extends AnimatedRvAdapter {

        private ArrayList<Folder> folders;

        private FoldersAdapter(ArrayList<Folder> folders) {
            this.folders = folders;
        }

        @NonNull @Override
        public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            if (asList)
                return new MyViewHolder(getLayoutInflater().inflate(R.layout.view_folder_list, parent, false));
            else
                return new MyViewHolder(getLayoutInflater().inflate(R.layout.view_folder_grid, parent, false));
        }

        @Override
        public void onBindViewHolder(@NonNull RecyclerView.ViewHolder mHolder, int position) {
            MyViewHolder holder = (MyViewHolder) mHolder;

            final Folder folder = folders.get(position);
            holder.tvFolderName.setText(folder.getName());
            holder.tvInfo.setText(folder.getInfo());
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override public void onClick(View v) {
                    openFolder(folder);
                }
            });
            if (asList) AdapterUtils.changeBackground(holder.itemView, position);

            super.onBindViewHolder(mHolder, position);
        }

        @Override public int getItemCount() {
            return folders.size();
        }

        public void notifyItemRemoved(Folder folder) {
            for (int i = 0; i < folders.size(); i++) {
                if (folders.get(i).getPath().equals(folder.getPath())) {
                    folders.remove(i);
                    notifyItemRemoved(i);
                }
            }

        }


        private class MyViewHolder extends RecyclerView.ViewHolder {
            public TextView tvInfo, tvFolderName;

            public MyViewHolder(View itemView) {
                super(itemView);
                tvInfo = itemView.findViewById(R.id.tvInfo);
                tvFolderName = itemView.findViewById(R.id.tvFolderName);
                if (asList)
                    AdapterUtils.adaptForDevice((View) itemView.findViewById(R.id.iv_art).getParent(), AdapterUtils.RV_FOLDER_LIST_VIEW_SIZE, 7, false);
                else
                    AdapterUtils.adaptForDevice(itemView.findViewById(R.id.iv_art), AdapterUtils.RV_FOLDER_VIEW_SIZE, 7, false);
            }
        }
    }

}
