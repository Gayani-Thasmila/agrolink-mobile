package lk.jiat.agrolink.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import lk.jiat.agrolink.R;
import lk.jiat.agrolink.model.User;

public class UserAdapter extends RecyclerView.Adapter<UserAdapter.ViewHolder> {

    private List<User> userList;

    public UserAdapter(List<User> userList) {
        this.userList = userList;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView name, email, phone, role;

        public ViewHolder(View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.txtUserName);
            email = itemView.findViewById(R.id.txtUserEmail);
            phone = itemView.findViewById(R.id.txtUserPhone);
            role = itemView.findViewById(R.id.txtUserRole);
        }
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_user, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        User user = userList.get(position);
        holder.name.setText(user.getName());
        holder.email.setText(user.getEmail());
        holder.phone.setText(user.getPhone() != null ? user.getPhone() : "N/A");
        holder.role.setText("ROLE: " + (user.getRole() != null ? user.getRole() : "USER"));
    }

    @Override
    public int getItemCount() {
        return userList.size();
    }
}
