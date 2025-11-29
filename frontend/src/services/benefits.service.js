import axios from "axios";
import authHeader from "./auth-header";
import apiUrl from "../deploy";

const API_URL = apiUrl + "/api/v1/benefits";

// Get all benefits
const getAllBenefits = () => {
  return axios.get(API_URL, { 
    headers: authHeader(),
    method: "GET",
    "Content-Type": "application/json" 
  });
};

// Get benefit by ID
const getBenefitById = (id) => {
  return axios.get(`${API_URL}/${id}`, { 
    headers: authHeader(),
    method: "GET",
    "Content-Type": "application/json" 
  });
};

// Create new benefit
const createBenefit = (benefitData) => {
  return axios.post(API_URL, benefitData, { 
    headers: authHeader(),
    "Content-Type": "application/json" 
  });
};

// Delete benefit
const deleteBenefit = (id) => {
  return axios.delete(`${API_URL}/${id}`, { 
    headers: authHeader(),
    "Content-Type": "application/json" 
  });
};

// Get benefits by type
const getBenefitsByType = (type) => {
  return axios.get(`${API_URL}/type/${type}`, { 
    headers: authHeader(),
    method: "GET",
    "Content-Type": "application/json" 
  });
};

// Get benefits available for specific points
const getBenefitsForPoints = (points) => {
  return axios.get(`${API_URL}/available/${points}`, { 
    headers: authHeader(),
    method: "GET",
    "Content-Type": "application/json" 
  });
};

// Check if benefit is duplicate
const checkDuplicateBenefit = (benefitData) => {
  return axios.post(`${API_URL}/check-duplicate`, benefitData, { 
    headers: authHeader(),
    "Content-Type": "application/json" 
  });
};

const benefitsService = {
  getAllBenefits,
  getBenefitById,
  createBenefit,
  deleteBenefit,
  getBenefitsByType,
  getBenefitsForPoints,
  checkDuplicateBenefit
};

export default benefitsService;