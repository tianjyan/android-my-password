package com.home.young.myPassword.adapter;

import android.app.AlertDialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.BaseAdapter;
import android.widget.TextView;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

import com.home.young.myPassword.R;
import com.home.young.myPassword.activity.EditPasswordActivity;
import com.home.young.myPassword.database.PasswordDBRealm;
import com.home.young.myPassword.model.Password;

public class PasswordListAdapter  extends BaseAdapter {

    //region field
    private static final long DAY = 1000 * 60 * 60 * 24;
    private List<PasswordItem> passwords = new ArrayList<>();
    private Context context;
    private SimpleDateFormat simpleDateFormatYear = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
    private int padding;
    private PasswordDBRealm mainBinder;
    private SimpleDateFormat simpleDateFormatMonth = new SimpleDateFormat("MM-dd", Locale.getDefault());
    private String passwordGroup;
    //endregion

    //region anonymous class
    private Comparator<PasswordItem> comparator = new Comparator<PasswordItem>() {
        @Override
        public int compare(PasswordItem lhs, PasswordItem rhs) {

            long value = rhs.password.getPublish() - lhs.password.getPublish();
            if (value > 0)
                return 1;
            else if (value == 0)
                return 0;
            else
                return -1;
        }
    };
    //endregion

    //region constructor
    public PasswordListAdapter(Context context) {
        this.context = context;
        padding = dip2px(6);
    }
    //endregion

    //region function

    //region override
    @Override
    public int getCount() {
        return passwords.size();
    }

    @Override
    public PasswordItem getItem(int position) {
        return passwords.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public void notifyDataSetChanged() {
        for (PasswordItem passwordItem : passwords) {
            passwordItem.initDataString();
        }
        super.notifyDataSetChanged();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder;
        if (convertView == null) {
            viewHolder = new ViewHolder();
            convertView = LayoutInflater.from(context).inflate(R.layout.password_item, parent, false);
            convertView.setTag(viewHolder);

            viewHolder.titleView = (TextView)convertView.findViewById(R.id.password_item_title);
            viewHolder.dateView = (TextView)convertView.findViewById(R.id.password_item_date);
            viewHolder.nameView = (TextView)convertView.findViewById(R.id.password_item_name);
            viewHolder.passwordView = (TextView)convertView.findViewById(R.id.password_item_password);
            viewHolder.payPasswordView = (TextView)convertView.findViewById(R.id.password_item_pay_password);
            viewHolder.payConainer = convertView.findViewById(R.id.password_item_pay_container);
            viewHolder.noteView = (TextView)convertView.findViewById(R.id.password_item_note);
            viewHolder.noteConainer = convertView.findViewById(R.id.password_item_note_container);
            viewHolder.copyView = convertView.findViewById(R.id.password_item_copy);
            viewHolder.deleteView = convertView.findViewById(R.id.password_item_delete);
            viewHolder.editView = convertView.findViewById(R.id.password_item_edit);
            viewHolder.showOrHideView = convertView.findViewById(R.id.password_item_showOrHide);
            viewHolder.showOrHideTextView = (TextView) convertView.findViewById(R.id.password_item_showOrHide_text);
            viewHolder.copyView.setOnClickListener(viewHolder);
            viewHolder.deleteView.setOnClickListener(viewHolder);
            viewHolder.editView.setOnClickListener(viewHolder);
            viewHolder.showOrHideView.setOnClickListener(viewHolder);

        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        if (position == 0) {
            convertView.setPadding(padding, padding, padding, padding);
        } else {
            convertView.setPadding(padding, 0, padding, padding);
        }

        PasswordItem passwordItem = getItem(position);

        viewHolder.bindView(passwordItem);

        return convertView;
    }
    //endregion

    //region public
    public int dip2px(float dipValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dipValue * scale + 0.5f);
    }

    public void setData(List<Password> passwords, PasswordDBRealm mainBinder) {
        this.mainBinder = mainBinder;
        this.passwords.clear();
        for (Password password : passwords) {
            this.passwords.add(new PasswordItem(password));
        }
        Collections.sort(this.passwords, comparator);
        notifyDataSetChanged();
    }

    public void onNewPassword(Password password) {
        passwords.add(0, new PasswordItem(password));
        Collections.sort(this.passwords, comparator);
        notifyDataSetChanged();
    }

    public void onDeletePassword(String id) {
        for (int i = 0; i < passwords.size(); i++) {
            PasswordItem passwordItem = passwords.get(i);
            if (Objects.equals(passwordItem.password.getId(), id)) {
                passwords.remove(i);
                break;
            }
        }
        notifyDataSetChanged();
    }

    public void onUpdatePassword(Password newPassword) {
        boolean needSort = false;

        boolean hasFind = false;

        for (int i = 0; i < passwords.size(); i++) {
            Password oldPassword = passwords.get(i).password;
            if (Objects.equals(oldPassword.getId(), newPassword.getId())) {
                if (newPassword.getPublish() != 0)
                    oldPassword.setPublish(newPassword.getPublish());
                if (newPassword.getTitle() != null)
                    oldPassword.setTitle(newPassword.getTitle());
                if (newPassword.getUserName() != null)
                    oldPassword.setUserName(newPassword.getUserName());
                if (newPassword.getPassword() != null)
                    oldPassword.setPassword(newPassword.getPassword());
                if (newPassword.getNote() != null)
                    oldPassword.setNote(newPassword.getNote());
                if (newPassword.getPayPassword() != null)
                    oldPassword.setPayPassword(newPassword.getPayPassword());

                if (!oldPassword.getGroupName().equals(newPassword.getGroupName()))
                    passwords.remove(i);
                hasFind = true;
                break;
            }
        }

        if (!hasFind) {
            passwords.add(0, new PasswordItem(newPassword));
            needSort = true;
        }

        if (needSort)
            Collections.sort(this.passwords, comparator);
        notifyDataSetChanged();
    }

    public void setPasswordGroup(String passwordGroup) {
        this.passwordGroup = passwordGroup;
    }

    public class PasswordItem {
        public String dataString;
        public Password password;

        private PasswordItem(Password password) {
            this.password = password;
            initDataString();
        }

        public void initDataString() {
            dataString = formatDate(password.getPublish());
        }

        private String formatDate(long createDate) {
            String result;
            long currentTime = System.currentTimeMillis();
            long distance = currentTime - createDate;
            if (createDate > currentTime) {
                result = simpleDateFormatYear.format(createDate);
            } else if (distance < 1000 * 60) {
                result = context.getString(R.string.just);
            } else if (distance < 1000 * 60 * 60) {
                String dateString = context.getString(R.string.minute_ago);
                result = String.format(Locale.getDefault(), dateString, distance / (1000 * 60));
            } else if (distance < DAY) {
                String dateString = context.getString(R.string.hour_ago);
                result = String.format(Locale.getDefault(), dateString, distance / (1000 * 60 * 60));
            } else if (distance < DAY * 365) {
                result = simpleDateFormatMonth.format(createDate);
            } else {
                result = simpleDateFormatYear.format(createDate);
            }

            return result;
        }
    }
    //endregion

    //endregion

    //region nested class
    private class ViewHolder implements android.view.View.OnClickListener {

        public TextView titleView;
        public TextView dateView;
        public TextView nameView;
        public TextView passwordView;
        public TextView payPasswordView;
        public TextView noteView;
        public View noteConainer;
        public View payConainer;
        public View copyView;
        public View deleteView;
        public View editView;
        public View showOrHideView;
        private PasswordItem passwordItem;
        private TextView showOrHideTextView;
        @Override
        public void onClick(View view) {
            switch (view.getId()) {
                case R.id.password_item_copy:
                    onCopyClick();
                    break;
                case R.id.password_item_delete:
                    onDeleteClick();
                    break;
                case R.id.password_item_edit:
                    onEditClick();
                    break;
                case R.id.password_item_showOrHide:
                    onShowOrHideClick();
                    break;
                default:
                    break;
            }
        }

        private void onCopyClick() {
            AlertDialog.Builder builder = new AlertDialog.Builder(context);

            String[] item;
            String payPassword = passwordItem.password.getPayPassword();
            if(!TextUtils.isEmpty(payPassword)) {
               item =  new String[]{context.getResources().getString(R.string.copy_user_name),
                       context.getResources().getString(R.string.copy_password),
                       context.getResources().getString(R.string.copy_pay_password)};
            }
            else {
                item =  new String[]{context.getResources().getString(R.string.copy_user_name),
                        context.getResources().getString(R.string.copy_password)};
            }

            builder.setItems(item, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    switch (which) {
                        case 0:
                            // 复制名字
                            ClipboardManager cmbName = (ClipboardManager) context
                                    .getSystemService(Context.CLIPBOARD_SERVICE);
                            ClipData clipDataName = ClipData.newPlainText(null, passwordItem.password.getUserName());
                            cmbName.setPrimaryClip(clipDataName);
                            Toast.makeText(context, R.string.copy_use_name_msg, Toast.LENGTH_SHORT).show();
                            break;
                        case 1:
                            // 复制密码
                            ClipboardManager cmbPassword = (ClipboardManager) context
                                    .getSystemService(Context.CLIPBOARD_SERVICE);
                            ClipData clipData = ClipData.newPlainText(null, passwordItem.password.getPassword());
                            cmbPassword.setPrimaryClip(clipData);
                            Toast.makeText(context, R.string.copy_password_msg, Toast.LENGTH_SHORT).show();
                            break;
                        case 2:
                            //复制支付密码
                            ClipboardManager cmbPayPassword = (ClipboardManager) context
                                    .getSystemService(Context.CLIPBOARD_SERVICE);
                            ClipData clip = ClipData.newPlainText(null, passwordItem.password.getPayPassword());
                            cmbPayPassword.setPrimaryClip(clip);
                            Toast.makeText(context, R.string.copy_pay_password_msg, Toast.LENGTH_SHORT).show();
                            break;
                        default:
                            break;
                    }
                }
            });
            builder.show();
        }

        private void onEditClick() {
            Intent intent = new Intent(context, EditPasswordActivity.class);
            intent.putExtra(EditPasswordActivity.ID, passwordItem.password.getId());
            intent.putExtra(EditPasswordActivity.PASSWORD_GROUP, passwordGroup);
            context.startActivity(intent);
        }

        private void onDeleteClick() {
            AlertDialog.Builder builder = new AlertDialog.Builder(context);
            builder.setMessage(R.string.delete_password_message);
            builder.setTitle(passwordItem.password.getTitle());
            builder.setNeutralButton(R.string.yes, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    mainBinder.deletePassword(passwordItem.password.getId());
                }
            });
            builder.setNegativeButton(R.string.no, null);
            builder.show();
        }

        private void onShowOrHideClick(){
               if(passwordView.getInputType() == (EditorInfo.TYPE_TEXT_VARIATION_PASSWORD | EditorInfo.TYPE_CLASS_TEXT)){
                    passwordView.setInputType(EditorInfo.TYPE_CLASS_TEXT);

                    if(payConainer.getVisibility() == View.VISIBLE){
                        payPasswordView.setInputType(EditorInfo.TYPE_CLASS_TEXT);
                    }

                    showOrHideTextView.setText(R.string.hide);
               } else {
                   passwordView.setInputType(EditorInfo.TYPE_TEXT_VARIATION_PASSWORD | EditorInfo.TYPE_CLASS_TEXT);

                   if(payConainer.getVisibility() == View.VISIBLE){
                       payPasswordView.setInputType(EditorInfo.TYPE_TEXT_VARIATION_PASSWORD | EditorInfo.TYPE_CLASS_TEXT);
                   }

                   showOrHideTextView.setText(R.string.show);
               }
        }

        void bindView(PasswordItem passwordItem) {
            this.passwordItem = passwordItem;
            titleView.setText(passwordItem.password.getTitle());
            dateView.setText(passwordItem.dataString);
            nameView.setText(passwordItem.password.getUserName());
            passwordView.setText(passwordItem.password.getPassword());

            String note = passwordItem.password.getNote();
            if (TextUtils.isEmpty(note)) {
                noteConainer.setVisibility(View.GONE);
            } else {
                noteConainer.setVisibility(View.VISIBLE);
                noteView.setText(note);
            }

            String payPassword = passwordItem.password.getPayPassword();
            if(TextUtils.isEmpty(payPassword)) {
                payConainer.setVisibility(View.GONE);
            } else {
                payConainer.setVisibility(View.VISIBLE);
                payPasswordView.setText(payPassword);
            }
        }
    }
    //endregion
}
