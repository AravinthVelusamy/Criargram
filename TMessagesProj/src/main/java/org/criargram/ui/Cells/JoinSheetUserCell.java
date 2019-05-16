/*
 * This is the source code of Telegram for Android v. 5.x.x.
 * It is licensed under GNU GPL v. 2 or later.
 * You should have received a copy of the license in this archive (see LICENSE).
 *
 * Copyright Nikolai Kudashov, 2013-2018.
 */

package org.criargram.ui.Cells;

import android.content.Context;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.Gravity;
import android.widget.FrameLayout;
import android.widget.TextView;

import org.criargram.messenger.AndroidUtilities;
import org.criargram.messenger.ContactsController;
import org.criargram.messenger.LocaleController;
import org.criargram.tgnet.TLRPC;
import org.criargram.ui.ActionBar.Theme;
import org.criargram.ui.Components.AvatarDrawable;
import org.criargram.ui.Components.BackupImageView;
import org.criargram.ui.Components.LayoutHelper;

public class JoinSheetUserCell extends FrameLayout {

    private BackupImageView imageView;
    private TextView nameTextView;
    private AvatarDrawable avatarDrawable = new AvatarDrawable();
    private int result[] = new int[1];

    public JoinSheetUserCell(Context context) {
        super(context);

        imageView = new BackupImageView(context);
        imageView.setRoundRadius(AndroidUtilities.dp(27));
        addView(imageView, LayoutHelper.createFrame(54, 54, Gravity.TOP | Gravity.CENTER_HORIZONTAL, 0, 7, 0, 0));

        nameTextView = new TextView(context);
        nameTextView.setTextColor(Theme.getColor(Theme.key_dialogTextBlack));
        nameTextView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 12);
        nameTextView.setMaxLines(1);
        nameTextView.setGravity(Gravity.TOP | Gravity.CENTER_HORIZONTAL);
        nameTextView.setLines(1);
        nameTextView.setSingleLine(true);
        nameTextView.setEllipsize(TextUtils.TruncateAt.END);
        addView(nameTextView, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT, Gravity.LEFT | Gravity.TOP, 6, 64, 6, 0));
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(MeasureSpec.makeMeasureSpec(AndroidUtilities.dp(100), MeasureSpec.EXACTLY), MeasureSpec.makeMeasureSpec(AndroidUtilities.dp(90), MeasureSpec.EXACTLY));
    }

    public void setUser(TLRPC.User user) {
        nameTextView.setText(ContactsController.formatName(user.first_name, user.last_name));
        avatarDrawable.setInfo(user);
        TLRPC.FileLocation photo = null;
        if (user != null && user.photo != null) {
            photo = user.photo.photo_small;
        }
        imageView.setImage(photo, "50_50", avatarDrawable, user);
    }

    public void setCount(int count) {
        nameTextView.setText("");
        avatarDrawable.setInfo(0, null, null, false, "+" + LocaleController.formatShortNumber(count, result));
        imageView.setImage(null, "50_50", avatarDrawable, null);
    }
}
