/*
 * This is the source code of Telegram for Android v. 5.x.x.
 * It is licensed under GNU GPL v. 2 or later.
 * You should have received a copy of the license in this archive (see LICENSE).
 *
 * Copyright Nikolai Kudashov, 2013-2018.
 */

package org.criargram.ui.Cells;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.view.Gravity;
import android.widget.FrameLayout;
import android.widget.ImageView;

import org.criargram.messenger.AndroidUtilities;
import org.criargram.messenger.LocaleController;
import org.criargram.messenger.MessagesController;
import org.criargram.messenger.R;
import org.criargram.messenger.UserConfig;
import org.criargram.messenger.UserObject;
import org.criargram.tgnet.ConnectionsManager;
import org.criargram.tgnet.TLRPC;
import org.criargram.ui.ActionBar.SimpleTextView;
import org.criargram.ui.ActionBar.Theme;
import org.criargram.ui.Components.AvatarDrawable;
import org.criargram.ui.Components.BackupImageView;
import org.criargram.ui.Components.LayoutHelper;

public class ManageChatUserCell extends FrameLayout {

    private BackupImageView avatarImageView;
    private SimpleTextView nameTextView;
    private SimpleTextView statusTextView;
    private ImageView optionsButton;

    private AvatarDrawable avatarDrawable;
    private TLRPC.User currentUser;

    private CharSequence currentName;
    private CharSequence currrntStatus;

    private String lastName;
    private int lastStatus;
    private TLRPC.FileLocation lastAvatar;
    private boolean isAdmin;

    private boolean needDivider;

    private int statusColor;
    private int statusOnlineColor;

    private int namePadding;

    private int currentAccount = UserConfig.selectedAccount;

    private ManageChatUserCellDelegate delegate;

    public interface ManageChatUserCellDelegate {
        boolean onOptionsButtonCheck(ManageChatUserCell cell, boolean click);
    }

    public ManageChatUserCell(Context context, int avatarPadding, int nPadding, boolean needOption) {
        super(context);

        statusColor = Theme.getColor(Theme.key_windowBackgroundWhiteGrayText);
        statusOnlineColor = Theme.getColor(Theme.key_windowBackgroundWhiteBlueText);

        namePadding = nPadding;

        avatarDrawable = new AvatarDrawable();

        avatarImageView = new BackupImageView(context);
        avatarImageView.setRoundRadius(AndroidUtilities.dp(23));
        addView(avatarImageView, LayoutHelper.createFrame(46, 46, (LocaleController.isRTL ? Gravity.RIGHT : Gravity.LEFT) | Gravity.TOP, LocaleController.isRTL ? 0 : 7 + avatarPadding, 8, LocaleController.isRTL ? 7 + avatarPadding : 0, 0));

        nameTextView = new SimpleTextView(context);
        nameTextView.setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteBlackText));
        nameTextView.setTextSize(17);
        nameTextView.setTypeface(AndroidUtilities.getTypeface("fonts/rmedium.ttf"));
        nameTextView.setGravity((LocaleController.isRTL ? Gravity.RIGHT : Gravity.LEFT) | Gravity.TOP);
        addView(nameTextView, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, 20, (LocaleController.isRTL ? Gravity.RIGHT : Gravity.LEFT) | Gravity.TOP, LocaleController.isRTL ? 28 + 18 : (68 + namePadding), 11.5f, LocaleController.isRTL ? (68 + namePadding) : 28 + 18, 0));

        statusTextView = new SimpleTextView(context);
        statusTextView.setTextSize(14);
        statusTextView.setGravity((LocaleController.isRTL ? Gravity.RIGHT : Gravity.LEFT) | Gravity.TOP);
        addView(statusTextView, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, 20, (LocaleController.isRTL ? Gravity.RIGHT : Gravity.LEFT) | Gravity.TOP, LocaleController.isRTL ? 28 : (68 + namePadding), 34.5f, LocaleController.isRTL ? (68 + namePadding) : 28, 0));

        if (needOption) {
            optionsButton = new ImageView(context);
            optionsButton.setFocusable(false);
            optionsButton.setBackgroundDrawable(Theme.createSelectorDrawable(Theme.getColor(Theme.key_stickers_menuSelector)));
            optionsButton.setImageResource(R.drawable.ic_ab_other);
            optionsButton.setColorFilter(new PorterDuffColorFilter(Theme.getColor(Theme.key_stickers_menu), PorterDuff.Mode.MULTIPLY));
            optionsButton.setScaleType(ImageView.ScaleType.CENTER);
            addView(optionsButton, LayoutHelper.createFrame(52, 64, (LocaleController.isRTL ? Gravity.LEFT : Gravity.RIGHT) | Gravity.TOP));
            optionsButton.setOnClickListener(v -> delegate.onOptionsButtonCheck(ManageChatUserCell.this, true));
        }
    }

    public void setData(TLRPC.User user, CharSequence name, CharSequence status, boolean divider) {
        if (user == null) {
            currrntStatus = null;
            currentName = null;
            currentUser = null;
            nameTextView.setText("");
            statusTextView.setText("");
            avatarImageView.setImageDrawable(null);
            return;
        }
        currrntStatus = status;
        currentName = name;
        currentUser = user;
        if (optionsButton != null) {
            boolean visible = delegate.onOptionsButtonCheck(ManageChatUserCell.this, false);
            optionsButton.setVisibility(visible ? VISIBLE : INVISIBLE);
            nameTextView.setLayoutParams(LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, 20, (LocaleController.isRTL ? Gravity.RIGHT : Gravity.LEFT) | Gravity.TOP, LocaleController.isRTL ? (visible ? 46 : 28) : (68 + namePadding), status == null || status.length() > 0 ? 11.5f : 20.5f, LocaleController.isRTL ? (68 + namePadding) : (visible ? 46 : 28), 0));
            statusTextView.setLayoutParams(LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, 20, (LocaleController.isRTL ? Gravity.RIGHT : Gravity.LEFT) | Gravity.TOP, LocaleController.isRTL ? (visible ? 46 : 28) : (68 + namePadding), 34.5f, LocaleController.isRTL ? (68 + namePadding) : (visible ? 46 : 28), 0));
        }
        needDivider = divider;
        setWillNotDraw(!needDivider);
        update(0);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(MeasureSpec.makeMeasureSpec(MeasureSpec.getSize(widthMeasureSpec), MeasureSpec.EXACTLY), MeasureSpec.makeMeasureSpec(AndroidUtilities.dp(64) + (needDivider ? 1 : 0), MeasureSpec.EXACTLY));
    }

    public void setStatusColors(int color, int onlineColor) {
        statusColor = color;
        statusOnlineColor = onlineColor;
    }

    public void setIsAdmin(boolean value) {
        isAdmin = value;
    }

    public void update(int mask) {
        if (currentUser == null) {
            return;
        }
        TLRPC.FileLocation photo = null;
        String newName = null;
        if (currentUser.photo != null) {
            photo = currentUser.photo.photo_small;
        }

        if (mask != 0) {
            boolean continueUpdate = false;
            if ((mask & MessagesController.UPDATE_MASK_AVATAR) != 0) {
                if (lastAvatar != null && photo == null || lastAvatar == null && photo != null && lastAvatar != null && photo != null && (lastAvatar.volume_id != photo.volume_id || lastAvatar.local_id != photo.local_id)) {
                    continueUpdate = true;
                }
            }
            if (currentUser != null && !continueUpdate && (mask & MessagesController.UPDATE_MASK_STATUS) != 0) {
                int newStatus = 0;
                if (currentUser.status != null) {
                    newStatus = currentUser.status.expires;
                }
                if (newStatus != lastStatus) {
                    continueUpdate = true;
                }
            }
            if (!continueUpdate && currentName == null && lastName != null && (mask & MessagesController.UPDATE_MASK_NAME) != 0) {
                newName = UserObject.getUserName(currentUser);
                if (!newName.equals(lastName)) {
                    continueUpdate = true;
                }
            }
            if (!continueUpdate) {
                return;
            }
        }

        avatarDrawable.setInfo(currentUser);
        if (currentUser.status != null) {
            lastStatus = currentUser.status.expires;
        } else {
            lastStatus = 0;
        }

        if (currentName != null) {
            lastName = null;
            nameTextView.setText(currentName);
        } else {
            lastName = newName == null ? UserObject.getUserName(currentUser) : newName;
            nameTextView.setText(lastName);
        }
        if (currrntStatus != null) {
            statusTextView.setTextColor(statusColor);
            statusTextView.setText(currrntStatus);
        } else if (currentUser != null) {
            if (currentUser.bot) {
                statusTextView.setTextColor(statusColor);
                if (currentUser.bot_chat_history || isAdmin) {
                    statusTextView.setText(LocaleController.getString("BotStatusRead", R.string.BotStatusRead));
                } else {
                    statusTextView.setText(LocaleController.getString("BotStatusCantRead", R.string.BotStatusCantRead));
                }
            } else {
                if (currentUser.id == UserConfig.getInstance(currentAccount).getClientUserId() || currentUser.status != null && currentUser.status.expires > ConnectionsManager.getInstance(currentAccount).getCurrentTime() || MessagesController.getInstance(currentAccount).onlinePrivacy.containsKey(currentUser.id)) {
                    statusTextView.setTextColor(statusOnlineColor);
                    statusTextView.setText(LocaleController.getString("Online", R.string.Online));
                } else {
                    statusTextView.setTextColor(statusColor);
                    statusTextView.setText(LocaleController.formatUserStatus(currentAccount, currentUser));
                }
            }
        }
        avatarImageView.setImage(photo, "50_50", avatarDrawable, currentUser);
    }

    public void recycle() {
        avatarImageView.getImageReceiver().cancelLoadImage();
    }

    public void setDelegate(ManageChatUserCellDelegate manageChatUserCellDelegate) {
        delegate = manageChatUserCellDelegate;
    }

    public TLRPC.User getCurrentUser() {
        return currentUser;
    }

    @Override
    public boolean hasOverlappingRendering() {
        return false;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (needDivider) {
            canvas.drawLine(LocaleController.isRTL ? 0 : AndroidUtilities.dp(68), getMeasuredHeight() - 1, getMeasuredWidth() - (LocaleController.isRTL ? AndroidUtilities.dp(68) : 0), getMeasuredHeight() - 1, Theme.dividerPaint);
        }
    }
}
