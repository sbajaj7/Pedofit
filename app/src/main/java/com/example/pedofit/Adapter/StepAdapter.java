package com.example.pedofit.Adapter;

import android.content.Context;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.pedofit.Model.Steps;
import com.example.pedofit.R;

import java.util.List;


public class StepAdapter extends RecyclerView.Adapter<StepAdapter.ViewHolder> {

    private List<Steps> stepList;
    private Context context;

    public StepAdapter(List<Steps> StepList) {
        this.stepList = StepList;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.weekly_steps_recycler_list_item, parent, false);
        ViewHolder viewHolder = new ViewHolder(view);
        context = parent.getContext();
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, final int position) {
        Steps dailySteps = stepList.get(position);

        holder.tvTimestamp.setText(dailySteps.getTimeStamp());
        holder.tvStepCount.setText(context.getString(R.string.step_count, dailySteps.getSteps()));
    }

    @Override
    public int getItemCount() {
        if (stepList != null)
            return stepList.size();
        return 0;
    }

    public Steps getItem(int position) {
        return stepList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return stepList.get(position).getTimestampMilliseconds();
    }

    @Override
    public void setHasStableIds(boolean hasStableIds) {
        super.setHasStableIds(hasStableIds);
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvTimestamp;
        TextView tvStepCount;
        CardView cardView;

        public ViewHolder(View itemView) {
            super(itemView);
            tvTimestamp = itemView.findViewById(R.id.tvDate);
            tvStepCount = itemView.findViewById(R.id.tvSteps);
            cardView = itemView.findViewById(R.id.cardView);
        }

    }
}
