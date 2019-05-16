/*
 * This is the source code of Telegram for Android v. 5.x.x.
 * It is licensed under GNU GPL v. 2 or later.
 * You should have received a copy of the license in this archive (see LICENSE).
 *
 * Copyright Nikolai Kudashov, 2013-2018.
 */

package org.criargram.ui.Adapters;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;

import org.criargram.messenger.AndroidUtilities;
import org.criargram.messenger.ContactsController;
import org.criargram.messenger.LocaleController;
import org.criargram.messenger.MessagesController;
import org.criargram.messenger.R;
import org.criargram.messenger.UserConfig;
import org.criargram.messenger.support.widget.RecyclerView;
import org.criargram.tgnet.TLObject;
import org.criargram.tgnet.TLRPC;
import org.criargram.ui.ActionBar.Theme;
import org.criargram.ui.Cells.DialogCell;
import org.criargram.ui.Cells.DialogMeUrlCell;
import org.criargram.ui.Cells.DialogsEmptyCell;
import org.criargram.ui.Cells.HeaderCell;
import org.criargram.ui.Cells.LoadingCell;
import org.criargram.ui.Cells.ShadowSectionCell;
import org.criargram.ui.Cells.UserCell;
import org.criargram.ui.Components.CombinedDrawable;
import org.criargram.ui.Components.LayoutHelper;
import org.criargram.ui.Components.RecyclerListView;
import org.criargram.ui.DialogsActivity;

import java.util.ArrayList;

public class DialogsAdapter extends RecyclerListView.SelectionAdapter {

    private Context mContext;
    private int dialogsType;
    private long openedDialogId;
    private int currentCount;
    private boolean isOnlySelect;
    private ArrayList<Long> selectedDialogs;
    private boolean hasHints;
    private int currentAccount = UserConfig.selectedAccount;
    private boolean showContacts;

    public DialogsAdapter(Context context, int type, boolean onlySelect) {
        mContext = context;
        dialogsType = type;
        isOnlySelect = onlySelect;
        hasHints = type == 0 && !onlySelect;
        if (onlySelect) {
            selectedDialogs = new ArrayList<>();
        }
    }

    public void setOpenedDialogId(long id) {
        openedDialogId = id;
    }

    public boolean hasSelectedDialogs() {
        return selectedDialogs != null && !selectedDialogs.isEmpty();
    }

    public void addOrRemoveSelectedDialog(long did, View cell) {
        if (selectedDialogs.contains(did)) {
            selectedDialogs.remove(did);
            if (cell instanceof DialogCell) {
                ((DialogCell) cell).setChecked(false, true);
            }
        } else {
            selectedDialogs.add(did);
            if (cell instanceof DialogCell) {
                ((DialogCell) cell).setChecked(true, true);
            }
        }
    }

    public ArrayList<Long> getSelectedDialogs() {
        return selectedDialogs;
    }

    public boolean isDataSetChanged() {
        int current = currentCount;
        return current != getItemCount() || current == 1;
    }

    @Override
    public int getItemCount() {
        showContacts = false;
        ArrayList<TLRPC.TL_dialog> array = DialogsActivity.getDialogsArray(dialogsType, currentAccount);
        int dialogsCount = array.size();
        if (dialogsCount == 0 && MessagesController.getInstance(currentAccount).loadingDialogs) {
            return 0;
        }
        int count = dialogsCount;
        if (!MessagesController.getInstance(currentAccount).dialogsEndReached || dialogsCount == 0) {
            count++;
        }
        if (hasHints) {
            count += 2 + MessagesController.getInstance(currentAccount).hintDialogs.size();
        } else if (dialogsType == 0 && dialogsCount == 0) {
            if (ContactsController.getInstance(currentAccount).contacts.isEmpty() && ContactsController.getInstance(currentAccount).isLoadingContacts()) {
                return 0;
            }
            if (!ContactsController.getInstance(currentAccount).contacts.isEmpty()) {
                count += ContactsController.getInstance(currentAccount).contacts.size() + 2;
                showContacts = true;
            }
        }
        currentCount = count;
        return count;
    }

    public TLObject getItem(int i) {
        if (showContacts) {
            i -= 3;
            if (i < 0 || i >= ContactsController.getInstance(currentAccount).contacts.size()) {
                return null;
            }
            return MessagesController.getInstance(currentAccount).getUser(ContactsController.getInstance(currentAccount).contacts.get(i).user_id);
        }
        ArrayList<TLRPC.TL_dialog> arrayList = DialogsActivity.getDialogsArray(dialogsType, currentAccount);
        if (hasHints) {
            int count = MessagesController.getInstance(currentAccount).hintDialogs.size();
            if (i < 2 + count) {
                return MessagesController.getInstance(currentAccount).hintDialogs.get(i - 1);
            } else {
                i -= count + 2;
            }
        }
        if (i < 0 || i >= arrayList.size()) {
            return null;
        }
        return arrayList.get(i);
    }

    @Override
    public void notifyDataSetChanged() {
        hasHints = dialogsType == 0 && !isOnlySelect && !MessagesController.getInstance(currentAccount).hintDialogs.isEmpty();
        super.notifyDataSetChanged();
    }

    @Override
    public void onViewAttachedToWindow(RecyclerView.ViewHolder holder) {
        if (holder.itemView instanceof DialogCell) {
            ((DialogCell) holder.itemView).checkCurrentDialogIndex();
        }
    }

    @Override
    public boolean isEnabled(RecyclerView.ViewHolder holder) {
        int viewType = holder.getItemViewType();
        return viewType != 1 && viewType != 5 && viewType != 3 && viewType != 8 && viewType != 7;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        View view;
        switch (viewType) {
            case 0:
                view = new DialogCell(mContext, isOnlySelect);
                break;
            case 1:
                view = new LoadingCell(mContext);
                break;
            case 2: {
                HeaderCell headerCell = new HeaderCell(mContext);
                headerCell.setText(LocaleController.getString("RecentlyViewed", R.string.RecentlyViewed));

                TextView textView = new TextView(mContext);
                textView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 15);
                textView.setTypeface(AndroidUtilities.getTypeface("fonts/rmedium.ttf"));
                textView.setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteBlueHeader));
                textView.setText(LocaleController.getString("RecentlyViewedHide", R.string.RecentlyViewedHide));
                textView.setGravity((LocaleController.isRTL ? Gravity.LEFT : Gravity.RIGHT) | Gravity.CENTER_VERTICAL);
                headerCell.addView(textView, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.MATCH_PARENT, (LocaleController.isRTL ? Gravity.LEFT : Gravity.RIGHT) | Gravity.TOP, 17, 15, 17, 0));
                textView.setOnClickListener(view1 -> {
                    MessagesController.getInstance(currentAccount).hintDialogs.clear();
                    SharedPreferences preferences = MessagesController.getGlobalMainSettings();
                    preferences.edit().remove("installReferer").commit();
                    notifyDataSetChanged();
                });

                view = headerCell;
                break;
            }
            case 3:
                FrameLayout frameLayout = new FrameLayout(mContext) {
                    @Override
                    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
                        super.onMeasure(MeasureSpec.makeMeasureSpec(MeasureSpec.getSize(widthMeasureSpec), MeasureSpec.EXACTLY), MeasureSpec.makeMeasureSpec(AndroidUtilities.dp(12), MeasureSpec.EXACTLY));
                    }
                };
                frameLayout.setBackgroundColor(Theme.getColor(Theme.key_windowBackgroundGray));
                View v = new View(mContext);
                v.setBackgroundDrawable(Theme.getThemedDrawable(mContext, R.drawable.greydivider, Theme.key_windowBackgroundGrayShadow));
                frameLayout.addView(v, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.MATCH_PARENT));
                view = frameLayout;
                break;
            case 4:
                view = new DialogMeUrlCell(mContext);
                break;
            case 5:
                view = new DialogsEmptyCell(mContext);
                break;
            case 6:
                view = new UserCell(mContext, 8, 0, false);
                break;
            case 7:
                HeaderCell headerCell = new HeaderCell(mContext);
                headerCell.setText(LocaleController.getString("YourContacts", R.string.YourContacts));
                view = headerCell;
                break;
            case 8:
            default:
                view = new ShadowSectionCell(mContext);
                Drawable drawable = Theme.getThemedDrawable(mContext, R.drawable.greydivider, Theme.key_windowBackgroundGrayShadow);
                CombinedDrawable combinedDrawable = new CombinedDrawable(new ColorDrawable(Theme.getColor(Theme.key_windowBackgroundGray)), drawable);
                combinedDrawable.setFullsize(true);
                view.setBackgroundDrawable(combinedDrawable);
                break;
        }
        view.setLayoutParams(new RecyclerView.LayoutParams(RecyclerView.LayoutParams.MATCH_PARENT, viewType == 5 ? RecyclerView.LayoutParams.MATCH_PARENT : RecyclerView.LayoutParams.WRAP_CONTENT));
        return new RecyclerListView.Holder(view);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int i) {
        switch (holder.getItemViewType()) {
            case 0: {
                DialogCell cell = (DialogCell) holder.itemView;
                TLRPC.TL_dialog dialog = (TLRPC.TL_dialog) getItem(i);
                TLRPC.TL_dialog nextDialog = (TLRPC.TL_dialog) getItem(i + 1);
                if (hasHints) {
                    i -= 2 + MessagesController.getInstance(currentAccount).hintDialogs.size();
                }
                cell.useSeparator = (i != getItemCount() - 1);
                cell.fullSeparator = dialog.pinned && nextDialog != null && !nextDialog.pinned;
                if (dialogsType == 0) {
                    if (AndroidUtilities.isTablet()) {
                        cell.setDialogSelected(dialog.id == openedDialogId);
                    }
                }
                if (selectedDialogs != null) {
                    cell.setChecked(selectedDialogs.contains(dialog.id), false);
                }
                cell.setDialog(dialog, i, dialogsType);
                break;
            }
            case 5: {
                DialogsEmptyCell cell = (DialogsEmptyCell) holder.itemView;
                cell.setType(showContacts ? 1 : 0);
                break;
            }
            case 4: {
                DialogMeUrlCell cell = (DialogMeUrlCell) holder.itemView;
                cell.setRecentMeUrl((TLRPC.RecentMeUrl) getItem(i));
                break;
            }
            case 6: {
                UserCell cell = (UserCell) holder.itemView;
                TLRPC.User user = MessagesController.getInstance(currentAccount).getUser(ContactsController.getInstance(currentAccount).contacts.get(i - 3).user_id);
                cell.setData(user, null, null, 0);
                break;
            }
        }
    }

    @Override
    public int getItemViewType(int i) {
        if (showContacts) {
            if (i == 0) {
                return 5;
            } else if (i == 1) {
                return 8;
            } else if (i == 2) {
                return 7;
            } else {
                return 6;
            }
        } else if (hasHints) {
            int count = MessagesController.getInstance(currentAccount).hintDialogs.size();
            if (i < 2 + count) {
                if (i == 0) {
                    return 2;
                } else if (i == 1 + count) {
                    return 3;
                }
                return 4;
            } else {
                i -= 2 + count;
            }
        }
        if (i == DialogsActivity.getDialogsArray(dialogsType, currentAccount).size()) {
            if (!MessagesController.getInstance(currentAccount).dialogsEndReached) {
                return 1;
            } else {
                return 5;
            }
        }
        return 0;
    }
}
