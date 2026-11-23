require("dotenv").config();

const express = require("express");
const app = express();

app.use(express.json());

const healthRoutes = require("./routes/health");
const errorHandler = require("./middleware/errorHandler");

const authRoutes = require("./routes/auth");

app.use("/health", healthRoutes);
app.use("/auth", authRoutes);

app.use(errorHandler);

const PORT = process.env.PORT || 3000;

app.listen(PORT, () => {
    console.log(`Server running on port ${PORT}`);
});
