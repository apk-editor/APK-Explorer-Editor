package navView.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import bottomNavView.R;
import navView.serializableItems.NavViewEntry;
import com.google.android.material.button.MaterialButton;

import java.util.List;

/*
 * Created by APK Explorer & Editor <apkeditor@protonmail.com> on July 13, 2026
 */
public class NavViewAdapter extends RecyclerView.Adapter<NavViewAdapter.ViewHolder> {

    private final List<NavViewEntry> data;
    private final OnItemClickListener clickListener;
    private int selectedPosition = 0;

    public NavViewAdapter(List<NavViewEntry> data, OnItemClickListener clickListener) {
        this.data = data;
        this.clickListener = clickListener;
    }

    @NonNull
    @Override
    public NavViewAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View rowItem = LayoutInflater.from(parent.getContext()).inflate(R.layout.recycle_view_bottomnav, parent, false);
        return new NavViewAdapter.ViewHolder(rowItem);
    }

    @Override
    public void onBindViewHolder(@NonNull NavViewAdapter.ViewHolder holder, int position) {
        this.data.get(position).load(holder.button, position == selectedPosition);
    }

    @Override
    public int getItemCount() {
        return this.data.size();
    }

    public NavViewEntry getNavViewEntry(int position) {
        if (position >= 0 && position < data.size()) {
            return data.get(position);
        }
        return null;
    }

    public void setSelectedPosition(int position) {
        if (position >= 0 && position < getItemCount() && selectedPosition != position) {
            int previousPosition = selectedPosition;
            selectedPosition = position;
            notifyItemChanged(previousPosition);
            notifyItemChanged(selectedPosition);
        }
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        private final MaterialButton button;

        public ViewHolder(View view) {
            super(view);
            this.button = view.findViewById(R.id.button);

            this.button.setOnClickListener(v -> {
                int currentPosition = getBindingAdapterPosition();
                if (currentPosition == RecyclerView.NO_POSITION || currentPosition == selectedPosition) {
                    this.button.setChecked(true);
                    return;
                }

                setSelectedPosition(currentPosition);

                if (clickListener != null) {
                    clickListener.onItemClick(data.get(currentPosition).getSupplier(), currentPosition);
                }
            });
        }
    }

    public interface OnItemClickListener {
        void onItemClick(NavViewEntry.FragmentSupplier supplier, int position);
    }

}