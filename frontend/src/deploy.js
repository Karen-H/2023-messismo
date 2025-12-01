let apiUrl;

if (process.env.NODE_ENV === "production") {
    apiUrl = process.env.REACT_APP_API_URL_PROD || "https://backend-moe-production.up.railway.app";
} else {
    apiUrl = process.env.REACT_APP_API_URL_DEV || "http://localhost:8080";
}

export default apiUrl;
