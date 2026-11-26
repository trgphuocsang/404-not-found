package com.nhom404.bachhoa.admin;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class OrdersAdapter extends RecyclerView.Adapter<OrdersAdapter.OrderViewHolder> {

    public interface OnOrderActionListener {
        void onViewDetail(DeliveryOrder order);
        void onAssignShipper(DeliveryOrder order, int position);
        void onAdvanceStatus(DeliveryOrder order, int position);
    }

    private final List<DeliveryOrder> orders = new ArrayList<>();
    private final OnOrderActionListener listener;

    public OrdersAdapter(OnOrderActionListener listener) {
        this.listener = listener;
    }

    public void setOrders(List<DeliveryOrder> newOrders) {
        orders.clear();
        if (newOrders != null) {
            orders.addAll(newOrders);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public OrderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_order, parent, false);
        return new OrderViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull OrderViewHolder holder, int position) {
        DeliveryOrder order = orders.get(position);

        holder.tvOrderCode.setText("Đơn #" + order.getOrderCode());
        holder.tvOrderStatus.setText(order.getStatus());
        holder.tvCustomerInfo.setText("Khách: " + order.getCustomerName() + " • " + order.getCustomerPhone());
        holder.tvDeliveryAddress.setText("Địa chỉ: " + order.getAddress());

        String shipper = (order.getAssignedShipper() == null || order.getAssignedShipper().trim().isEmpty())
                ? "Chưa phân công"
                : order.getAssignedShipper();
        holder.tvAssignedShipper.setText("Nhân viên giao: " + shipper);

        String formattedMoney = String.format(Locale.getDefault(), "%,d đ", order.getTotalAmount())
                .replace(',', '.');
        holder.tvOrderTotal.setText("Tổng tiền: " + formattedMoney);

        holder.btnOrderDetail.setOnClickListener(v -> {
            if (listener != null) {
                listener.onViewDetail(order);
            }
        });

        boolean canAssign = !DeliveryOrder.STATUS_DELIVERED.equals(order.getStatus())
                && !DeliveryOrder.STATUS_CANCELLED.equals(order.getStatus());
        holder.btnAssignShipper.setEnabled(canAssign);
        holder.btnAssignShipper.setOnClickListener(v -> {
            if (listener != null) {
                listener.onAssignShipper(order, holder.getBindingAdapterPosition());
            }
        });

        String nextStatus = order.getNextWorkflowStatus();
        holder.btnAdvanceStatus.setText(order.getNextActionLabel());
        holder.btnAdvanceStatus.setEnabled(nextStatus != null);
        holder.btnAdvanceStatus.setOnClickListener(v -> {
            if (listener != null) {
                listener.onAdvanceStatus(order, holder.getBindingAdapterPosition());
            }
        });
    }

    @Override
    public int getItemCount() {
        return orders.size();
    }

    static class OrderViewHolder extends RecyclerView.ViewHolder {
        final TextView tvOrderCode;
        final TextView tvOrderStatus;
        final TextView tvCustomerInfo;
        final TextView tvDeliveryAddress;
        final TextView tvAssignedShipper;
        final TextView tvOrderTotal;
        final Button btnOrderDetail;
        final Button btnAssignShipper;
        final Button btnAdvanceStatus;

        OrderViewHolder(@NonNull View itemView) {
            super(itemView);
            tvOrderCode = itemView.findViewById(R.id.tvOrderCode);
            tvOrderStatus = itemView.findViewById(R.id.tvOrderStatus);
            tvCustomerInfo = itemView.findViewById(R.id.tvCustomerInfo);
            tvDeliveryAddress = itemView.findViewById(R.id.tvDeliveryAddress);
            tvAssignedShipper = itemView.findViewById(R.id.tvAssignedShipper);
            tvOrderTotal = itemView.findViewById(R.id.tvOrderTotal);
            btnOrderDetail = itemView.findViewById(R.id.btnOrderDetail);
            btnAssignShipper = itemView.findViewById(R.id.btnAssignShipper);
            btnAdvanceStatus = itemView.findViewById(R.id.btnAdvanceStatus);
        }
    }
}