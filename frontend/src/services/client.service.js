import axios from "axios";
import authHeader from "./auth-header";
import apiUrl from "../deploy";

const API_URL = apiUrl + "/api/v1/client";

const getProducts = () => {
  return axios.get(API_URL + "/products", { 
    headers: authHeader(),
    method: "GET",
    "Content-Type": "application/json" 
  });
};

const getProfile = () => {
  return axios.get(API_URL + "/profile", { 
    headers: authHeader(),
    method: "GET",
    "Content-Type": "application/json" 
  });
};

const getClientOrders = () => {
  return axios.get(API_URL + "/orders", { 
    headers: authHeader(),
    method: "GET",
    "Content-Type": "application/json" 
  });
};

const clientService = {
  getProducts,
  getProfile,
  getClientOrders
};

export default clientService;