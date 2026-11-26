package com.nhom404.bachhoa.admin;

public class DeliveryOrder {
    public static final String STATUS_PENDING = "CHO_XAC_NHAN";
    public static final String STATUS_CONFIRMED = "DA_XAC_NHAN";
    public static final String STATUS_DELIVERING = "DANG_GIAO";
    public static final String STATUS_DELIVERED = "DA_GIAO";
    public static final String STATUS_CANCELLED = "DA_HUY";

    private final String orderCode;
    private final String customerName;
    private final String customerPhone;
    private final String address;
    private final String itemsSummary;
    private final long totalAmount;
    private String status;
    private String assignedShipper;

    public DeliveryOrder(
            String orderCode,
            String customerName,
            String customerPhone,
            String address,
            String itemsSummary,
            long totalAmount,
            String status,
            String assignedShipper
    ) {
        this.orderCode = orderCode;
        this.customerName = customerName;
        this.customerPhone = customerPhone;
        this.address = address;
        this.itemsSummary = itemsSummary;
        this.totalAmount = totalAmount;
        this.status = status;
        this.assignedShipper = assignedShipper;
    }

    public String getOrderCode() {
        return orderCode;
    }

    public String getCustomerName() {
        return customerName;
    }

    public String getCustomerPhone() {
        return customerPhone;
    }

    public String getAddress() {
        return address;
    }

    public String getItemsSummary() {
        return itemsSummary;
    }

    public long getTotalAmount() {
        return totalAmount;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getAssignedShipper() {
        return assignedShipper;
    }

    public void setAssignedShipper(String assignedShipper) {
        this.assignedShipper = assignedShipper;
    }

    public String getNextWorkflowStatus() {
        switch (status) {
            case STATUS_PENDING:
                return STATUS_CONFIRMED;
            case STATUS_CONFIRMED:
                return STATUS_DELIVERING;
            case STATUS_DELIVERING:
                return STATUS_DELIVERED;
            default:
                return null;
        }
    }

    public String getNextActionLabel() {
        switch (status) {
            case STATUS_PENDING:
                return "Xác nhận";
            case STATUS_CONFIRMED:
                return "Bắt đầu giao";
            case STATUS_DELIVERING:
                return "Đã giao";
            default:
                return "Hoàn tất";
        }
    }
}