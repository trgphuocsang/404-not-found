// Lỗi nghiệp vụ: route tạo lỗi kèm mã HTTP, ví dụ httpError(401, "Sai mật khẩu")
function httpError(status, message) {
    const err = new Error(message);
    err.status = status;
    return err;
}

// Express nhận ra đây là middleware lỗi vì hàm có đủ 4 tham số (err, req, res, next)
function errorHandler(err, req, res, next) {
    // Lỗi có status (do mình chủ động tạo) thì dùng, không có thì là lỗi bất ngờ => 500
    const status = err.status || 500;

    if (status === 500) {
        // Lỗi 500 chỉ ghi log ở server, không gửi chi tiết cho client (tránh lộ thông tin)
        console.error(err);
        return res.status(500).json({ message: "Internal Server Error" });
    }

    res.status(status).json({ message: err.message });
}

module.exports = errorHandler;
module.exports.httpError = httpError;
