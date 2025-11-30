import axios from "axios";
import authHeader from "./auth-header";
import apiUrl from "../deploy";

const API_URL_BASE = apiUrl + "/api/v1/client";

const getCurrentPoints = () => {
  return axios.get(`${API_URL_BASE}/points`, { headers: authHeader() });
};

const getPointsHistory = () => {
  return axios.get(`${API_URL_BASE}/points/history`, { headers: authHeader() });
};

const pointsService = {
  getCurrentPoints,
  getPointsHistory,
};

export default pointsService;