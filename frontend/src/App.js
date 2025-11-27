import React from "react";
import "./App.css";
import { BrowserRouter, Routes, Route } from "react-router-dom";
import Welcome from "./screens/Welcome";
import SignInUpForm from "./screens/SignInUpForm";
import Home from "./screens/Home";
import Products from "./screens/Products";
import Orders from "./screens/Orders";
import Users from "./screens/Users";
import Categories from "./screens/Categories";
import Login from "./screens/Login";
import Register from "./screens/Register";
import Dashboard from "./screens/Dashboard";
import Goals from "./screens/Goals";
import ClientHome from "./screens/ClientHome";
import ClientProducts from "./screens/ClientProducts";
import MyOrders from "./screens/MyOrders";
import Settings from "./screens/Settings";
import Benefits from "./screens/Benefits";

function App() {
  return (
    
    <div className="App">
      <BrowserRouter>
        <Routes>
          <Route path="/" element={<Welcome />} />
          <Route path="/login" element={<Login />} />
          <Route path="/register" element={<Register />} />
          <Route path="/homepage" element={<Home />} />
          <Route path="/dashboard" element={<Dashboard />} />
          <Route path="/goals" element={<Goals />} />
          <Route path="/products" element={<Products />} />
          <Route path="/orders" element={<Orders />} />
          <Route path="/users" element={<Users />} />
          <Route path="/categories" element={<Categories />} />
          <Route path="/client-home" element={<ClientHome />} />
          <Route path="/client-products" element={<ClientProducts />} />
          <Route path="/my-orders" element={<MyOrders />} />
          <Route path="/settings" element={<Settings />} />
          <Route path="/benefits" element={<Benefits />} />
        </Routes>
      </BrowserRouter>
    </div>
  );
}

export default App;
